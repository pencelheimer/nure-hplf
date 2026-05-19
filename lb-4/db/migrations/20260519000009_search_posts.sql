-- migrate:up

CREATE OR REPLACE FUNCTION search_posts(
  p_query     text        DEFAULT NULL,
  p_author_id bigint      DEFAULT NULL,
  p_after     timestamptz DEFAULT NULL,
  p_before    timestamptz DEFAULT NULL,
  p_order_by  text        DEFAULT 'created_at',
  p_direction text        DEFAULT 'DESC',
  p_limit     int         DEFAULT 20,
  p_offset    int         DEFAULT 0
)
RETURNS SETOF posts
LANGUAGE plpgsql STABLE SECURITY DEFINER AS $$
BEGIN
  IF p_order_by NOT IN ('created_at', 'updated_at', 'title') THEN
    RAISE EXCEPTION 'invalid_order_by'
      USING HINT = 'Allowed values: created_at, updated_at, title', ERRCODE = 'P0001';
  END IF;

  IF upper(p_direction) NOT IN ('ASC', 'DESC') THEN
    RAISE EXCEPTION 'invalid_direction'
      USING HINT = 'Allowed values: ASC, DESC', ERRCODE = 'P0001';
  END IF;

  RETURN QUERY EXECUTE format(
    'SELECT * FROM posts
      WHERE ($1 IS NULL OR title ILIKE $1 OR content ILIKE $1)
        AND ($2 IS NULL OR user_id = $2)
        AND ($3 IS NULL OR created_at >= $3)
        AND ($4 IS NULL OR created_at <= $4)
      ORDER BY %I %s
      LIMIT $5 OFFSET $6',
    p_order_by,
    upper(p_direction)
  ) USING
    CASE WHEN p_query IS NOT NULL THEN '%' || p_query || '%' END,
    p_author_id,
    p_after,
    p_before,
    p_limit,
    p_offset;
END;
$$;

COMMENT ON FUNCTION search_posts(text, bigint, timestamptz, timestamptz, text, text, int, int) IS
  'Filtered and sorted post search.';

GRANT EXECUTE ON FUNCTION search_posts(text, bigint, timestamptz, timestamptz, text, text, int, int)
  TO anon, authenticated;

-- migrate:down

REVOKE EXECUTE ON FUNCTION search_posts(text, bigint, timestamptz, timestamptz, text, text, int, int)
  FROM anon, authenticated;

DROP FUNCTION IF EXISTS search_posts(text, bigint, timestamptz, timestamptz, text, text, int, int);
