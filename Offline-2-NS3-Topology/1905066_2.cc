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

#include "ns3/applications-module.h"
#include "ns3/core-module.h"
#include "ns3/internet-module.h"
#include "ns3/network-module.h"
#include "ns3/mobility-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/ssid.h"
#include "ns3/yans-wifi-helper.h"
#include <fstream>


/*
    sender                                  receiver

    n2  --*                                 *-- n7

    n3  --*     AP                  AP      *-- n8
                        10.1.1.0
    n4  --*   *--n0 ---------------- n1--*  *-- n9
                    point-to-point
    n5  --*                                 *-- n10

    n6  --*                                 *-- n11

wifi 10.1.2.0                              wifi 10.1.3.0
*/

using namespace ns3;

/*Define a Log component with a specific name.

This macro should be used at the top of every file in which you want to use 
the NS_LOG macro. This macro defines a new "log component" which can be later 
selectively enabled or disabled with the ns3::LogComponentEnable and 
ns3::LogComponentDisable functions or with the NS_LOG environment variable.*/

NS_LOG_COMPONENT_DEFINE("1905066_1");

uint32_t packets_sent = 0;
uint32_t packets_received = 0;
uint32_t packet_size = 1024; 

void RxReceive(std::string context,  Ptr<const Packet> packet, const Address& address) {
    packets_received += packet->GetSize()/packet_size;
}

void TxSent(std::string context, Ptr<const Packet> packet) {
    packets_sent++;
}


int
main(int argc, char* argv[])
{
    CommandLine cmd(__FILE__);

    // ========================== variables ==========================
    std::string gatewayBandwidth= "1Mbps";
    std::string gatewayDelay = "5ms";

    uint32_t nNodes = 10;
    uint32_t nFlows = 20;
    uint64_t nPacketsPerSecond = 100;
    uint32_t speed = 5; // m/s;

    double simulationTime = 10;
    
    // ========================== Command Line ==========================
    cmd.AddValue("nNodes", "Number of nodes in each side", nNodes);
    cmd.AddValue("nFlows", "Number of flows", nFlows);
    cmd.AddValue("nPacketsPerSeconds", "Number of packets sent per second from each application", nPacketsPerSecond);
    cmd.AddValue("speed", "speed of nodes", speed);
    cmd.AddValue("simulationTime", "Simulation Time", simulationTime);
    cmd.Parse(argc, argv);
    

    /* Configure TCP Options */
    Config::SetDefault("ns3::TcpSocket::SegmentSize", UintegerValue(packet_size));

    //  sets the time resolution to one nanosecond
    Time::SetResolution(Time::NS);

    // Add logging to code
    NS_LOG_INFO("Creating Topology");

    // ========================== gateway ==========================
    NodeContainer gatewayNodes;
    gatewayNodes.Create(2);

    PointToPointHelper gateway;
    gateway.SetDeviceAttribute("DataRate", StringValue(gatewayBandwidth)); 
    gateway.SetChannelAttribute("Delay", StringValue(gatewayDelay));

    NetDeviceContainer gatewayDevices;
    gatewayDevices = gateway.Install(gatewayNodes);

    // ========================== Wifi ==========================
    NodeContainer wifiSenderNodes;
    NodeContainer wifiReceiverNodes;
    NodeContainer wifiApSenderNode = gatewayNodes.Get(0);
    NodeContainer wifiApReceiverNode = gatewayNodes.Get(1);
    wifiReceiverNodes.Create(nNodes);
    wifiSenderNodes.Create(nNodes);

    YansWifiChannelHelper channelLeft = YansWifiChannelHelper::Default();
    YansWifiChannelHelper channelRight = YansWifiChannelHelper::Default();

    YansWifiPhyHelper phyLeft, phyRight;
    phyLeft.SetChannel(channelLeft.Create());
    phyRight.SetChannel(channelRight.Create());

    WifiMacHelper macLeft, macRight;
    Ssid ssidLeft = Ssid("ns-3-ssid-left");
    Ssid ssidRight = Ssid("ns-3-ssid-right"); 

    WifiHelper wifi;

    NetDeviceContainer staLeftDevices, staRightDevices;
    macLeft.SetType("ns3::StaWifiMac", "Ssid", SsidValue(ssidLeft), "ActiveProbing", BooleanValue(false));
    macRight.SetType("ns3::StaWifiMac", "Ssid", SsidValue(ssidRight), "ActiveProbing", BooleanValue(false));
    staLeftDevices = wifi.Install(phyLeft, macLeft, wifiSenderNodes);
    staRightDevices = wifi.Install(phyRight, macRight, wifiReceiverNodes);

    NetDeviceContainer apLeftDevices, apRightDevices;
    macLeft.SetType("ns3::ApWifiMac", "Ssid", SsidValue(ssidLeft));
    macRight.SetType("ns3::ApWifiMac", "Ssid", SsidValue(ssidRight));
    apLeftDevices = wifi.Install(phyLeft, macLeft, wifiApSenderNode);
    apRightDevices = wifi.Install(phyRight, macRight, wifiApReceiverNode);

    MobilityHelper mobility;

    // random disc mobility model
    mobility.SetPositionAllocator("ns3::RandomDiscPositionAllocator",
                                "X",
                                StringValue("100.0"),
                                "Y",
                                StringValue("100.0"),
                                "Rho",
                                StringValue("ns3::UniformRandomVariable[Min=0|Max=50]"));

    // set speed and bounds
    mobility.SetMobilityModel("ns3::RandomWalk2dMobilityModel",
                            "Bounds", RectangleValue(Rectangle(0, 200, 0, 200)),
                            "Speed", StringValue("ns3::ConstantRandomVariable[Constant="+std::to_string(speed)+"]"));
    
    mobility.Install(wifiReceiverNodes);
    mobility.Install(wifiSenderNodes);
    
    mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
    mobility.Install(wifiApReceiverNode);
    mobility.Install(wifiApSenderNode);

    // ========================== Internet Stack ==========================
    InternetStackHelper stack;
    stack.Install(wifiReceiverNodes);
    stack.Install(wifiSenderNodes);
    stack.Install(wifiApReceiverNode);
    stack.Install(wifiApSenderNode);

    // ========================== IP Addresses ==========================
    Ipv4AddressHelper addressgateway;
    addressgateway.SetBase("10.1.1.0", "255.255.255.0");
    Ipv4InterfaceContainer gatewayInterfaces;
    gatewayInterfaces = addressgateway.Assign(gatewayDevices);

    Ipv4AddressHelper addressLeft;
    addressLeft.SetBase("10.1.2.0", "255.255.255.0");
    Ipv4InterfaceContainer staLeftInterfaces;
    staLeftInterfaces = addressLeft.Assign(staLeftDevices);
    Ipv4InterfaceContainer apLeftInterfaces;
    apLeftInterfaces = addressLeft.Assign(apLeftDevices);
    

    Ipv4AddressHelper addressRight;
    addressRight.SetBase("10.1.3.0", "255.255.255.0");
    Ipv4InterfaceContainer staRightInterfaces;
    staRightInterfaces = addressRight.Assign(staRightDevices);
    Ipv4InterfaceContainer apRightInterfaces;
    apRightInterfaces = addressRight.Assign(apRightDevices);

    
    // ========================== Applications ==========================
    // receiver
    uint16_t port = 9;
    PacketSinkHelper packetSinkHelper("ns3::TcpSocketFactory", InetSocketAddress(Ipv4Address::GetAny(), port));
    ApplicationContainer sinkApps = packetSinkHelper.Install(wifiReceiverNodes);
    
    std::vector<Ptr<PacketSink>> sinks;
    for (uint32_t i = 0; i < sinkApps.GetN(); i++) sinks.push_back(StaticCast<PacketSink>(sinkApps.Get(i)));

    for(uint32_t i = 0; i< nNodes; i++){
        std::ostringstream oss;
        oss << "/NodeList/" << wifiReceiverNodes.Get(i)->GetId() << "/ApplicationList/0/$ns3::PacketSink/Rx";
        Config::Connect(oss.str(), MakeCallback(&RxReceive));
    }


    // sender
    ApplicationContainer senderApps;
    OnOffHelper onOffHelper("ns3::TcpSocketFactory", Address());
    onOffHelper.SetAttribute("OnTime", StringValue("ns3::ConstantRandomVariable[Constant=1]"));
    onOffHelper.SetAttribute("OffTime", StringValue("ns3::ConstantRandomVariable[Constant=0]"));
    onOffHelper.SetAttribute("DataRate", DataRateValue(DataRate(packet_size*nPacketsPerSecond*8))); //DataRate constructor takes bits per second
    onOffHelper.SetAttribute("PacketSize", UintegerValue(packet_size));

    for(uint32_t i = 0, app_count = 0; i< nFlows; i++){
        uint32_t receiver_index = (i + app_count) % nNodes;
        uint32_t sender_index = i % nNodes;
        onOffHelper.SetAttribute("Remote", AddressValue(InetSocketAddress(staRightInterfaces.GetAddress(receiver_index), port)));
        senderApps.Add(onOffHelper.Install(wifiSenderNodes.Get(sender_index)));
        
        std::ostringstream oss;
        oss << "/NodeList/" << wifiSenderNodes.Get(sender_index)->GetId() << "/ApplicationList/"<<app_count<<"/$ns3::OnOffApplication/Tx";
        Config::Connect(oss.str(), MakeCallback(&TxSent));
        
        if(i%nNodes == 0 && i!=0) app_count++;
    }


    sinkApps.Start(Seconds(0.0));
    senderApps.Start(Seconds(1.0));

    // ========================== Routing ==========================
    Ipv4GlobalRoutingHelper::PopulateRoutingTables();

    // ============================ Run ============================
    Simulator::Stop(Seconds(simulationTime));
    Simulator::Run();

    // ========================== Results ==========================
    double totalRx = 0.0;
    for (size_t i = 0; i < sinks.size(); ++i) totalRx += sinks[i]->GetTotalRx();
    double throughput = totalRx * 8.0 / 1e6 / simulationTime;
    double packet_delivery_ratio = (double)packets_received / (double)packets_sent;

    //std::cout << "Total throughput: " << totalRx * 8.0 / 1e6 / simulationTime << " Mbps" << std::endl;
    // std::cout << "Total packets sent: " << packets_sent << std::endl;
    // std::cout << "Total packets received: " << packets_received << std::endl;
    // std::cout << "Packet delivery ratio: " << (double)packets_received / (double)packets_sent << std::endl;

    // append delivery ratio to file
    std::ofstream output_delivery_ratio, output_throughput;
    output_delivery_ratio.open("scratch/output/1905066_2_delivery_ratio.dat", std::ios::app);
    output_throughput.open("scratch/output/1905066_2_throughput.dat", std::ios::app);
    output_delivery_ratio<<nNodes<<" "<<nFlows<<" "<<nPacketsPerSecond<<" "<<speed<<" "<<packet_delivery_ratio<<std::endl;
    output_throughput<<nNodes<<" "<<nFlows<<" "<<nPacketsPerSecond<<" "<<speed<<" "<<throughput<<std::endl;
    output_delivery_ratio.close();
    output_throughput.close();

    std::cout<<"Simulation done"<<std::endl;
    
    // ========================== Cleanup ==========================
    Simulator::Destroy();
    return 0;
}


