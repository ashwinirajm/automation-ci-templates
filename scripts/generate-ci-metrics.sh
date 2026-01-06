#!/bin/bash

REPORT="target/surefire-reports/testng-results.xml"

if [ ! -f "$REPORT" ]; then
  echo "TestNG report not found. Exiting."
  exit 1
fi

TOTAL=$(grep -o 'tests="[0-9]*"' $REPORT | grep -o '[0-9]*')
FAILED=$(grep -o 'failures="[0-9]*"' $REPORT | grep -o '[0-9]*')
SKIPPED=$(grep -o 'skipped="[0-9]*"' $REPORT | grep -o '[0-9]*')
PASSED=$((TOTAL - FAILED - SKIPPED))

PASS_PERCENTAGE=$(awk "BEGIN {printf \"%.1f\", ($PASSED/$TOTAL)*100}")

mkdir -p metrics

cat <<EOF > metrics/ci-metrics.json
{
  "framework": "Appium + TestNG",
  "total_tests": $TOTAL,
  "passed": $PASSED,
  "failed": $FAILED,
  "skipped": $SKIPPED,
  "pass_percentage": $PASS_PERCENTAGE,
  "commit_sha": "$GITHUB_SHA",
  "workflow_run_id": "$GITHUB_RUN_ID",
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
}
EOF

echo "TOTAL_TESTS=$TOTAL" >> $GITHUB_ENV
echo "PASSED=$PASSED" >> $GITHUB_ENV
echo "FAILED=$FAILED" >> $GITHUB_ENV
echo "SKIPPED=$SKIPPED" >> $GITHUB_ENV
echo "PASS_PERCENTAGE=$PASS_PERCENTAGE" >> $GITHUB_ENV
