#export SPRING_DATASOURCE_USERNAME=""
#export SPRING_DATASOURCE_PASSWORD=""
export SPRING_DATASOURCE_DB="garang_guni"
export SPRING_AUTH_PRIVATE_KEY="$(cat src/main/resources/certs/private.pem)"
export SPRING_AUTH_PUBLIC_KEY="$(cat src/main/resources/certs/public.pem)"
export POSTGRES_DB_HOST="spring_boot_db"
export POSTGRES_DB_PORT="5432"
export SPRING_REDIS_HOST="redis"
export SPRING_REDIS_PORT="6379"

docker compose up --build