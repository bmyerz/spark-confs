cp conf/hive-site.template.xml conf/hive-site.xml
sed -i "s/HNAMENODE/$1/g" conf/hive-site.xml
