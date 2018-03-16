#!/usr/bin/env bash
set -e
if [ ! -f ./payload.json ]; then
  echo "Make sure you have a 'payload.json' file, or you can't test the lambda afterwards."
  exit 1
fi
curl -X POST \
  https://kwu90iacdl.execute-api.us-east-1.amazonaws.com/prod/jira/search \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/json' \
  -H 'Postman-Token: 8d0d51ba-8b2d-4596-a349-8f56f873a388' \
  -d "$(cat ./payload.json)" | jq '.'
