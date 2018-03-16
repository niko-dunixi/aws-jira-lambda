#!/usr/bin/env bash
set -e
if [ ! -f ./payload.json ]; then
  echo "Make sure you have a 'payload.json' file, or you can't test the lambda afterwards."
  exit 1
fi
./mvnw clean package
AWS_PROFILE=paulbaker
export AWS_PROFILE
aws lambda update-function-code --function-name jira-jql-lambda --zip-file "fileb://$(ls ./target/aws-jira-lambda-*.jar)"
./invoke-lambda.sh
