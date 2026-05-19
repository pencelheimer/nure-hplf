base_url := "http://localhost:8080"

# Open the Dozzle log viewer UI in the default browser
logs:
    xdg-open {{ base_url }}/logs 2>/dev/null

# Open the Scalar API docs in the default browser
docs:
    xdg-open {{ base_url }}/docs 2>/dev/null
