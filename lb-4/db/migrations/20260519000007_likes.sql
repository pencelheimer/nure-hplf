-- migrate:up
CREATE TABLE likes (
  id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  post_id    BIGINT      NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
  user_id    BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (post_id, user_id)
);

COMMENT ON TABLE  likes            IS 'Post likes; each user may like a post exactly once';
COMMENT ON COLUMN likes.id         IS 'Auto-generated surrogate key';
COMMENT ON COLUMN likes.post_id    IS 'The liked post';
COMMENT ON COLUMN likes.user_id    IS 'The user who liked the post';
COMMENT ON COLUMN likes.created_at IS 'Timestamp of the like';

GRANT SELECT                 ON likes TO anon;
GRANT SELECT, INSERT, DELETE ON likes TO authenticated;

GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO authenticated;

ALTER TABLE likes ENABLE ROW LEVEL SECURITY;

CREATE POLICY select_all  ON likes FOR SELECT TO anon, authenticated USING (true);
CREATE POLICY auth_insert ON likes FOR INSERT TO authenticated
  WITH CHECK (user_id = current_user_id());
CREATE POLICY auth_delete ON likes FOR DELETE TO authenticated
  USING (user_id = current_user_id());

-- migrate:down
DROP POLICY IF EXISTS auth_delete ON likes;
DROP POLICY IF EXISTS auth_insert ON likes;
DROP POLICY IF EXISTS select_all  ON likes;
ALTER TABLE likes DISABLE ROW LEVEL SECURITY;
REVOKE USAGE ON ALL SEQUENCES IN SCHEMA public FROM authenticated;
REVOKE ALL ON likes FROM anon, authenticated;
DROP TABLE IF EXISTS likes;
