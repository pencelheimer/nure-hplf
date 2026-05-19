-- migrate:up

-- Separate schema keeps extension functions out of the public schema,
-- so PostgREST does not expose them in the OpenAPI spec
CREATE SCHEMA extensions;

CREATE EXTENSION pgcrypto SCHEMA extensions;  -- bcrypt hashing via crypt() + gen_salt()
CREATE EXTENSION pgjwt    SCHEMA extensions;  -- JWT signing via sign() and verify()

-- migrate:down

DROP EXTENSION IF EXISTS pgjwt;
DROP EXTENSION IF EXISTS pgcrypto;
DROP SCHEMA IF EXISTS extensions;
