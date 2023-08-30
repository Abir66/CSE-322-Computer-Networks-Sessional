/*
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
 */
#include "tutorial-app.h"

#include "ns3/applications-module.h"
#include "ns3/core-module.h"
#include "ns3/flow-monitor-module.h"
#include "ns3/internet-module.h"
#include "ns3/network-module.h"
#include "ns3/point-to-point-dumbbell.h"
#include "ns3/point-to-point-module.h"

#include <fstream>

// dumbbell topology
//
//     TCP Senders              TCP Receivers
//
//         n2---                  ---n4
//              |    10.1.1.0     |
//   10.1.2.0  n0 -------------- n1    10.1.3.0
//              |                 |
//         n3---                  ---n5

using namespace ns3;

/*Define a Log component with a specific name.

This macro should be used at the top of every file in which you want to use
the NS_LOG macro. This macro defines a new "log component" which can be later
selectively enabled or disabled with the ns3::LogComponentEnable and
ns3::LogComponentDisable functions or with the NS_LOG environment variable.*/

NS_LOG_COMPONENT_DEFINE("1905066_OFFLINE_2");

static void
CwndChange(Ptr<OutputStreamWrapper> stream, uint32_t oldCwnd, uint32_t newCwnd)
{
    // NS_LOG_UNCOND(Simulator::Now().GetSeconds() << "\t" << newCwnd);
    *stream->GetStream() << Simulator::Now().GetSeconds() << "\t" << newCwnd
                         << std::endl;
}

int
main(int argc, char* argv[])
{
    CommandLine cmd(__FILE__);

    // ========================== variables ==========================
    uint32_t packet_size = 1024;
    uint32_t nLeftLeaf = 2;
    uint32_t nRightLeaf = 2;
    std::string dataRate = "1Gbps";
    std::string delay = "1ms";
    std::string bottleneck_dataRate = "100Mbps";
    std::string bottleneck_delay = "100ms";
    double packet_loss_rate_power = -6;
    double simulation_time = 60.0;
    double cleanup_time = 5.0;
    std::string output_file = "../output/output.txt";
    std::string congestion_output_folder = "../output";
    bool trace_cwnd = true;

    std::string algo[] = {"TcpNewReno", "TcpHighSpeed"};

    // ========================== command line ==========================
    cmd.AddValue("packetSize", "Payload size in bytes", packet_size);
    cmd.AddValue("dataRate", "Data rate of the point to point links", dataRate);
    cmd.AddValue("delay", "Delay of the point to point links", delay);
    cmd.AddValue("bottleneck_dataRate", "Data rate of the bottleneck link", bottleneck_dataRate);
    cmd.AddValue("bottleneck_delay", "Delay of the bottleneck link", bottleneck_delay);
    cmd.AddValue("packet_loss_rate", "Packet loss rate of the bottleneck link", packet_loss_rate_power);
    cmd.AddValue("simulation_time", "Simulation time in seconds", simulation_time);
    cmd.AddValue("cleanup_time", "Cleanup time in seconds", cleanup_time);
    cmd.AddValue("output_file", "Output file name", output_file);
    cmd.AddValue("congestion_output_folder", "Congestion output folder", congestion_output_folder);
    cmd.AddValue("trace_cwnd", "Trace congestion window", trace_cwnd);
    
    // take algo input from command line
    cmd.AddValue("algo1", "Congestion control algorithm", algo[0]);
    cmd.AddValue("algo2", "Congestion control algorithm", algo[1]);

    cmd.Parse(argc, argv);
    //std::cout<<bottleneck_dataRate<<std::endl;
    double packet_loss_rate = std::pow(10.0, packet_loss_rate_power);

    // print the input parameters
    // std::cout << "packetSize = " << packet_size << std::endl;
    // std::cout << "dataRate = " << dataRate << std::endl;
    // std::cout << "delay = " << delay << std::endl;
    // std::cout << "bottleneck_dataRate = " << bottleneck_dataRate << std::endl;
    // std::cout << "bottleneck_delay = " << bottleneck_delay << std::endl;
    // std::cout << "packet_loss_rate = " << packet_loss_rate << std::endl;
    // std::cout << "simulation_time = " << simulation_time << std::endl;
    // std::cout << "cleanup_time = " << cleanup_time << std::endl;
    // std::cout << "output_file = " << output_file << std::endl;
    // std::cout << "congestion_output_folder = " << congestion_output_folder << std::endl;
    // std::cout << "trace_cwnd = " << trace_cwnd << std::endl;    
    // std::cout << "algo1 = " << algo[0] << std::endl;
    // std::cout << "algo2 = " << algo[1] << std::endl;
    


    //  sets the time resolution to one nanosecond
    Time::SetResolution(Time::NS);

    // Add logging to code
    NS_LOG_INFO("Creating Topology");

    // ========================== Topology ==========================
    PointToPointHelper p2pLeaf;
    p2pLeaf.SetDeviceAttribute("DataRate", StringValue(dataRate));
    p2pLeaf.SetChannelAttribute("Delay", StringValue(delay));

    double bottleneck_dataRate_in_Mbps = std::stod(bottleneck_dataRate.substr(0, bottleneck_dataRate.size() - 4));
    double bottleneck_delay_in_ms = std::stod(bottleneck_delay.substr(0, bottleneck_delay.size() - 2));
    double bandwidth_delay_product = bottleneck_dataRate_in_Mbps *1000 * bottleneck_delay_in_ms/packet_size / 64.0; 

    p2pLeaf.SetQueue("ns3::DropTailQueue", "MaxSize", StringValue(std::to_string(bandwidth_delay_product) +"p"));

    PointToPointHelper p2pBottleneck;
    p2pBottleneck.SetDeviceAttribute("DataRate", StringValue(bottleneck_dataRate));
    p2pBottleneck.SetChannelAttribute("Delay", StringValue(bottleneck_delay));

    PointToPointDumbbellHelper p2pDumbbell(nLeftLeaf, p2pLeaf, nRightLeaf, p2pLeaf, p2pBottleneck);

    // add rate error model
    Ptr<RateErrorModel> error_model = CreateObject<RateErrorModel>();
    error_model->SetAttribute("ErrorRate", DoubleValue(packet_loss_rate));
    p2pDumbbell.m_routerDevices.Get(0)->SetAttribute("ReceiveErrorModel", PointerValue(error_model));
    p2pDumbbell.m_routerDevices.Get(1)->SetAttribute("ReceiveErrorModel", PointerValue(error_model));

    
    
    // ========================== Internet Stack ==========================
    Config::SetDefault("ns3::TcpL4Protocol::SocketType", StringValue("ns3::" + algo[0]));
    InternetStackHelper stack1;
    stack1.Install(p2pDumbbell.GetLeft(0));
    stack1.Install(p2pDumbbell.GetRight(0));

    Config::SetDefault("ns3::TcpL4Protocol::SocketType", StringValue("ns3::" + algo[1]));
    InternetStackHelper stack2;
    stack2.Install(p2pDumbbell.GetLeft(1));
    stack2.Install(p2pDumbbell.GetRight(1));

    Config::SetDefault("ns3::TcpL4Protocol::SocketType", StringValue("ns3::" + algo[0]));
    InternetStackHelper stack3;
    stack3.Install(p2pDumbbell.GetLeft());
    stack3.Install(p2pDumbbell.GetRight());

    
    
    // ========================== IP Addresses ==========================
    p2pDumbbell.AssignIpv4Addresses(Ipv4AddressHelper("10.1.1.0", "255.255.255.0"),
                                    Ipv4AddressHelper("10.2.1.0", "255.255.255.0"),
                                    Ipv4AddressHelper("10.3.1.0", "255.255.255.0"));

    
    
    // ========================== Routing ==========================
    Ipv4GlobalRoutingHelper::PopulateRoutingTables();


    // ========================== Applications ==========================
    uint16_t port = 9;
    for (uint32_t i = 0; i < p2pDumbbell.RightCount(); i++)
    {
        PacketSinkHelper packetSinkHelper("ns3::TcpSocketFactory",
                                          InetSocketAddress(Ipv4Address::GetAny(), port));
        ApplicationContainer sinkApp = packetSinkHelper.Install(p2pDumbbell.GetRight(i));
        sinkApp.Start(Seconds(0.0));
        sinkApp.Stop(Seconds(simulation_time));
    }
 
    for (uint32_t i = 0; i < p2pDumbbell.LeftCount(); i++)
    {
        Ptr<Socket> ns3TcpSocket =
            Socket::CreateSocket(p2pDumbbell.GetLeft(i), TcpSocketFactory::GetTypeId());
        Ptr<TutorialApp> app = CreateObject<TutorialApp>();
        Address sinkAddress(InetSocketAddress(p2pDumbbell.GetRightIpv4Address(i), port));
        app->Setup(ns3TcpSocket, sinkAddress, packet_size, simulation_time, DataRate(dataRate));
        app->SetStartTime(Seconds(1.0));
        app->SetStopTime(Seconds(simulation_time));

        p2pDumbbell.GetLeft(i)->AddApplication(app);

        if(trace_cwnd){
            AsciiTraceHelper asciiTraceHelper;
            Ptr<OutputStreamWrapper> stream = asciiTraceHelper.CreateFileStream(congestion_output_folder + "/" +algo[i] + ".cwnd");
            ns3TcpSocket->TraceConnectWithoutContext("CongestionWindow", MakeBoundCallback(&CwndChange, stream));
        }
        
    }

    FlowMonitorHelper flowmon;
    Ptr<FlowMonitor> flowMonitor = flowmon.InstallAll();


    Simulator::Stop(Seconds(simulation_time +cleanup_time));
    Simulator::Run();

    /*
    The data collected for each flow are:
        timeFirstTxPacket: when the first packet in the flow was transmitted;
        timeLastTxPacket: when the last packet in the flow was transmitted;
        timeFirstRxPacket: when the first packet in the flow was received by an end node;
        timeLastRxPacket: when the last packet in the flow was received;
        delaySum: the sum of all end-to-end delays for all received packets of the flow;
        jitterSum: the sum of all end-to-end delay jitter (delay variation) values for all received packets of the flow, as defined in RFC 3393;
        txBytes, txPackets: total number of transmitted bytes / packets for the flow;
        rxBytes, rxPackets: total number of received bytes / packets for the flow;
        lostPackets: total number of packets that are assumed to be lost (not reported over 10 seconds);
        timesForwarded: the number of times a packet has been reportedly forwarded;
        delayHistogram, jitterHistogram, packetSizeHistogram: histogram versions for the delay, jitter, and packet sizes, respectively;
        packetsDropped, bytesDropped: the number of lost packets and bytes, divided according to the loss reason code (defined in the probe).
    */

    //flowMonitor->SerializeToXmlFile("NameOfFile.xml", true, true);
    //  flow 1: algo1, src->dest
    //  flow 2: algo2, src->dest
    //  flow 3: algo1, dest->src
    //  flow 4: algo2, dest->src

    // ========================== Flow Monitor =========================
    uint32_t algo1_rxBytes = 0;
    uint32_t algo2_rxBytes = 0;
    int i = 0;


    
    for(auto flow : flowMonitor->GetFlowStats ()){
        if(i%2 == 0) algo1_rxBytes += flow.second.rxBytes;
        else algo2_rxBytes += flow.second.rxBytes;
        i++;
    }

    double algo1_throughput = (algo1_rxBytes * 8.0) / (simulation_time * 1000000.0); // Mbps
    double algo2_throughput = (algo2_rxBytes * 8.0) / (simulation_time * 1000000.0); // Mbps

    // jain's fairness index
    double nominator = (algo1_throughput + algo2_throughput) * (algo1_throughput + algo2_throughput);
    double denominator = 2 * (algo1_throughput * algo1_throughput + algo2_throughput * algo2_throughput);
    if (denominator == 0)
        denominator = 1; // to avoid division by zero (when both algo have 0 throughput
    
    double fairness_index = nominator / denominator;

    // std::cout << "Throughput of " << algo[0] << " = " << algo1_throughput << " Mbps" << std::endl;
    // std::cout << "Throughput of " << algo[1] << " = " << algo2_throughput << " Mbps" << std::endl;
    // std::cout << "Jain's fairness index = " << fairness_index << std::endl;

    // append in ouptup file
    std::ofstream out(output_file, std::ios_base::app);

    out<<bottleneck_dataRate_in_Mbps<<" "<<packet_loss_rate_power<<" "<<algo1_throughput*1000<<" "<<algo2_throughput*1000<<" "<<fairness_index<<std::endl;
    out.close();


    Simulator::Destroy(); // destroy all objects created
    return 0;
}
