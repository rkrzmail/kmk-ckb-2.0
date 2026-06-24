#!/bin/bash

# Navigate to your project directory
cd /path/to/your/project

# Run the SonarQube scan
mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.projectKey=kmk-ckb-api \
  -Dsonar.projectName='kmk-ckb-api' \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=sqp_3afb6dccf2551b250c532469e7801a876a4c0f59

# Check the exit status
if [ $? -eq 0 ]; then
  echo "SonarQube scan completed successfully."
else
  echo "SonarQube scan failed. Please check the logs for details."
fi