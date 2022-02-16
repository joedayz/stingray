-- noinspection SqlNoDataSourceInspectionForFile

-- stingraydb
CREATE USER jamesbond WITH PASSWORD 's3cr3t';
CREATE DATABASE stingraydb;
GRANT ALL PRIVILEGES ON DATABASE stingraydb TO jamesbond;
ALTER ROLE jamesbond SUPERUSER;
