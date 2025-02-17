-- Unique index on email for active users only
CREATE UNIQUE INDEX unique_email_active_users
    ON user_detail(email)
    WHERE active = true;

-- Unique index on username for active users only
CREATE UNIQUE INDEX unique_username_active_users
    ON user_detail(username)
    WHERE active = true;