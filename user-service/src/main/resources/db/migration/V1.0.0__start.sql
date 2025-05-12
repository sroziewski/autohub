-- Create schema for AutoHub User Service
CREATE SCHEMA IF NOT EXISTS autohub AUTHORIZATION autohub_user;

-- Set search path to the schema
SET search_path TO autohub;

-- Users table: Stores core user information
CREATE TABLE users (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       phone VARCHAR(20),
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE,
                       CONSTRAINT uk_users_email UNIQUE (email),
                       CONSTRAINT chk_users_email CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
                       CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED'))
);

-- roles table: Stores role assignments for users
CREATE TABLE roles (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            role VARCHAR(50) NOT NULL,
                            assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT pk_roles UNIQUE (user_id, role),
                            CONSTRAINT fk_roles_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT chk_roles_role CHECK (role IN ('USER', 'SELLER', 'ADMIN', 'MODERATOR'))
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_roles_user_id ON roles(user_id);

-- Insert default admin user (for initial setup)
INSERT INTO users (email, password, phone, status, created_at)
VALUES (
           'admin@autohub.com',
           '$2a$12$8Qz7Xz5Z5Z5Z5Z5Z5Z5Z5u.', -- Example BCrypt hash (replace with actual hash)
           '+1234567890',
           'ACTIVE',
           CURRENT_TIMESTAMP
       );

INSERT INTO roles (user_id, role, assigned_at)
VALUES (
           (SELECT id FROM users WHERE email = 'admin@autohub.com'),
           'ADMIN',
           CURRENT_TIMESTAMP
       );
