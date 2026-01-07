#!/bin/bash
set -e

# Make sure script is executable
chmod +x "$0" || true

RESULTS="target/surefire-reports/testng-results.xml"
RETRY_FAILED="target/surefire-reports/testng-failed.xml"
SUMMARY_FILE="$GITHUB_STEP_SUMMARY"

echo "### 🧪 Final Test Summary" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

# Table header
TABLE="| Test | Initial Result | Retryable | Rerun Result |\n"
TABLE="$TABLE|------|----------------|-----------|--------------|\n"

# Check if test results exist
if [ ! -f "$RESULTS" ]; then
  TABLE="${TABLE}| No test results found | N/A | N/A | N/A |\n"
  echo -e "$TABLE" >> "$SUMMARY_FILE"
  echo "⚠️ TestNG results not found: $RESULTS" >> "$SUMMARY_FILE"
  exit 1
fi

# Capture all failed tests (both TestNG <test-method> and <testcase> failures)
FAILED_LINES=()
mapfile -t FAILED_LINES < <(
  grep -E '<test-method status="FAIL"|<testcase.*failure' "$RESULTS" || true
)

# If no failed tests
if [ ${#FAILED_LINES[@]} -eq 0 ]; then
  TABLE="${TABLE}| All tests passed | Pass | N/A | N/A |\n"
else
  for line in "${FAILED_LINES[@]}"; do
    # Extract class and method names
    CLASS=$(echo "$line" | sed -n 's/.*class="\([^"]*\)".*/\1/p')
    METHOD=$(echo "$line" | sed -n 's/.*name="\([^"]*\)".*/\1/p')
    # fallback for <testcase classname="">
    if [ -z "$CLASS" ]; then
      CLASS=$(echo "$line" | sed -n 's/.*classname="\([^"]*\)".*/\1/p')
    fi
    if [ -z "$METHOD" ]; then
      METHOD=$(echo "$line" | sed -n 's/.*name="\([^"]*\)".*/\1/p')
    fi
    # Extract exception message if present
    MESSAGE=$(echo "$line" | sed -n 's/.*>\(.*\)<\/exception>.*/\1/p')

    # Determine if retryable (Timeout / HTTP 500)
    if echo "$MESSAGE" | grep -Ei "timeout|HTTP 500|500 Internal Server Error" > /dev/null; then
      RETRY="Yes"
      # Check if it still failed after rerun
      if [ -f "$RETRY_FAILED" ] && grep -q "$METHOD" "$RETRY_FAILED"; then
        RERUN_RESULT="Fail"
      else
        RERUN_RESULT="Pass"
      fi
    else
      RETRY="No"
      RERUN_RESULT="N/A"
    fi

    TABLE="${TABLE}| ${CLASS}.${METHOD} | Fail | ${RETRY} | ${RERUN_RESULT} |\n"
  done
fi

# Append the table to GitHub Actions summary
echo -e "$TABLE" >> "$SUMMARY_FILE"

# Overall verdict
if [ -f "$RETRY_FAILED" ]; then
  echo "" >> "$SUMMARY_FILE"
  echo "❌ Some retryable tests still failing after rerun" >> "$SUMMARY_FILE"
  exit 1
else
  echo "" >> "$SUMMARY_FILE"
  echo "✅ All retryable failures passed after rerun" >> "$SUMMARY_FILE"
fi
