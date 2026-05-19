base_url := "http://localhost:8080/api"

# List posts; $TOKEN is optional, filters are PostgREST query params
# (`just data posts` OR `just data posts "title=ilike.*hello*"`)
@posts filters="":
    xh "{{ base_url }}/posts?{{ filters }}" \
      ${TOKEN:+Authorization:"Bearer $TOKEN"}

# Create a post — requires $TOKEN  (just data create-post "Title" "Body")
@create-post title content:
    xh POST {{ base_url }}/posts \
      Authorization:"Bearer $TOKEN" \
      Prefer:"return=representation" \
      'title={{ title }}' \
      'content={{ content }}'

# Search posts by keyword, author, date range, sort order and pagination.
# Pass any subset of params as key=value (strings) or key:=value (numbers/nulls).
# Examples:
#   just data search p_query=hello
#   just data search p_query=hello p_order_by=title p_direction=ASC
#   just data search p_author_id:=1 p_limit:=5 p_offset:=10
@search *args='':
    xh POST {{ base_url }}/rpc/search_posts {{ args }}

# List all users
@users:
    xh {{ base_url }}/users?select=id,username,role,email,bio,avatar_url,created_at
