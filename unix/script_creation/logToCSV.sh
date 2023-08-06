#!/usr/bin/env bash
# This program should take a fileIn as the first parameter:
# It takes the input log file that has the same format as the `access_log` file and maps it to a CSV format.
# The CSV format is:
# Client,Time,Type,Path,Status,Size
#
# The program should not create a CSV file.
# This can be done by piping the output to a file.
# Example: `./logToCSV access_log > output.csv`
# It could take some time to convert all of the `access_log` file contents. Consider using a small subset for testing.
grep ""<$1 | cut -f1,4,6,7,9,10 -d' '| awk '{print $1,substr($2,2,length($2)-1),$3,$4,$5,$6}'|tr -d '"'|tr ' ' ',' 