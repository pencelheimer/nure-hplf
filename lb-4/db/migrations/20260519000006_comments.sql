-- migrate:up

CREATE TABLE comments (
  id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  post_id    BIGINT      NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
  user_id    BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content    TEXT        NOT NULL CHECK (char_length(content) > 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE  comments            IS 'Comments left on posts';
COMMENT ON COLUMN comments.id         IS 'Auto-generated surrogate key';
COMMENT ON COLUMN comments.post_id    IS 'The commented post; filter with ?post_id=eq.1';
COMMENT ON COLUMN comments.user_id    IS 'The commenter; filter with ?user_id=eq.1';
COMMENT ON COLUMN comments.content    IS 'Comment text';
COMMENT ON COLUMN comments.created_at IS 'Timestamp of the comment';

GRANT SELECT               ON comments TO anon;
GRANT SELECT, INSERT       ON comments TO authenticated;
GRANT UPDATE (content)     ON comments TO authenticated;
GRANT DELETE               ON comments TO authenticated;

ALTER TABLE comments ENABLE ROW LEVEL SECURITY;

CREATE POLICY select_all  ON comments FOR SELECT TO anon, authenticated USING (true);
CREATE POLICY auth_insert ON comments FOR INSERT TO authenticated
  WITH CHECK (user_id = current_user_id());
CREATE POLICY auth_update ON comments FOR UPDATE TO authenticated
  USING (user_id = current_user_id()) WITH CHECK (user_id = current_user_id());
CREATE POLICY auth_delete ON comments FOR DELETE TO authenticated
  USING (user_id = current_user_id());
-- migrate:down

DROP POLICY IF EXISTS auth_delete ON comments;
DROP POLICY IF EXISTS auth_update ON comments;
DROP POLICY IF EXISTS auth_insert ON comments;
DROP POLICY IF EXISTS select_all  ON comments;
ALTER TABLE comments DISABLE ROW LEVEL SECURITY;
REVOKE ALL ON comments FROM anon, authenticated;
DROP TABLE IF EXISTS comments;
