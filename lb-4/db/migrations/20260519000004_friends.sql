-- migrate:up
CREATE TABLE friends (
  user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  friend_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, friend_id),
  CHECK (user_id <> friend_id)
);

COMMENT ON TABLE  friends            IS 'Directed friendship edges between users';
COMMENT ON COLUMN friends.user_id    IS 'The user who added the friend';
COMMENT ON COLUMN friends.friend_id  IS 'The user being added as a friend';
COMMENT ON COLUMN friends.created_at IS 'Timestamp when the friendship was established';

GRANT SELECT                 ON friends TO anon;
GRANT SELECT, INSERT, DELETE ON friends TO authenticated;

ALTER TABLE friends ENABLE ROW LEVEL SECURITY;

CREATE POLICY select_all  ON friends FOR SELECT TO anon, authenticated USING (true);
CREATE POLICY auth_insert ON friends FOR INSERT TO authenticated
  WITH CHECK (user_id = current_user_id());
CREATE POLICY auth_delete ON friends FOR DELETE TO authenticated
  USING (user_id = current_user_id());

-- migrate:down
DROP POLICY IF EXISTS auth_delete ON friends;
DROP POLICY IF EXISTS auth_insert ON friends;
DROP POLICY IF EXISTS select_all  ON friends;
ALTER TABLE friends DISABLE ROW LEVEL SECURITY;
REVOKE ALL ON friends FROM anon, authenticated;
DROP TABLE IF EXISTS friends;
