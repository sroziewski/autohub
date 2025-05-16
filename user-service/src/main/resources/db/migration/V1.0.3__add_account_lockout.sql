-- Add columns for account lockout functionality
ALTER TABLE autohub.users
    ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN account_locked_until TIMESTAMP;

-- Create index on account_locked_until for faster queries
CREATE INDEX idx_users_account_locked_until ON autohub.users (account_locked_until);

-- Comment on columns
COMMENT ON COLUMN autohub.users.failed_login_attempts IS 'Number of consecutive failed login attempts';
COMMENT ON COLUMN autohub.users.account_locked_until IS 'Timestamp until which the account is locked due to failed login attempts';
