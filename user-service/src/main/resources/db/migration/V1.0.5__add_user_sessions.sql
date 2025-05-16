-- Create user_sessions table for session management
CREATE TABLE autohub.user_sessions (
    id UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(512),
    device_info VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    
    CONSTRAINT fk_user_sessions_user_id FOREIGN KEY (user_id) REFERENCES autohub.users(id) ON DELETE CASCADE
);

-- Create indexes for faster lookups
CREATE INDEX idx_user_sessions_user_id ON autohub.user_sessions(user_id);
CREATE INDEX idx_user_sessions_active ON autohub.user_sessions(active);
CREATE INDEX idx_user_sessions_expires_at ON autohub.user_sessions(expires_at);

-- Add comment to the table
COMMENT ON TABLE autohub.user_sessions IS 'Stores user session information for tracking and management';
