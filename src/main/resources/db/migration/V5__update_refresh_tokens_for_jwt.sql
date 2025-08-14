-- =================================================================
-- Update Refresh Tokens Table for JWT Implementation
-- =================================================================

-- Rename token_value column to jti (JWT ID) since we'll store the JTI claim instead of hash
ALTER TABLE refresh_tokens RENAME COLUMN token_value TO jti;

-- Update the column type and constraint since JTI is shorter than a hash
ALTER TABLE refresh_tokens ALTER COLUMN jti TYPE VARCHAR(36);

-- Update index name to reflect the change
DROP INDEX IF EXISTS idx_refresh_tokens_token_value;
CREATE INDEX idx_refresh_tokens_jti ON refresh_tokens(jti);

-- Update comments for documentation
COMMENT ON COLUMN refresh_tokens.jti IS 'The JWT ID (jti) claim from the refresh token JWT';
COMMENT ON TABLE refresh_tokens IS 'Stores refresh token JTI claims for JWT authentication with token rotation support';
