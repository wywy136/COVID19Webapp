CREATE EXTERNAL TABLE IF NOT EXISTS `ywang27_covid_all`(
    `fips` string,
    `admin` string,
    `province_state` string,
    `country_region` string,
    `last_update` timestamp,
    `latitude` float,
    `longitude` float,
    `confirmed` int,
    `deaths` int,
    `recovered` int,
    `active` int,
    `combined_key` string,
    `incidence_rate` float,
    `case_fatality_ratio` float
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' NULL DEFINED AS ''
STORED AS TEXTFILE;


CREATE EXTERNAL TABLE IF NOT EXISTS `ywang27_covid`(
    `fips` string,
    `admin` string,
    `province_state` string,
    `country_region` string,
    `last_update` timestamp,
    `latitude` float,
    `longitude` float,
    `confirmed` int,
    `deaths` int,
    `recovered` int,
    `active` int,
    `combined_key` string,
    `incidence_rate` float,
    `case_fatality_ratio` float
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' NULL DEFINED AS ''
STORED AS TEXTFILE;
