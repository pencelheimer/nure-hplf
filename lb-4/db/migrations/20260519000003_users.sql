-- migrate:up
CREATE TABLE users (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  username      TEXT NOT NULL UNIQUE,
  email         TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  role          TEXT NOT NULL DEFAULT 'user' CHECK (role IN ('user', 'admin')),
  bio           TEXT,
  avatar_url    TEXT,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE  users               IS 'Registered users of the social network';
COMMENT ON COLUMN users.id            IS 'Auto-generated surrogate key';
COMMENT ON COLUMN users.username      IS 'Unique display name; filter with ?username=eq.alice';
COMMENT ON COLUMN users.email         IS 'Unique email address';
COMMENT ON COLUMN users.password_hash IS 'bcrypt hash (pgcrypto crypt + gen_salt); excluded from API responses';
COMMENT ON COLUMN users.role          IS 'Application role: "user" (default) or "admin"; controls access to promote()';
COMMENT ON COLUMN users.bio           IS 'Optional short biography';
COMMENT ON COLUMN users.avatar_url    IS 'URL of the profile picture';
COMMENT ON COLUMN users.created_at    IS 'Account creation timestamp';

-- NOTE(pencelheimer): password_hash is excluded
GRANT SELECT (id, username, role, email, bio, avatar_url, created_at) ON users TO anon;
GRANT SELECT (id, username, role, email, bio, avatar_url, created_at) ON users TO authenticated;
GRANT UPDATE (username, email, bio, avatar_url)                       ON users TO authenticated;
GRANT DELETE                                                          ON users TO authenticated;

ALTER TABLE users ENABLE ROW LEVEL SECURITY;

CREATE POLICY select_all  ON users FOR SELECT TO anon, authenticated USING (true);
CREATE POLICY auth_update ON users FOR UPDATE TO authenticated
  USING (id = current_user_id()) WITH CHECK (id = current_user_id());
CREATE POLICY auth_delete ON users FOR DELETE TO authenticated
  USING (id = current_user_id());

-- migrate:down
DROP POLICY IF EXISTS auth_delete ON users;
DROP POLICY IF EXISTS auth_update ON users;
DROP POLICY IF EXISTS select_all  ON users;
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
REVOKE ALL ON users FROM anon, authenticated;
DROP TABLE IF EXISTS users;
