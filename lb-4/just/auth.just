base_url := "http://localhost:8080/api"

# Register a new user and print the JWT  (just auth register alice alice@example.com s3cr3t)
register username email password:
    xh POST {{base_url}}/rpc/register \
      p_username={{username}} p_email={{email}} p_password={{password}}

# Log in and print the JWT  (just auth login alice s3cr3t)
login username password:
    xh POST {{base_url}}/rpc/login \
      p_username={{username}} p_password={{password}}

# Promote a user to a role — requires $TOKEN set to an admin JWT  (just auth promote <user_id> admin)
promote user_id role:
    xh POST {{base_url}}/rpc/promote \
      Authorization:"Bearer $TOKEN" \
      p_user_id:={{user_id}} p_new_role={{role}}
