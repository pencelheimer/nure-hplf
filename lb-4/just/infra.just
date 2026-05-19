# Build custom db image and start all services in the background
up:
    docker compose up -d --build

# Stop all services
down:
    docker compose down

# Stop all services and wipe all data volumes
reset:
    docker compose down -v

# Stream container logs
logs service="":
    docker compose logs -f {{service}}

# Open a psql shell inside the db container
psql:
    docker compose exec db psql -U postgres social
