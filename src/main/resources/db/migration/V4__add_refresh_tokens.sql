-- =================================================================
-- Refresh Tokens Table for JWT Token Rotation
-- =================================================================

-- Create refresh tokens table
CREATE TABLE refresh_tokens (
    id SERIAL PRIMARY KEY,
    token_value VARCHAR(255) UNIQUE NOT NULL,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    replaced_by_token_id INT REFERENCES refresh_tokens(id) ON DELETE SET NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Create index for performance
CREATE INDEX idx_refresh_tokens_token_value ON refresh_tokens(token_value);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- Add trigger to update the updated_at column
CREATE TRIGGER update_refresh_tokens_updated_at 
    BEFORE UPDATE ON refresh_tokens 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Add comment for documentation
COMMENT ON TABLE refresh_tokens IS 'Stores refresh tokens for JWT authentication with token rotation support';
COMMENT ON COLUMN refresh_tokens.token_value IS 'The actual refresh token value (hashed)';
COMMENT ON COLUMN refresh_tokens.replaced_by_token_id IS 'References the new token that replaced this one during rotation';
COMMENT ON COLUMN refresh_tokens.revoked IS 'Whether this token has been revoked for security reasons';
