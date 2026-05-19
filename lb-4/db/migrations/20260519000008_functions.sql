-- migrate:up
CREATE OR REPLACE FUNCTION register(p_username text, p_email text, p_password text)
RETURNS json LANGUAGE plpgsql SECURITY DEFINER AS $$
DECLARE
  _id bigint;
BEGIN
  INSERT INTO users (username, email, password_hash)
  VALUES (p_username, p_email, extensions.crypt(p_password, extensions.gen_salt('bf')))
  RETURNING id INTO _id;

  RETURN json_build_object(
    'token', extensions.sign(
      json_build_object(
        'role',      'authenticated',
        'user_id',   _id,
        'user_role', 'user',
        'exp',       extract(epoch FROM now() + interval '24 hours')::integer
      ),
      current_setting('app.jwt_secret')
    )
  );
END;
$$;

COMMENT ON FUNCTION register(text, text, text) IS
  'Registers a new user, hashes the password with bcrypt, and returns a signed JWT. '
  'Callable by anon via POST /rpc/register. '
  'JWT payload: { role: "authenticated", user_id, user_role, exp }.';

CREATE OR REPLACE FUNCTION login(p_username text, p_password text)
RETURNS json LANGUAGE plpgsql SECURITY DEFINER AS $$
DECLARE
  _id        bigint;
  _hash      text;
  _user_role text;
BEGIN
  SELECT id, password_hash, role INTO _id, _hash, _user_role
  FROM users
  WHERE username = p_username;

  IF _id IS NULL OR _hash <> extensions.crypt(p_password, _hash) THEN
    RAISE EXCEPTION 'invalid_credentials'
      USING HINT = 'Wrong username or password', ERRCODE = 'P0001';
  END IF;

  RETURN json_build_object(
    'token', extensions.sign(
      json_build_object(
        'role',      'authenticated',
        'user_id',   _id,
        'user_role', _user_role,
        'exp',       extract(epoch FROM now() + interval '24 hours')::integer
      ),
      current_setting('app.jwt_secret')
    )
  );
END;
$$;

COMMENT ON FUNCTION login(text, text) IS
  'Verifies username + bcrypt password and returns a signed JWT valid for 24 hours. '
  'Callable by anon via POST /rpc/login. '
  'Raises P0001 for both bad username and bad password to prevent user enumeration.';

CREATE OR REPLACE FUNCTION promote(p_user_id bigint, p_new_role text)
RETURNS void LANGUAGE plpgsql SECURITY DEFINER AS $$
BEGIN
  IF (current_setting('request.jwt.claims', true)::jsonb->>'user_role') <> 'admin' THEN
    RAISE EXCEPTION 'permission_denied'
      USING HINT = 'Only admin users can change roles', ERRCODE = 'P0001';
  END IF;

  IF p_new_role NOT IN ('user', 'admin') THEN
    RAISE EXCEPTION 'invalid_role'
      USING HINT = 'Role must be "user" or "admin"', ERRCODE = 'P0001';
  END IF;

  UPDATE users SET role = p_new_role WHERE id = p_user_id;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'user_not_found'
      USING HINT = 'No user with the given id', ERRCODE = 'P0001';
  END IF;
END;
$$;

COMMENT ON FUNCTION promote(bigint, text) IS
  'Changes the application role of the target user to "user" or "admin". '
  'Callable by authenticated users via POST /rpc/promote, but raises P0001 '
  'unless the caller''s JWT contains user_role = "admin". '
  'The caller must re-login after being promoted for the new role to appear in their JWT.';

GRANT EXECUTE ON FUNCTION register(text, text, text) TO anon;
GRANT EXECUTE ON FUNCTION login(text, text)          TO anon;
GRANT EXECUTE ON FUNCTION promote(bigint, text)      TO authenticated;

-- migrate:down
REVOKE EXECUTE ON FUNCTION register(text, text, text) FROM anon;
REVOKE EXECUTE ON FUNCTION login(text, text)          FROM anon;
REVOKE EXECUTE ON FUNCTION promote(bigint, text)      FROM authenticated;

DROP FUNCTION IF EXISTS promote(bigint, text);
DROP FUNCTION IF EXISTS login(text, text);
DROP FUNCTION IF EXISTS register(text, text, text);
