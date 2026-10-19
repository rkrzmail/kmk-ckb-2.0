#!/bin/bash
set -e

# Validate required environment variable
if [ -z "${SONAR_TOKEN}" ]; then
  echo "Error: SONAR_TOKEN environment variable is not set." >&2
  echo "Please set SONAR_TOKEN before running this script (e.g., export SONAR_TOKEN=your_token)." >&2
  exit 1
fi

SONAR_HOST_URL="${SONAR_HOST_URL:-http://localhost:9000}"

# Run the SonarQube scan using environment variables
mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.projectKey=kmk-ckb-api \
  -Dsonar.projectName='kmk-ckb-api' \
  -Dsonar.host.url="${SONAR_HOST_URL}" \
  -Dsonar.token="${SONAR_TOKEN}"

echo "SonarQube scan completed successfully."
