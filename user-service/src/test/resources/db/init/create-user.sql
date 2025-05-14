CREATE ROLE autohub_user WITH LOGIN PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE autohub_test TO autohub_user;
-- Additional permissions as needed
ALTER USER autohub_user WITH SUPERUSER;
