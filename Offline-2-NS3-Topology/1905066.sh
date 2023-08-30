cd ns-3.39
mkdir -p scratch/output
touch scratch/output/1905066_1_delivery_ratio.dat
touch scratch/output/1905066_1_throughput.dat
touch scratch/output/1905066_2_delivery_ratio.dat
touch scratch/output/1905066_2_throughput.dat

# parameters - title, xlabel, ylabel, input file, output file, x column, y column
plot_graph () {
    gnuCommand="set terminal pngcairo size 1024,768 enhanced font 'Verdana,12'; 
                set output 'scratch/output/$5.png'; 
                set title '$1' font 'Verdana,16'; 
                set xlabel '$2' font 'Verdana,14'; 
                set ylabel '$3' font 'Verdana,14'; 
                set grid;
                set style line 1 lc rgb '#0060ad' lt 1 lw 2 pt 7 ps 1.5; 
                set border 3; 
                set xtics nomirror; 
                set ytics nomirror;
                set lmargin 12;
                set rmargin 6;
                set tmargin 6;
                set bmargin 6;
                plot '$4' using $6:$7 title '$1' with linespoints linestyle 1;
                exit;"
    
    echo $gnuCommand | gnuplot
}

clear_output () {
    > scratch/output/1905066_1_delivery_ratio.dat
    > scratch/output/1905066_1_throughput.dat
    > scratch/output/1905066_2_delivery_ratio.dat
    > scratch/output/1905066_2_throughput.dat
}

# --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


vary_number_of_nodes(){

    clear_output

    nNodes=(10 20 30 40 50 60 70 80 90 100)
    nFlows=(20 30 40 50 60 80 80 100 100 120)

    for i in {0..9}
    do
        ./ns3 run "scratch/1905066_1.cc --nNodes=${nNodes[$i]} --nFlows=${nFlows[$i]} --nPacketsPerSeconds=500"
        ./ns3 run "scratch/1905066_2.cc --nNodes=${nNodes[$i]} --nFlows=${nFlows[$i]} --nPacketsPerSeconds=500"
    done
}

# --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

vary_number_of_flows () {

    clear_output

    nNodes=20
    nFlows=(20 30 40 50 60 70 80 90 100)
    nPacketsPerSeconds=500
    for flow in ${nFlows[@]}
    do
        ./ns3 run "scratch/1905066_1.cc --nNodes=$nNodes --nFlows=$flow --nPacketsPerSeconds=$nPacketsPerSeconds"
        ./ns3 run "scratch/1905066_2.cc --nNodes=$nNodes --nFlows=$flow --nPacketsPerSeconds=$nPacketsPerSeconds"
    done

    # plot delivery ratio vs number of flows
    plot_graph "Delivery Ratio vs Number of Flows(static)" "Number of Flows" "Delivery Ratio" "scratch/output/1905066_1_delivery_ratio.dat" "1905066_static_flow_delivary_ratio" 2 5
    plot_graph "Delivery Ratio vs Number of Flows(mobile)" "Number of Flows" "Delivery Ratio" "scratch/output/1905066_2_delivery_ratio.dat" "1905066_mobile_flow_delivary_ratio" 2 5
    plot_graph "Throughput vs Number of Flows(static)" "Number of Flows" "Throughput(Mbps)" "scratch/output/1905066_1_throughput.dat" "1905066_static_flow_throughput" 2 5
    plot_graph "Throughput vs Number of Flows(mobile)" "Number of Flows" "Throughput(Mbps)" "scratch/output/1905066_2_throughput.dat" "1905066_mobile_flow_throughput" 2 5
}


# --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

vary_packets_per_second () {

    clear_output

    nNodes=20
    nFlows=50
    nPacketsPerSeconds=(50 100 150 200 250 300 350 400 450 500)
    for packets in ${nPacketsPerSeconds[@]}
    do
        ./ns3 run "scratch/1905066_1.cc --nNodes=$nNodes --nFlows=$nFlows --nPacketsPerSeconds=$packets"
        ./ns3 run "scratch/1905066_2.cc --nNodes=$nNodes --nFlows=$nFlows --nPacketsPerSeconds=$packets"
    done

    # plot delivery ratio vs number of packets per second
    plot_graph "Delivery Ratio vs Number of Packets Per Second(static)" "Number of Packets Per Second" "Delivery Ratio" "scratch/output/1905066_1_delivery_ratio.dat" "1905066_static_packet_delivary_ratio" 3 5
    plot_graph "Delivery Ratio vs Number of Packets Per Second(mobile)" "Number of Packets Per Second" "Delivery Ratio" "scratch/output/1905066_2_delivery_ratio.dat" "1905066_mobile_packet_delivary_ratio" 3 5
    plot_graph "Throughput vs Number of Packets Per Second(static)" "Number of Packets Per Second" "Throughput(Mbps)" "scratch/output/1905066_1_throughput.dat" "1905066_static_packet_throughput" 3 5
    plot_graph "Throughput vs Number of Packets Per Second(mobile)" "Number of Packets Per Second" "Throughput(Mbps)" "scratch/output/1905066_2_throughput.dat" "1905066_mobile_packet_throughput" 3 5
}

# --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

vary_coverage_area () {
    clear_output
    nNodes=20
    nFlows=50
    nPacketsPerSeconds=100
    coverageAreas=(1 2 3 4 5 6 7 8 9 10)
    for area in ${coverageAreas[@]}
    do
        ./ns3 run "scratch/1905066_1.cc --nNodes=$nNodes --nFlows=$nFlows --nPacketsPerSeconds=$nPacketsPerSeconds --coverageArea=$area"
    done

    # plot delivery ratio vs coverage area
    plot_graph "Delivery Ratio vs Coverage Area" "Coverage Area" "Delivery Ratio" "scratch/output/1905066_1_delivery_ratio.dat" "1905066_static_coverage_delivary_ratio" 4 5
    plot_graph "Throughput vs Coverage Area" "Coverage Area" "Throughput(Mbps)" "scratch/output/1905066_1_throughput.dat" "1905066_static_coverage_throughput" 4 5
}

# --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


vary_speed () {
    clear_output
    nNodes=20
    nFlows=50
    nPacketsPerSeconds=100
    speeds=(5 10 15 20 25 30 35 40 45 50)
    for speed in ${speeds[@]}
    do
        ./ns3 run "scratch/1905066_2.cc --nNodes=$nNodes --nFlows=$nFlows --nPacketsPerSeconds=$nPacketsPerSeconds --speed=$speed"
    done

    # plot delivery ratio vs speed
    plot_graph "Delivery Ratio vs Speed" "Speed" "Delivery Ratio" "scratch/output/1905066_2_delivery_ratio.dat" "1905066_mobile_speed_delivary_ratio" 4 5
    plot_graph "Throughput vs Speed" "Speed" "Throughput(Mbps)" "scratch/output/1905066_2_throughput.dat" "1905066_mobile_speed_throughput" 4 5
}

vary_packets_per_second