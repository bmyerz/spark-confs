master=$1

export SPARK_MASTER_ROOT=`hostname`

pushd $HIVE_HOME
sbin/site_conf.sh $master
popd $HIVE_HOME

pushd $SHARK_HOME
sbin/copy-data-localmode.sh node0406-ib ~/escience/sp2b/bin/sp2b.10gb.str
rm -rf metastore_db
$SHARK_HOME/bin/shark -f $SHARK_HOME/sbin/load_sp2bench.hql
popd
