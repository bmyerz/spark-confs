filebase=$1
#SCALA_HOME=~/escience/spark/scala-2.10.4
PKG=org/apache/spark/examples

mkdir -p target/scala-2.9.3/classes
pushd target/scala-2.9.3/classes
CLASSPATH=`../../../../bin/compute-classpath.sh`
#$SCALA_HOME/bin/scalac -classpath $CLASSPATH ../../../src/main/scala/$GRAPHX_PKG/$filebase.scala
scalac -classpath $CLASSPATH ../../../src/main/scala/$PKG/$filebase.scala

# update the jar file
jar uf ../../spark-examples_2.9.3-0.8.0-incubating.jar $PKG/$filebase*.class
popd

