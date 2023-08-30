#include "tcp-adaptive-reno.h"

#include "ns3/log.h"
#include "ns3/simulator.h"

// #include "rtt-estimator.h"
// #include "tcp-socket-base.h"


NS_LOG_COMPONENT_DEFINE("TcpAdaptiveReno");

namespace ns3
{

NS_OBJECT_ENSURE_REGISTERED(TcpAdaptiveReno);

TypeId
TcpAdaptiveReno::GetTypeId(void)
{
    static TypeId tid =
        TypeId("ns3::TcpAdaptiveReno")
            .SetParent<TcpNewReno>()
            .SetGroupName("Internet")
            .AddConstructor<TcpAdaptiveReno>()
            .AddAttribute(
                "FilterType",
                "Use this to choose no filter or Tustin's approximation filter",
                EnumValue(TcpAdaptiveReno::TUSTIN),
                MakeEnumAccessor(&TcpAdaptiveReno::m_fType),
                MakeEnumChecker(TcpAdaptiveReno::NONE, "None", TcpAdaptiveReno::TUSTIN, "Tustin"))
            .AddTraceSource("EstimatedBW",
                            "The estimated bandwidth",
                            MakeTraceSourceAccessor(&TcpAdaptiveReno::m_currentBW),
                            "ns3::TracedValueCallback::Double");
    return tid;
}

TcpAdaptiveReno::TcpAdaptiveReno()
    : TcpWestwoodPlus(),
      m_wBase(0),
      m_wProbe(0),
      m_RTT(Time(0)),
      m_minRTT(Time::Max()),
      m_packet_drop_RTT(Time(0)),
      m_RTT_cong(Time(0)),
      m_RTT_cong_prev(Time(0))

{
    NS_LOG_FUNCTION(this);
}

TcpAdaptiveReno::TcpAdaptiveReno(const TcpAdaptiveReno& sock)
    : TcpWestwoodPlus(sock),
      m_wBase(sock.m_wBase),
      m_wProbe(sock.m_wProbe),
      m_RTT(sock.m_RTT),
      m_minRTT(sock.m_minRTT),
      m_packet_drop_RTT(sock.m_packet_drop_RTT),
      m_RTT_cong(sock.m_RTT_cong),
      m_RTT_cong_prev(sock.m_RTT_cong_prev)
{
    NS_LOG_FUNCTION(this);
    NS_LOG_LOGIC("Invoked the copy constructor");
}

TcpAdaptiveReno::~TcpAdaptiveReno(void)
{
}

std::string
TcpAdaptiveReno::GetName() const
{
    return "TcpAdaptiveReno";
}


void
TcpAdaptiveReno::PktsAcked(Ptr<TcpSocketState> tcb, uint32_t packetsAcked, const Time& rtt)
{
    NS_LOG_FUNCTION(this << tcb << packetsAcked << rtt);

    if (rtt.IsZero())
    {
        NS_LOG_WARN("RTT measured is zero!");
        return;
    }

    m_ackedSegments += packetsAcked;
   
    if(m_minRTT.IsZero()) m_minRTT = rtt; 
    else if(rtt <= m_minRTT) m_minRTT = rtt; 
    
    m_RTT = rtt;

    TcpWestwoodPlus::EstimateBW(rtt, tcb);
}

double
TcpAdaptiveReno::EstimateCongestionLevel()
{
    double a = 0.85;


    double cong_RTT_j = a * m_RTT_cong_prev.GetSeconds() + (1 - a) * m_packet_drop_RTT.GetSeconds();

    if(m_RTT_cong_prev < m_minRTT) cong_RTT_j = m_packet_drop_RTT.GetSeconds(); //first event

    m_RTT_cong = Seconds(cong_RTT_j);

    double c = (m_RTT.GetSeconds() - m_minRTT.GetSeconds()) / (m_RTT_cong.GetSeconds() - m_minRTT.GetSeconds());
    return std::min(1.0, c);
}

int32_t
TcpAdaptiveReno::EstimateIncWnd(Ptr<TcpSocketState> tcb)
{
    double c = EstimateCongestionLevel(); // congestion level
    double m = 1000;                      // scaling factor
    double mss = static_cast<double>(tcb->m_segmentSize * tcb->m_segmentSize);
    
    double W_inc_max = m_currentBW.Get().GetBitRate() /8.0/ m * mss;

    double alpha = 10;
    double beta = 2 * W_inc_max * ((1 / alpha) - ((1 / alpha + 1) / (std::exp(alpha))));
    double gamma = 1 - (2 * W_inc_max * ((1 / alpha) - ((1 / alpha + 1.0 / 2) / (std::exp(alpha)))));

    double w_inc = W_inc_max / std::exp(alpha * c) + (beta * c) + gamma;

    return w_inc;
}

void
TcpAdaptiveReno::CongestionAvoidance(Ptr<TcpSocketState> tcb, uint32_t segmentsAcked)
{
    NS_LOG_FUNCTION(this << tcb << segmentsAcked);

    if (segmentsAcked > 0)
    {
        double wInc = EstimateIncWnd(tcb); // update wInc
        
        double mss = static_cast<double>(tcb->m_segmentSize * tcb->m_segmentSize);
        m_wBase += static_cast<uint32_t>(std::max(1.0, mss/tcb->m_cWnd.Get()));
        m_wProbe = std::max((double)(m_wProbe + wInc / (int)tcb->m_cWnd.Get()), 0.0);

        tcb->m_cWnd = m_wBase + m_wProbe;
    }
}

uint32_t
TcpAdaptiveReno::GetSsThresh(Ptr<const TcpSocketState> tcb, uint32_t bytesInFlight)
{
    m_RTT_cong_prev = m_RTT_cong;
    m_packet_drop_RTT = m_RTT;
    
    double congestion_level = EstimateCongestionLevel();

    m_wBase = tcb->m_cWnd / (1.0 + congestion_level);
    m_wBase = std::max(m_wBase, 2 * tcb->m_segmentSize);

    m_wProbe = 0;

    return m_wBase;
}

Ptr<TcpCongestionOps>
TcpAdaptiveReno::Fork()
{
    return CreateObject<TcpAdaptiveReno>(*this);
}

} // namespace ns3