CREATE EXTERNAL TABLE IF NOT EXISTS ywang27_country_date_for_hbase (
    country_state_date string,
    confirmed bigint,
    deaths bigint,
    recovered bigint,
    active bigint
)
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
WITH SERDEPROPERTIES ('hbase.columns.mapping' =
    ':key,data:confirmed,data:deaths,data:recovered,data:active')
TBLPROPERTIES ('hbase.table.name' = 'ywang27_country_state_date');


INSERT OVERWRITE TABLE ywang27_country_date_for_hbase
SELECT concat(country_region, province_state, update_date),
       confirmed,
       deaths,
       recovered,
       active
FROM ywang27_country_date;
