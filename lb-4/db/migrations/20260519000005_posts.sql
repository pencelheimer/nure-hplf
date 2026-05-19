-- migrate:up
CREATE TABLE posts (
  id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  title      TEXT NOT NULL CHECK (char_length(title)   > 0),
  content    TEXT NOT NULL CHECK (char_length(content) > 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE  posts            IS 'User-authored posts';
COMMENT ON COLUMN posts.id         IS 'Auto-generated surrogate key';
COMMENT ON COLUMN posts.user_id    IS 'Author; filter with ?user_id=eq.1';
COMMENT ON COLUMN posts.title      IS 'Post title; filter with ?title=ilike.*keyword*';
COMMENT ON COLUMN posts.content    IS 'Post body; filter with ?content=ilike.*keyword*';
COMMENT ON COLUMN posts.created_at IS 'Creation timestamp; range filter with ?created_at=gte.2024-01-01';
COMMENT ON COLUMN posts.updated_at IS 'Last edit timestamp; auto-updated by trigger on every PATCH';

CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN NEW.updated_at = now(); RETURN NEW; END;
$$;

COMMENT ON FUNCTION set_updated_at() IS 'Trigger function: sets updated_at = now() before every UPDATE.';

CREATE TRIGGER posts_updated_at BEFORE UPDATE ON posts
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

GRANT SELECT                  ON posts TO anon;
GRANT SELECT, INSERT          ON posts TO authenticated;
GRANT UPDATE (title, content) ON posts TO authenticated;
GRANT DELETE                  ON posts TO authenticated;

ALTER TABLE posts ENABLE ROW LEVEL SECURITY;

CREATE POLICY select_all  ON posts FOR SELECT TO anon, authenticated USING (true);
CREATE POLICY auth_insert ON posts FOR INSERT TO authenticated
  WITH CHECK (user_id = current_user_id());
CREATE POLICY auth_update ON posts FOR UPDATE TO authenticated
  USING (user_id = current_user_id()) WITH CHECK (user_id = current_user_id());
CREATE POLICY auth_delete ON posts FOR DELETE TO authenticated
  USING (user_id = current_user_id());

-- migrate:down
DROP POLICY IF EXISTS auth_delete ON posts;
DROP POLICY IF EXISTS auth_update ON posts;
DROP POLICY IF EXISTS auth_insert ON posts;
DROP POLICY IF EXISTS select_all  ON posts;
ALTER TABLE posts DISABLE ROW LEVEL SECURITY;
REVOKE ALL ON posts FROM anon, authenticated;
DROP TRIGGER  IF EXISTS posts_updated_at ON posts;
DROP FUNCTION IF EXISTS set_updated_at();
DROP TABLE    IF EXISTS posts;
