#!/bin/bash

export SPRING_DATASOURCE_USERNAME=""
export SPRING_DATASOURCE_PASSWORD=""
export SPRING_DATASOURCE_DB=""
export SPRING_AUTH_PRIVATE_KEY="$(cat src/main/resources/certs/private.pem)"
export SPRING_AUTH_PUBLIC_KEY="$(cat src/main/resources/certs/public.pem)"
export POSTGRES_DB_HOST="localhost"
export POSTGRES_DB_PORT="5432"
export SPRING_REDIS_HOST="localhost"
export SPRING_REDIS_PORT="6379"

if [ -z "$SPRING_DATASOURCE_USERNAME" ]; then
    echo "⚠️  Please set SPRING_DATASOURCE_USERNAME before running the script"
    exit 1
fi

if [ -z "$SPRING_DATASOURCE_PASSWORD" ]; then
    echo "⚠️  Please set SPRING_DATASOURCE_PASSWORD before running the script"
    exit 1
fi

if [ -z "$SPRING_DATASOURCE_DB" ]; then
    echo "⚠️  Please set SPRING_DATASOURCE_DB before running the script"
    exit 1
fi

if [ ! -f "src/main/resources/certs/private.pem" ]; then
    echo "⚠️  Private key not found at src/main/resources/certs/private.pem"
    echo "Please refer to this pull request on how to create them locally: https://github.com/lolfoollor/garang-guni-backend/pull/1"
    echo "Or this website for instructions: https://docs.openssl.org/3.4/man1/openssl-genpkey/"
    exit 1
fi

if [ ! -f "src/main/resources/certs/public.pem" ]; then
    echo "⚠️  Public key not found at src/main/resources/certs/public.pem"
    echo "Please refer to this pull request on how to create them locally: https://github.com/lolfoollor/garang-guni-backend/pull/1"
    echo "Or this website for instructions: https://docs.openssl.org/3.4/man1/openssl-genpkey/"
    exit 1
fi

./mvnw spring-boot:run
#./mvnw test  -- For Testing