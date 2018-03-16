#!/usr/bin/env bash
set -e
AWS_PROFILE=paulbaker
export AWS_PROFILE
if [ ! -f ./payload.json ]; then
  echo "Make sure you have a 'payload.json' file, or you can't test the lambda afterwards."
  exit 1
fi
aws lambda invoke --function-name jira-jql-lambda --payload "$(cat ./payload.json)" "outfile.json"
cat outfile.json | jq '.'
