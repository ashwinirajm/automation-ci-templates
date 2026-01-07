#!/bin/bash
set -e

# Ensure this script is executable (works if accidentally checked in non-executable)
chmod +x "$0" || true

RESULTS="target/surefire-reports/testng-results.xml"
RETRY_FAILED="target/surefire-reports/testng-failed.xml"
SUMMARY_FILE="$GITHUB_STEP_SUMMARY"

echo "### 🧪 Final Test Summary" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

# Table header
TABLE="| Test | Initial Result | Retryable | Rerun Result |\n"
TABLE="$TABLE|------|----------------|-----------|--------------|\n"

# Read failed lines safely
FAILED_LINES=()
while IFS= read -r line; do
  FAILED_LINES+=("$line")
done < <(grep '<test-method status="FAIL"' "$RESULTS" || true)

for line in "${FAILED_LINES[@]}"; do
  CLASS=$(echo "$line" | sed -n 's/.*class="\([^"]*\)".*/\1/p')
  METHOD=$(echo "$line" | sed -n 's/.*name="\([^"]*\)".*/\1/p')
  MESSAGE=$(echo "$line" | sed -n 's/.*>\(.*\)<\/exception>.*/\1/p')

  if echo "$MESSAGE" | grep -Ei "timeout|HTTP 500|500 Internal Server Error" > /dev/null; then
    RETRY="Yes"
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

# Append table to summary
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
