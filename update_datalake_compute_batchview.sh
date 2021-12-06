cd ../COVID-19/ || exit
git pull
cd ../COVID-19/csse_covid_19_data/csse_covid_19_daily_reports || exit
rm -rf all.csv
cat *.csv > all.csv
hdfs dfs -put all.csv /ywang27
cd ../../../final/ || exit
hive -f ./BatchLayer/update_batchlayer.hql
hive -f ./ServingLayer/compute_batchview.hql
hive -f ./ServingLayer/write_to_hive.hql
