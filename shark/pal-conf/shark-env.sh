#!/usr/bin/env bash

# Copyright (C) 2012 The Regents of The University California.
# All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# (Required) Amount of memory used per slave node. This should be in the same
# format as the JVM's -Xmx option, e.g. 300m or 1g.
export SPARK_MEM=32g

# (Required) Set the master program's memory
export SHARK_MASTER_MEM=4g

# (Required) Point to your Scala installation.
export SCALA_HOME="/people/bdmyers/escience/spark/scala-2.9.3"

# (Required) Point to the patched Hive binary distribution
export HIVE_HOME="/people/bdmyers/escience/spark/hive-0.9.0-bin"

# (Optional) Specify the location of Hive's configuration directory. By default,
# it points to $HIVE_HOME/conf
#export HIVE_CONF_DIR="$HIVE_HOME/conf"

# For running Shark in distributed mode, set the following:
#export HADOOP_HOME="/scratch/hadoop-0.20.2-cdh3u2"
export HADOOP_HOME="/scratch/hadoop-1.2.1"
export SPARK_HOME="/people/bdmyers/escience/spark/spark-0.8.1-incubating-bin-hadoop1"
export MASTER="spark://$SPARK_MASTER_HOST:7077"
# Only required if using Mesos:
#export MESOS_NATIVE_LIBRARY=/usr/local/lib/libmesos.so 

# Only required if run shark with spark on yarn
#export SHARK_EXEC_MODE=yarn
#export SPARK_ASSEMBLY_JAR=
#export SHARK_ASSEMBLY_JAR=

# (Optional) Extra classpath
#export SPARK_LIBRARY_PATH=""

# Java options
# On EC2, change the local.dir to /mnt/tmp
SPARK_JAVA_OPTS="-Dspark.local.dir=/tmp "
SPARK_JAVA_OPTS+="-Dspark.kryoserializer.buffer.mb=10 "
SPARK_JAVA_OPTS+="-verbose:gc -XX:-PrintGCDetails -XX:+PrintGCTimeStamps "
export SPARK_JAVA_OPTS

source $SPARK_HOME/conf/spark-env.sh

# to get hive-site.xml
CLASSPATH+=$HIVE_HOME/conf
export CLASSPATH
