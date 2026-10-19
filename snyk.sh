#!/bin/bash
set -e

# Validate required environment variable
if [ -z "${SNYK_ORG}" ]; then
  echo "Error: SNYK_ORG environment variable is not set." >&2
  echo "Please set SNYK_ORG before running this script (e.g., export SNYK_ORG=your_org_id)." >&2
  exit 1
fi

export MAVEN_OPTS="${MAVEN_OPTS:--Xmx512m}"

snyk test || true
snyk monitor --all-projects --org="${SNYK_ORG}"
