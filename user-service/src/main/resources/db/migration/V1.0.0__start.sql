-- Create schema for AutoHub User Service
CREATE SCHEMA IF NOT EXISTS autohub AUTHORIZATION autohub_user;

-- Set search path to the schema
SET search_path TO autohub;

-- Users table: Stores core user information
CREATE TABLE users (
                       id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       phone VARCHAR(20),
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE,
                       first_name VARCHAR(50),
                       second_name VARCHAR(50),
                       last_name VARCHAR(50),
                       birth_date DATE,
                       last_login_at TIMESTAMP WITH TIME ZONE,
                       verified BOOLEAN DEFAULT FALSE,
                       verification_token VARCHAR(255),
                       reset_password_token VARCHAR(255),
                       reset_password_expires TIMESTAMP WITH TIME ZONE,
                       CONSTRAINT uk_users_email UNIQUE (email),
                       CONSTRAINT chk_users_email CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
                       CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED'))
);

-- roles table: Stores role assignments for users
CREATE TABLE roles (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            user_id UUID NOT NULL,
                            role VARCHAR(50) NOT NULL,
                            assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT pk_roles UNIQUE (user_id, role),
                            CONSTRAINT fk_roles_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT chk_roles_role CHECK (role IN ('USER', 'SELLER', 'ADMIN', 'MODERATOR'))
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_roles_user_id ON roles(user_id);

CREATE TABLE regions
(
    id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(20) UNIQUE
);

CREATE TABLE cities
(
    id                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    region_id           INTEGER      NOT NULL,
    name                VARCHAR(100) NOT NULL,
    postal_code_pattern VARCHAR(100),
    CONSTRAINT fk_cities_region FOREIGN KEY (region_id) REFERENCES regions (id) ON DELETE CASCADE,
    CONSTRAINT uk_cities_region_name UNIQUE (region_id, name)
);

CREATE TABLE addresses
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      UUID                   NOT NULL,
    address_type VARCHAR(20)              NOT NULL,
    company_name VARCHAR(255),
    street       VARCHAR(255)             NOT NULL,
    city_id      INTEGER                  NOT NULL,
    region_id    INTEGER                  NOT NULL,
    postal_code  VARCHAR(20)              NOT NULL,
    is_default   BOOLEAN                           DEFAULT FALSE,
    coordinates  POINT,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES autohub.users (id) ON DELETE CASCADE,
    CONSTRAINT fk_addresses_city FOREIGN KEY (city_id) REFERENCES cities (id),
    CONSTRAINT fk_addresses_region FOREIGN KEY (region_id) REFERENCES regions (id),
    CONSTRAINT chk_addresses_type CHECK (address_type IN ('PERSONAL', 'COMPANY')),
    CONSTRAINT chk_company_name CHECK (
        (address_type = 'COMPANY' AND company_name IS NOT NULL) OR
        (address_type = 'PERSONAL' AND company_name IS NULL)
        )
);

-- Index for faster lookups
CREATE INDEX idx_addresses_user_id ON addresses (user_id);
CREATE INDEX idx_addresses_city_id ON addresses (city_id);
CREATE INDEX idx_addresses_region_id ON addresses (region_id);
CREATE UNIQUE INDEX idx_unique_default_address_per_user
    ON addresses (user_id)
    WHERE is_default = TRUE;


