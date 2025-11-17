ALTER SYSTEM SET wal_level = 'logical';
ALTER SYSTEM SET max_replication_slots = 10;
ALTER SYSTEM SET max_wal_senders = 10;

CREATE USER debezium WITH REPLICATION PASSWORD 'mypassword';
GRANT ALL PRIVILEGES ON DATABASE reservation TO debezium;
