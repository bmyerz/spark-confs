drop table sp2bench_str;
create table sp2bench_str(s string, p string, o string);
load data inpath '/user/bdmyers/sp2b.10gb.str' into table sp2bench_str;
