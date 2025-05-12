ALTER TABLE autohub.users
    ADD COLUMN first_name VARCHAR(50),
    ADD COLUMN second_name VARCHAR(50),
    ADD COLUMN last_name VARCHAR(50),
    ADD COLUMN birth_date DATE,
    ADD COLUMN last_login_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN verified BOOLEAN DEFAULT FALSE,
    ADD COLUMN verification_token VARCHAR(255),
    ADD COLUMN reset_password_token VARCHAR(255),
    ADD COLUMN reset_password_expires TIMESTAMP WITH TIME ZONE;
