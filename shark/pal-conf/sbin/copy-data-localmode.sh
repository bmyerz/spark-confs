#!/bin/bash

namenode=$1
filen=$2

fspath=hdfs://$namenode:54310

export HADOOP_HOME=/scratch/hadoop-1.2.1
#$HADOOP_HOME/bin/hadoop fs -ls $fspath/user
$HADOOP_HOME/bin/hadoop fs -mkdir $fspath/user/bdmyers
filebasename=`basename $filen`
echo "copying to $fspath/user/bdmyers/$filebasename"
$HADOOP_HOME/bin/hadoop fs -copyFromLocal $filen $fspath/user/bdmyers
