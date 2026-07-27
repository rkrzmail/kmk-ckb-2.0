export MAVEN_OPTS="-Xmx512m"
snyk test || true
snyk monitor --all-projects --org=8fca34cc-424b-49e8-80c5-aa264cccd87b