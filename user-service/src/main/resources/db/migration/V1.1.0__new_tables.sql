CREATE TABLE regions (
                         id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         name VARCHAR(100) NOT NULL UNIQUE,
                         code VARCHAR(20) UNIQUE
);

CREATE TABLE cities (
                        id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        region_id INTEGER NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        postal_code_pattern VARCHAR(100),
                        CONSTRAINT fk_cities_region FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE,
                        CONSTRAINT uk_cities_region_name UNIQUE (region_id, name)
);

CREATE TABLE addresses (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       user_id BIGINT NOT NULL,
                       address_type VARCHAR(20) NOT NULL,
                       company_name VARCHAR(255),
                       street_line1 VARCHAR(255) NOT NULL,
                       street_line2 VARCHAR(255),
                       city_id INTEGER NOT NULL,
                       region_id INTEGER NOT NULL,
                       postal_code VARCHAR(20) NOT NULL,
                       is_default BOOLEAN DEFAULT FALSE,
                       coordinates POINT,
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE,

                       CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES autohub.users(id) ON DELETE CASCADE,
                       CONSTRAINT fk_addresses_city FOREIGN KEY (city_id) REFERENCES cities(id),
                       CONSTRAINT fk_addresses_region FOREIGN KEY (region_id) REFERENCES regions(id),
                       CONSTRAINT chk_addresses_type CHECK (address_type IN ('PERSONAL', 'COMPANY')),
                       CONSTRAINT chk_company_name CHECK (
                           (address_type = 'COMPANY' AND company_name IS NOT NULL) OR
                           (address_type = 'PERSONAL' AND company_name IS NULL)
                           ),

                       -- Ensures only one default address per user
                       CONSTRAINT uk_addresses_user_default UNIQUE (user_id, is_default),
                       CONSTRAINT chk_addresses_default_only_one CHECK (
                           NOT is_default OR
                           (is_default AND (SELECT COUNT(*) FROM addresses a2
                           WHERE a2.user_id = user_id AND a2.is_default = TRUE) = 1)
                       )
);

-- Index for faster lookups
CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_addresses_city_id ON addresses(city_id);
CREATE INDEX idx_addresses_region_id ON addresses(region_id);
