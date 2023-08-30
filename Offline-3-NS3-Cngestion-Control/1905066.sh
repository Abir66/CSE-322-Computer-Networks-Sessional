# ./ns3 run "scratch/1905066/1905066.cc"

# parameters - title, xlabel, ylabel, input file1, output file, x column, y column1, y tick1, input file2, y column2, y tick2
plot_graph () {
    gnuCommand="set terminal pngcairo size 1280,768 enhanced font 'Verdana,12'; 
                set output '$5.png'; 
                set title '$1' font 'Verdana,16'; 
                set xlabel '$2' font 'Verdana,14'; 
                set ylabel '$3' font 'Verdana,14'; 
                set grid;
                set style line 1 lc rgb '#0060ad' lt 1 lw 2 pt 7 ps 1.5;
                set style line 2 lc rgb '#e63946' lt 1 lw 2 pt 9 ps 1.5; 
                set border 3; 
                set xtics nomirror; 
                set ytics nomirror;
                set lmargin 12;
                set rmargin 6;
                set tmargin 6;
                set bmargin 6;
                plot '$4' using $6:$7  title '$8' with linespoints linestyle 1"


    if [[ $# -gt 8 ]]
    then
        gnuCommand=$gnuCommand", '$9' using $6:${10} title '${11}' with linespoints linestyle 2;"
    else
        gnuCommand=$gnuCommand";"
    fi

    if [[ $# -gt 11 ]]
    then

        # replace linespoints with lines for the legend
        gnuCommand=${gnuCommand//linespoints/lines}

    fi 

    gnuCommand=$gnuCommand" exit;"


    
    echo $gnuCommand | gnuplot
}



cngw_output_folder="../output"
mkdir -p $cngw_output_folder

vary_data_rate(){
    output_file="$cngw_output_folder/output.txt"
    > $output_file


    bottle_neck_data_rates=(1 20 40 60 80 100 120 140 160 180 200 220 240 260 280 300)
    packet_loss_rate=-6
    #loop over all the data rates
    for data_rate in ${bottle_neck_data_rates[@]}
    do

        data_rate=$data_rate'Mbps'
        ./ns3 run "scratch/1905066/1905066.cc --bottleneck_dataRate=$data_rate --packet_loss_rate=$packet_loss_rate --congestion_output_folder=$cngw_output_folder --output_file=$output_file --trace_cwnd=false --algo1=$1 --algo2=$2"    
        echo "Done with data rate: $data_rate"

    done

    plot_graph "Throughput Vs Bottleneck Data Rate" "Bottleneck Data Rate" "Throughput(kbps)" "../output/output.txt" "../output/Throughput-vs-DataRate-$1-$2" 1 3 "$1" "../output/output.txt" 4 "$2"
    plot_graph "Fairness Index Vs Bottleneck Data Rate" "Bottleneck Data Rate" "Fairness Index" "../output/output.txt" "../output/Fair-Index-vs-DataRate-$1-$2" 1 5 "Fairness Index"

}


vary_packet_loss_rate(){
    output_file="$cngw_output_folder/output.txt"
    > $output_file


    data_rate='50Mbps'
    packet_loss_rates=(-2 -2.5 -3 -3.5 -4 -4.5 -5 -5.5 -6)
    
    for packet_loss_rate in ${packet_loss_rates[@]}
    do
        ./ns3 run "scratch/1905066/1905066.cc --bottleneck_dataRate=$data_rate --packet_loss_rate=$packet_loss_rate --congestion_output_folder=$cngw_output_folder --output_file=$output_file --trace_cwnd=false --algo1=$1 --algo2=$2"    
        echo "done with packet loss rate: $packet_loss_rate"
    done


    plot_graph "Throughput Vs Packet Loss Rate" "Packet Loss Rate" "Throughput(kbps)" "../output/output.txt" "../output/Throughput-vs-packet-$1-$2" 2 3 "$1" "../output/output.txt" 4 "$2"
    plot_graph "Fairness Index Vs Packet Loss Rate" "Packet Loss Rate" "Fairness Index" "../output/output.txt" "../output/Fair-Index-vs-packet-$1-$2" 2 5 "Fairness Index"

}

congestion_window(){
    output_file="$cngw_output_folder/output.txt"
    input_file_1="$cngw_output_folder/$1.cwnd"
    input_file_2="$cngw_output_folder/$2.cwnd"

    > $output_file
    data_rate='1Mbps'
    packet_loss_rate=-6

    ./ns3 run "scratch/1905066/1905066.cc --bottleneck_dataRate=$data_rate --packet_loss_rate=$packet_loss_rate --congestion_output_folder=$cngw_output_folder --output_file=$output_file --trace_cwnd=true --algo1=$1 --algo2=$2"
    echo "okay"
    plot_graph "Congestion window Vs Time" "Time" "Congestion window" "$input_file_1" "../output/Congestion-$1-$2" 1 2 "$1" "$input_file_2" 2 "$2" "hehe" 
}


vary_data_rate "TcpNewReno" "TcpHighSpeed"
vary_packet_loss_rate "TcpNewReno" "TcpHighSpeed"
congestion_window "TcpNewReno" "TcpHighSpeed"

vary_data_rate "TcpNewReno" "TcpAdaptiveReno"
vary_packet_loss_rate "TcpNewReno" "TcpAdaptiveReno"
congestion_window "TcpNewReno" "TcpAdaptiveReno"


vary_data_rate "TcpNewReno" "TcpWestwoodPlus"
vary_packet_loss_rate "TcpNewReno" "TcpWestwoodPlus"
congestion_window "TcpNewReno" "TcpWestwoodPlus"