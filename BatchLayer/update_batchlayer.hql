load data inpath '/ywang27/all.csv' overwrite into table ywang27_covid_all;


insert overwrite table ywang27_covid
select * from ywang27_covid_all n
where n.last_update is NOT NULL;
