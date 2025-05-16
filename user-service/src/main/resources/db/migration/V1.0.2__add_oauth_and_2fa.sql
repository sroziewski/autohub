-- Add OAuth2 provider columns to users table
ALTER TABLE autohub.users
    ADD COLUMN oauth_provider VARCHAR(50),
    ADD COLUMN oauth_provider_id VARCHAR(255);

-- Create index for OAuth2 provider lookup
CREATE INDEX idx_users_oauth_provider ON autohub.users(oauth_provider, oauth_provider_id);

-- Add two-factor authentication columns to users table
ALTER TABLE autohub.users
    ADD COLUMN two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN two_factor_secret VARCHAR(255),
    ADD COLUMN backup_codes TEXT[];

-- Set default values for existing users
UPDATE autohub.users
SET two_factor_enabled = FALSE,
    backup_codes = '{}';

COMMENT ON COLUMN autohub.users.two_factor_enabled IS 'Flag indicating whether two-factor authentication is enabled for the user';
COMMENT ON COLUMN autohub.users.two_factor_secret IS 'Secret key used for TOTP generation';
COMMENT ON COLUMN autohub.users.backup_codes IS 'Array of backup codes for account recovery';
