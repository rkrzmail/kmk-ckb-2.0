#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

# Deteksi file env (prioritas: .env.development -> .env.dev -> .env)
ENV_FILE=""
if [ -f ".env.development" ]; then
  ENV_FILE=".env.development"
elif [ -f ".env.dev" ]; then
  ENV_FILE=".env.dev"
elif [ -f ".env" ]; then
  ENV_FILE=".env"
else
  echo "Error: File environment (.env.development / .env.dev / .env) tidak ditemukan di $DIR"
  exit 1
fi

JAR_FILE="target/kmkbe-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
  echo "==> File $JAR_FILE belum ada. Membangun project terlebih dahulu..."
  ./build-dev.sh -DskipTests
fi

echo "==> Memuat environment variables dari $ENV_FILE..."

# Load environment variables dari file env secara aman
while IFS='=' read -r key value || [ -n "$key" ]; do
  [[ "$key" =~ ^[[:space:]]*# ]] && continue
  [[ -z "$key" ]] && continue
  key="$(echo "$key" | xargs)"
  [ -z "$key" ] && continue
  if [[ ! "$key" =~ ^[a-zA-Z_][a-zA-Z0-9_]*$ ]]; then
    continue
  fi
  value="${value%\"}"
  value="${value#\"}"
  value="${value%\'}"
  value="${value#\'}"
  export "$key=$value"
done < "$ENV_FILE"

echo "==> Menjalankan $JAR_FILE dengan profile: ${SPRING_PROFILES_ACTIVE:-dev}..."
exec java -jar "$JAR_FILE" "$@"
