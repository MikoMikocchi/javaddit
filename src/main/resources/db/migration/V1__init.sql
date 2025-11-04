-- Flyway V1: Initial schema for Reddit-like app (users, communities, posts, comments)
-- Database: PostgreSQL

BEGIN;

-- =============================
-- Utility: updated_at auto-timestamp trigger
-- =============================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
	NEW.updated_at = NOW();
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Recompute post.comments_count on comment mutations
CREATE OR REPLACE FUNCTION update_post_comments_count()
RETURNS trigger AS $$
BEGIN
	IF (TG_OP = 'INSERT') THEN
		UPDATE posts SET comments_count = comments_count + 1 WHERE id = NEW.post_id;
		RETURN NEW;
	ELSIF (TG_OP = 'DELETE') THEN
		UPDATE posts SET comments_count = GREATEST(comments_count - 1, 0) WHERE id = OLD.post_id;
		RETURN OLD;
	ELSIF (TG_OP = 'UPDATE') THEN
		IF NEW.post_id IS DISTINCT FROM OLD.post_id THEN
			UPDATE posts SET comments_count = GREATEST(comments_count - 1, 0) WHERE id = OLD.post_id;
			UPDATE posts SET comments_count = comments_count + 1 WHERE id = NEW.post_id;
		END IF;
		RETURN NEW;
	END IF;
	RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- =============================
-- Users
-- =============================
CREATE TABLE users (
	id            BIGSERIAL PRIMARY KEY,
	username      VARCHAR(30)  NOT NULL,
	email         VARCHAR(320) NOT NULL,
	password_hash TEXT         NOT NULL,
	display_name  VARCHAR(50),
	about         TEXT,
	is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
	deleted_at    TIMESTAMPTZ,
	created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
	updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

	CONSTRAINT users_username_format CHECK (username ~ '^[A-Za-z0-9_]{3,30}$'),
	CONSTRAINT users_email_format CHECK (email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$')
);

-- Case-insensitive uniqueness for username and email
CREATE UNIQUE INDEX users_username_unique_ci ON users (LOWER(username));
CREATE UNIQUE INDEX users_email_unique_ci    ON users (LOWER(email));

CREATE TRIGGER trg_users_set_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =============================
-- Communities
-- =============================
CREATE TABLE communities (
	id          BIGSERIAL PRIMARY KEY,
	name        VARCHAR(50)  NOT NULL,
	title       VARCHAR(100),
	description TEXT,
	is_nsfw     BOOLEAN      NOT NULL DEFAULT FALSE,
	owner_id    BIGINT,
	created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
	updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

	-- enforce lowercase canonical names with allowed charset
	CONSTRAINT communities_name_lower CHECK (name = lower(name)),
	CONSTRAINT communities_name_format CHECK (name ~ '^[a-z0-9_][a-z0-9_-]{2,49}$'),
	CONSTRAINT uq_communities_name UNIQUE (name),
	CONSTRAINT fk_communities_owner FOREIGN KEY (owner_id)
		REFERENCES users(id)
		ON DELETE SET NULL
);

-- Index for queries by owner
CREATE INDEX communities_owner_idx ON communities(owner_id);

CREATE TRIGGER trg_communities_set_updated_at
BEFORE UPDATE ON communities
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =============================
-- Posts
-- =============================
CREATE TABLE posts (
	id              BIGSERIAL PRIMARY KEY,
	community_id    BIGINT      NOT NULL,
	author_id       BIGINT      NOT NULL,
	title           VARCHAR(300) NOT NULL,
	slug            VARCHAR(350) NOT NULL,
	content         TEXT,
	url             TEXT,
	score           INTEGER     NOT NULL DEFAULT 0,
	comments_count  INTEGER     NOT NULL DEFAULT 0,
	created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

	CONSTRAINT fk_posts_community FOREIGN KEY (community_id)
		REFERENCES communities(id)
		ON DELETE CASCADE,
	CONSTRAINT fk_posts_author FOREIGN KEY (author_id)
		REFERENCES users(id)
		ON DELETE RESTRICT,
	CONSTRAINT posts_title_len CHECK (char_length(title) >= 1),
	CONSTRAINT posts_slug_len  CHECK (char_length(slug)  >= 1),
	-- enforce lowercase slugs and allowed charset
	CONSTRAINT posts_slug_lower CHECK (slug = lower(slug)),
	CONSTRAINT posts_slug_format CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
	-- Exactly one of content or url must be provided (XOR), treating empty/whitespace as NULL
	CONSTRAINT posts_content_or_url CHECK (
		((content IS NOT NULL AND length(btrim(content)) > 0) <> (url IS NOT NULL AND length(btrim(url)) > 0))
		AND (
			url IS NULL OR length(btrim(url)) = 0 OR url ~* '^(https?)://'
		)
	)
);

-- Per-community unique slug (case-insensitive)
CREATE UNIQUE INDEX posts_slug_unique_per_community
	ON posts (community_id, LOWER(slug));

-- Optimal feed ordering by community and time
CREATE INDEX posts_community_created_at_idx ON posts (community_id, created_at DESC);
CREATE INDEX posts_author_idx               ON posts (author_id);
-- Global feed by time
CREATE INDEX posts_created_at_idx           ON posts (created_at DESC);

CREATE TRIGGER trg_posts_set_updated_at
BEFORE UPDATE ON posts
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =============================
-- Comments
-- =============================
CREATE TABLE comments (
	id         BIGSERIAL PRIMARY KEY,
	post_id    BIGINT      NOT NULL,
	author_id  BIGINT      NOT NULL,
	parent_id  BIGINT,
	content    TEXT        NOT NULL,
	score      INTEGER     NOT NULL DEFAULT 0,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

	CONSTRAINT fk_comments_post FOREIGN KEY (post_id)
		REFERENCES posts(id)
		ON DELETE CASCADE,
	CONSTRAINT fk_comments_author FOREIGN KEY (author_id)
		REFERENCES users(id)
		ON DELETE RESTRICT,
	CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id)
		REFERENCES comments(id)
		ON DELETE CASCADE,
	CONSTRAINT comments_not_self_parent CHECK (parent_id IS NULL OR parent_id <> id)
);

-- Optimal ordering by post and time
CREATE INDEX comments_post_created_at_idx ON comments (post_id, created_at DESC);
CREATE INDEX comments_parent_idx    ON comments (parent_id);
CREATE INDEX comments_author_idx    ON comments (author_id);


CREATE TRIGGER trg_comments_set_updated_at
BEFORE UPDATE ON comments
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- maintain posts.comments_count automatically
CREATE TRIGGER trg_comments_after_insert
AFTER INSERT ON comments
FOR EACH ROW EXECUTE FUNCTION update_post_comments_count();

CREATE TRIGGER trg_comments_after_delete
AFTER DELETE ON comments
FOR EACH ROW EXECUTE FUNCTION update_post_comments_count();

CREATE TRIGGER trg_comments_after_update
AFTER UPDATE OF post_id ON comments
FOR EACH ROW EXECUTE FUNCTION update_post_comments_count();

COMMIT;

