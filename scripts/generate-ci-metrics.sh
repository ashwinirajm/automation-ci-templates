#!/bin/bash

REPORT="target/surefire-reports/testng-results.xml"

# 1️⃣ Check if TestNG report exists
if [ ! -f "$REPORT" ]; then
  echo "⚠️  testng-results.xml not found at $REPORT"
  echo "Please ensure Surefire plugin is configured and tests are running."
  mkdir -p metrics
  cat <<EOF > metrics/ci-metrics.json
{
  "framework": "Appium + TestNG",
  "total_tests": 0,
  "passed": 0,
  "failed": 0,
  "skipped": 0,
  "pass_percentage": 0,
  "commit_sha": "$GITHUB_SHA",
  "workflow_run_id": "$GITHUB_RUN_ID",
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
}
EOF
  echo "TOTAL_TESTS=0" >> $GITHUB_ENV
  echo "PASSED=0" >> $GITHUB_ENV
  echo "FAILED=0" >> $GITHUB_ENV
  echo "SKIPPED=0" >> $GITHUB_ENV
  echo "PASS_PERCENTAGE=0" >> $GITHUB_ENV
  exit 0
fi

# 2️⃣ Extract counts from XML (default 0 if empty)
TOTAL=$(grep -o 'tests="[0-9]*"' $REPORT | grep -o '[0-9]*')
TOTAL=${TOTAL:-0}

FAILED=$(grep -o 'failures="[0-9]*"' $REPORT | grep -o '[0-9]*')
FAILED=${FAILED:-0}

SKIPPED=$(grep -o 'skipped="[0-9]*"' $REPORT | grep -o '[0-9]*')
SKIPPED=${SKIPPED:-0}

PASSED=$((TOTAL - FAILED - SKIPPED))
PASSED=${PASSED:-0}

PASS_PERCENTAGE=$(awk "BEGIN {printf \"%.1f\", ($PASSED/$TOTAL)*100}")
PASS_PERCENTAGE=${PASS_PERCENTAGE:-0}

# 3️⃣ Create metrics directory
mkdir -p metrics

# 4️⃣ Generate metrics JSON
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

# 5️⃣ Export to GitHub environment
echo "TOTAL_TESTS=$TOTAL" >> $GITHUB_ENV
echo "PASSED=$PASSED" >> $GITHUB_ENV
echo "FAILED=$FAILED" >> $GITHUB_ENV
echo "SKIPPED=$SKIPPED" >> $GITHUB_ENV
echo "PASS_PERCENTAGE=$PASS_PERCENTAGE" >> $GITHUB_ENV

echo "✅ CI metrics generated successfully"
