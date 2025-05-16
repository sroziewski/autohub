-- Add indexes for token fields to improve query performance

-- Index for verification token lookup
CREATE INDEX IF NOT EXISTS idx_users_verification_token ON autohub.users(verification_token);

-- Index for reset password token lookup
CREATE INDEX IF NOT EXISTS idx_users_reset_password_token ON autohub.users(reset_password_token);

-- Index for user status to improve filtering by status
CREATE INDEX IF NOT EXISTS idx_users_status ON autohub.users(status);

-- Add comment explaining the purpose of these indexes
COMMENT ON INDEX autohub.idx_users_verification_token IS 'Index for faster verification token lookups during account verification';
COMMENT ON INDEX autohub.idx_users_reset_password_token IS 'Index for faster reset password token lookups during password reset';
COMMENT ON INDEX autohub.idx_users_status IS 'Index for faster filtering of users by status';
