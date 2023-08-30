/*
 * Copyright (c) 2013 ResiliNets, ITTC, University of Kansas
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Authors: Siddharth Gangadhar <siddharth@ittc.ku.edu>, Truc Anh N. Nguyen <annguyen@ittc.ku.edu>,
 * and Greeshma Umapathi
 *
 * James P.G. Sterbenz <jpgs@ittc.ku.edu>, director
 * ResiliNets Research Group  https://resilinets.org/
 * Information and Telecommunication Technology Center (ITTC)
 * and Department of Electrical Engineering and Computer Science
 * The University of Kansas Lawrence, KS USA.
 *
 * Work supported in part by NSF FIND (Future Internet Design) Program
 * under grant CNS-0626918 (Postmodern Internet Architecture),
 * NSF grant CNS-1050226 (Multilayer Network Resilience Analysis and Experimentation on GENI),
 * US Department of Defense (DoD), and ITTC at The University of Kansas.
 */

#ifndef TCPADAPTIVERENO_H
#define TCPADAPTIVERENO_H

#include "tcp-congestion-ops.h"
#include "ns3/tcp-westwood-plus.h"
#include "ns3/data-rate.h"
#include "ns3/event-id.h"
#include "ns3/tcp-recovery-ops.h"
#include "ns3/traced-value.h"



namespace ns3
{

class Time;

class TcpAdaptiveReno : public TcpWestwoodPlus
{
  public:

    static TypeId GetTypeId();

    TcpAdaptiveReno();
    TcpAdaptiveReno(const TcpAdaptiveReno& sock);
    ~TcpAdaptiveReno() override;
    std::string GetName() const override;
    
    enum FilterType
    {
        NONE,
        TUSTIN
    };

    uint32_t GetSsThresh(Ptr<const TcpSocketState> tcb, uint32_t bytesInFlight) override;
    
    void PktsAcked(Ptr<TcpSocketState> tcb, uint32_t packetsAcked, const Time& rtt) override;
    
    Ptr<TcpCongestionOps> Fork() override;

  private:
    double EstimateCongestionLevel();
    int32_t EstimateIncWnd(Ptr<TcpSocketState> tcb);
    void EstimateBW(const Time& rtt, Ptr<TcpSocketState> tcb);


  protected:
    void CongestionAvoidance(Ptr<TcpSocketState> tcb, uint32_t segmentsAcked) override;
    uint32_t m_wBase; 
    int32_t m_wProbe;
    
    Time m_RTT;  
    Time m_minRTT{Time::Max()};      
    Time m_packet_drop_RTT; 
    Time m_RTT_cong;     
    Time m_RTT_cong_prev;
};

}

#endif