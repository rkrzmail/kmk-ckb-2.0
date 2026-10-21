#!/usr/bin/env bash
set -e

# Pindah ke root direktori project (tempat script berada)
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

echo "==> Memuat environment variables dari $ENV_FILE..."

# Load environment variables dari file env secara aman
while IFS='=' read -r key value || [ -n "$key" ]; do
  # Lewati baris kosong atau baris komentar
  [[ "$key" =~ ^[[:space:]]*# ]] && continue
  [[ -z "$key" ]] && continue
  
  # Trim spasi
  key="$(echo "$key" | xargs)"
  [ -z "$key" ] && continue
  
  # Pastikan key adalah identifier shell yang valid
  if [[ ! "$key" =~ ^[a-zA-Z_][a-zA-Z0-9_]*$ ]]; then
    continue
  fi
  
  # Hapus tanda kutip depan/belakang jika ada
  value="${value%\"}"
  value="${value#\"}"
  value="${value%\'}"
  value="${value#\'}"
  
  export "$key=$value"
done < "$ENV_FILE"

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
