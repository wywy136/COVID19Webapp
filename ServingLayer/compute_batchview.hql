DROP TABLE IF EXISTS ywang27_country_date;
CREATE TABLE ywang27_country_date AS
SELECT DISTINCT
    country_region,
    if(province_state is not NULL, province_state, country_region) as province_state,
    to_date(last_update) as update_date,
    confirmed as confirmed,
    deaths as deaths,
    recovered as recovered,
    active as active
FROM ywang27_covid
WHERE to_date(last_update) is not NULL
ORDER BY country_region, update_date;


INSERT OVERWRITE TABLE ywang27_country_date
SELECT country_region,
       province_state,
       update_date,
       coalesce(sum(confirmed), 0) as confirmed,
       coalesce(sum(deaths), 0) as deaths,
       coalesce(sum(recovered), 0) as recovered,
       coalesce(sum(active), 0) as active
FROM ywang27_country_date
GROUP BY province_state, country_region, update_date
ORDER BY country_region, update_date;
