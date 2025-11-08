ALTER TABLE users
    ADD COLUMN profile_picture_url VARCHAR(2048),
    ADD COLUMN is_private BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN receive_notifications BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE user_subscriptions
(
    subscriber_id    BIGINT NOT NULL,
    subscribed_to_id BIGINT NOT NULL,
    PRIMARY KEY (subscriber_id, subscribed_to_id),
    FOREIGN KEY (subscriber_id) REFERENCES users (id),
    FOREIGN KEY (subscribed_to_id) REFERENCES users (id)
);

CREATE TABLE user_blocks
(
    blocker_id BIGINT NOT NULL,
    blocked_id BIGINT NOT NULL,
    PRIMARY KEY (blocker_id, blocked_id),
    FOREIGN KEY (blocker_id) REFERENCES users (id),
    FOREIGN KEY (blocked_id) REFERENCES users (id)
);
