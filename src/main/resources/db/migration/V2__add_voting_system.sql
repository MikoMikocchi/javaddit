-- Flyway V2: Add voting system for posts and comments
-- Database: PostgreSQL

BEGIN;

-- =============================
-- Votes Table
-- =============================
CREATE TABLE votes (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    post_id     BIGINT,
    comment_id  BIGINT,
    vote_type   VARCHAR(10) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_votes_user FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_votes_post FOREIGN KEY (post_id)
        REFERENCES posts(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_votes_comment FOREIGN KEY (comment_id)
        REFERENCES comments(id)
        ON DELETE CASCADE,

    -- Vote type must be UPVOTE or DOWNVOTE
    CONSTRAINT votes_type_check CHECK (vote_type IN ('UPVOTE', 'DOWNVOTE')),

    -- XOR constraint: exactly one of post_id or comment_id must be set
    CONSTRAINT votes_target_xor CHECK (
        (post_id IS NOT NULL AND comment_id IS NULL) OR
        (post_id IS NULL AND comment_id IS NOT NULL)
    )
);

-- =============================
-- Unique Constraints
-- =============================
-- One vote per user per post (unique when comment_id is NULL)
CREATE UNIQUE INDEX votes_user_post_unique
    ON votes (user_id, post_id)
    WHERE comment_id IS NULL;

-- One vote per user per comment (unique when post_id is NULL)
CREATE UNIQUE INDEX votes_user_comment_unique
    ON votes (user_id, comment_id)
    WHERE post_id IS NULL;

-- =============================
-- Performance Indexes
-- =============================
CREATE INDEX votes_post_id_idx    ON votes(post_id) WHERE post_id IS NOT NULL;
CREATE INDEX votes_comment_id_idx ON votes(comment_id) WHERE comment_id IS NOT NULL;
CREATE INDEX votes_user_id_idx    ON votes(user_id);

-- =============================
-- Trigger: Auto-update updated_at
-- =============================
CREATE TRIGGER trg_votes_set_updated_at
BEFORE UPDATE ON votes
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =============================
-- Function: Recalculate Post Score
-- =============================
CREATE OR REPLACE FUNCTION update_post_score()
RETURNS TRIGGER AS $$
DECLARE
    target_post_id BIGINT;
BEGIN
    -- Determine which post_id to update
    IF (TG_OP = 'DELETE') THEN
        target_post_id := OLD.post_id;
    ELSE
        target_post_id := NEW.post_id;
    END IF;

    -- Skip if this vote is for a comment, not a post
    IF target_post_id IS NULL THEN
        RETURN COALESCE(NEW, OLD);
    END IF;

    -- Recalculate score from all votes for this post
    UPDATE posts
    SET score = COALESCE((
        SELECT SUM(
            CASE
                WHEN vote_type = 'UPVOTE' THEN 1
                WHEN vote_type = 'DOWNVOTE' THEN -1
                ELSE 0
            END
        )
        FROM votes
        WHERE post_id = target_post_id
    ), 0)
    WHERE id = target_post_id;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- =============================
-- Function: Recalculate Comment Score
-- =============================
CREATE OR REPLACE FUNCTION update_comment_score()
RETURNS TRIGGER AS $$
DECLARE
    target_comment_id BIGINT;
BEGIN
    -- Determine which comment_id to update
    IF (TG_OP = 'DELETE') THEN
        target_comment_id := OLD.comment_id;
    ELSE
        target_comment_id := NEW.comment_id;
    END IF;

    -- Skip if this vote is for a post, not a comment
    IF target_comment_id IS NULL THEN
        RETURN COALESCE(NEW, OLD);
    END IF;

    -- Recalculate score from all votes for this comment
    UPDATE comments
    SET score = COALESCE((
        SELECT SUM(
            CASE
                WHEN vote_type = 'UPVOTE' THEN 1
                WHEN vote_type = 'DOWNVOTE' THEN -1
                ELSE 0
            END
        )
        FROM votes
        WHERE comment_id = target_comment_id
    ), 0)
    WHERE id = target_comment_id;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- =============================
-- Triggers: Auto-update Post Score
-- =============================
CREATE TRIGGER trg_votes_post_after_insert
AFTER INSERT ON votes
FOR EACH ROW
WHEN (NEW.post_id IS NOT NULL)
EXECUTE FUNCTION update_post_score();

CREATE TRIGGER trg_votes_post_after_update
AFTER UPDATE ON votes
FOR EACH ROW
WHEN (NEW.post_id IS NOT NULL OR OLD.post_id IS NOT NULL)
EXECUTE FUNCTION update_post_score();

CREATE TRIGGER trg_votes_post_after_delete
AFTER DELETE ON votes
FOR EACH ROW
WHEN (OLD.post_id IS NOT NULL)
EXECUTE FUNCTION update_post_score();

-- =============================
-- Triggers: Auto-update Comment Score
-- =============================
CREATE TRIGGER trg_votes_comment_after_insert
AFTER INSERT ON votes
FOR EACH ROW
WHEN (NEW.comment_id IS NOT NULL)
EXECUTE FUNCTION update_comment_score();

CREATE TRIGGER trg_votes_comment_after_update
AFTER UPDATE ON votes
FOR EACH ROW
WHEN (NEW.comment_id IS NOT NULL OR OLD.comment_id IS NOT NULL)
EXECUTE FUNCTION update_comment_score();

CREATE TRIGGER trg_votes_comment_after_delete
AFTER DELETE ON votes
FOR EACH ROW
WHEN (OLD.comment_id IS NOT NULL)
EXECUTE FUNCTION update_comment_score();

COMMIT;
