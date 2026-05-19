-- migrate:up

CREATE OR REPLACE FUNCTION set_user_id()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  NEW.user_id := current_user_id();
  RETURN NEW;
END;
$$;

CREATE TRIGGER set_user_id BEFORE INSERT ON posts
  FOR EACH ROW EXECUTE FUNCTION set_user_id();

CREATE TRIGGER set_user_id BEFORE INSERT ON comments
  FOR EACH ROW EXECUTE FUNCTION set_user_id();

CREATE TRIGGER set_user_id BEFORE INSERT ON likes
  FOR EACH ROW EXECUTE FUNCTION set_user_id();

CREATE TRIGGER set_user_id BEFORE INSERT ON friends
  FOR EACH ROW EXECUTE FUNCTION set_user_id();

-- migrate:down

DROP TRIGGER IF EXISTS set_user_id ON friends;
DROP TRIGGER IF EXISTS set_user_id ON likes;
DROP TRIGGER IF EXISTS set_user_id ON comments;
DROP TRIGGER IF EXISTS set_user_id ON posts;

DROP FUNCTION IF EXISTS set_user_id();
