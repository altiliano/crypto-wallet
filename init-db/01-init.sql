-- Initial database setup for crypto wallet
-- This script runs automatically when the database container starts

-- Create database (already done by POSTGRES_DB env var)
-- CREATE DATABASE crypto_wallet;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE crypto_wallet TO crypto_user;

-- Enable logging (optional)
-- ALTER SYSTEM SET log_statement = 'all';

-- You can add any initial data here if needed
-- INSERT INTO ...;
