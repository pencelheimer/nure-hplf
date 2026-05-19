-- migrate:up
CREATE ROLE anon          NOLOGIN;
CREATE ROLE authenticated NOLOGIN;

CREATE ROLE authenticator LOGIN NOINHERIT PASSWORD 'authenticator';

GRANT anon          TO authenticator;
GRANT authenticated TO authenticator;

GRANT USAGE ON SCHEMA public TO anon, authenticated;

CREATE OR REPLACE FUNCTION current_user_id() RETURNS bigint
  LANGUAGE sql STABLE SECURITY DEFINER AS $$
    SELECT nullif(
      current_setting('request.jwt.claims', true)::jsonb->>'user_id', ''
    )::bigint
  $$;

COMMENT ON FUNCTION current_user_id() IS
  'Extracts user_id from the JWT claims set by PostgREST. '
  'Returns NULL for anonymous (unauthenticated) requests. '
  'Used in RLS policies to scope writes to the owning user.';

GRANT EXECUTE ON FUNCTION current_user_id() TO authenticated;

-- migrate:down
REVOKE EXECUTE ON FUNCTION current_user_id() FROM authenticated;
DROP FUNCTION IF EXISTS current_user_id();

REVOKE USAGE ON SCHEMA public FROM anon, authenticated;

DROP ROLE IF EXISTS authenticator;
DROP ROLE IF EXISTS authenticated;
DROP ROLE IF EXISTS anon;
