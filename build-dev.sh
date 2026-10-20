#!/usr/bin/env bash
set -e

# Pindah ke root direktori project (tempat script berada)
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

ENV_FILE=".env.development"

if [ ! -f "$ENV_FILE" ]; then
  echo "Error: File $ENV_FILE tidak ditemukan di $DIR"
  exit 1
fi

echo "==> Memuat environment variables dari $ENV_FILE..."

# Load environment variables dari .env.development
set -a
# shellcheck disable=SC1090
source <(grep -v '^[[:space:]]*#' "$ENV_FILE" | grep -v '^[[:space:]]*$')
set +a

# Pilih maven wrapper (./mvnw) jika ada, atau fallback ke mvn sistem
if [ -x "./mvnw" ]; then
  MVN_CMD="./mvnw"
elif command -v mvn >/dev/null 2>&1; then
  MVN_CMD="mvn"
else
  echo "Error: Maven (mvn) atau ./mvnw tidak ditemukan!"
  exit 1
fi

echo "==> Menjalankan $MVN_CMD clean package dengan profile active: ${SPRING_PROFILES_ACTIVE:-dev}..."

# Eksekusi clean package dengan argumen tambahan yang diteruskan (misal: -DskipTests)
$MVN_CMD clean package "$@"

echo "==> Build selesai! Hasil JAR berada di target/"
