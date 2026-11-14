#!/usr/bin/env bash
set -euo pipefail

image=${1:-ms-usuarios:0.4.0}
network="p4-smoke-$$"
database="$network-db"
service="$network-app"

docker network create "$network"
cleanup() {
    docker rm -f "$service" "$database" >/dev/null 2>&1 || true
    docker network rm "$network" >/dev/null 2>&1 || true
}
trap cleanup EXIT

test "$(docker run --rm "$image" whoami)" = app
password=$(openssl rand -hex 24)
jwt=$(openssl rand -base64 32)
docker run -d --name "$database" --network "$network" \
    -e POSTGRES_DB=usuarios -e POSTGRES_USER=usuarios \
    -e "POSTGRES_PASSWORD=$password" postgres:16
for attempt in {1..30}; do
    if docker exec "$database" pg_isready -h 127.0.0.1 -U usuarios -d usuarios; then
        break
    fi
    sleep 2
done
docker exec "$database" pg_isready -h 127.0.0.1 -U usuarios -d usuarios

docker run -d --name "$service" --network "$network" -p 127.0.0.1::8080 \
    -e "DB_URL=jdbc:postgresql://$database:5432/usuarios" \
    -e DB_USERNAME=usuarios -e "DB_PASSWORD=$password" -e "JWT_SECRET=$jwt" "$image"

address=$(docker port "$service" 8080/tcp)
for attempt in {1..45}; do
    if [[ "$(docker inspect -f '{{.State.Health.Status}}' "$service")" == healthy ]]; then
        curl -fsS "http://$address/actuator/health" | jq -e '.status == "UP"'
        exit 0
    fi
    sleep 2
done
docker logs "$service" >&2
exit 1