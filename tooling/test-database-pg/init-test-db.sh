#!/bin/bash

psql -c "CREATE ROLE ddluser WITH LOGIN PASSWORD 'ddluser';" -U postgres
psql -c "GRANT ALL PRIVILEGES ON DATABASE gretl TO ddluser;" -U postgres
psql -c "CREATE ROLE dmluser WITH LOGIN PASSWORD 'dmluser';" -U postgres
psql -c "CREATE ROLE readeruser WITH LOGIN PASSWORD 'readeruser';" -U postgres
psql -c 'select version();' -d gretl -U postgres
psql -c 'create extension postgis;' -d gretl -U postgres
psql -c 'create extension "uuid-ossp";' -d gretl -U postgres
psql -c 'select postgis_full_version();' -d gretl -U postgres
psql -c "ALTER DATABASE gretl SET postgis.gdal_enabled_drivers TO 'GTiff PNG JPEG';" -d gretl -U postgres
