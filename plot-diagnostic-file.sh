#!/bin/bash

DATA_FILE=/tmp/data.csv
PLOT_FILE=/tmp/gnuplot.plot

cat << EOPLOT > $PLOT_FILE
  set datafile separator ","
  set xdata time
  set timefmt "%s"
  set format x "%m/%d/%Y %H:%M:%S"
  set term dumb

  set key autotitle columnhead

  plot "$DATA_FILE" using 2:1 with linespoints
EOPLOT

rm -f $DATA_FILE
touch $DATA_FILE
echo '"START"','"PHASE"','"DURATION"' > $DATA_FILE

jq -r '.phases[]| { start: .start, phase: ("\"" + .phase + "\""), duration: (.end - .start) }| join(",")' < "$1" >> $DATA_FILE

gnuplot $PLOT_FILE


