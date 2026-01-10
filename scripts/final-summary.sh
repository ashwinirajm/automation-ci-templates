#!/bin/bash
set -e

INITIAL="test-output/testng-results-initial.xml"
RERUN="target/surefire-reports/testng-results.xml"
SUMMARY="$GITHUB_STEP_SUMMARY"

echo "### 🧪 Final Test Summary" >> "$SUMMARY"
echo "" >> "$SUMMARY"
echo "| Test | Initial Result | Retryable | Rerun Result |" >> "$SUMMARY"
echo "|------|----------------|-----------|--------------|" >> "$SUMMARY"

# Safety
if [ ! -f "$INITIAL" ]; then
  echo "| Initial results missing | ❌ | N/A | N/A |" >> "$SUMMARY"
  exit 1
fi

# Extract failed tests from INITIAL run
mapfile -t FAILED < <(grep 'status="FAIL"' "$INITIAL")

if [ ${#FAILED[@]} -eq 0 ]; then
  echo "| All tests passed | Pass | N/A | N/A |" >> "$SUMMARY"
  echo "" >> "$SUMMARY"
  echo "✅ All tests passed in initial run" >> "$SUMMARY"
  exit 0
fi

for line in "${FAILED[@]}"; do
  CLASS=$(echo "$line" | sed -n 's/.*class="\([^"]*\)".*/\1/p')
  METHOD=$(echo "$line" | sed -n 's/.*name="\([^"]*\)".*/\1/p')

  MESSAGE=$(grep -A3 "$METHOD" "$INITIAL" | grep '<message>' | head -1 | sed 's/.*<message>\(.*\)<\/message>.*/\1/')

  if echo "$MESSAGE" | grep -Ei "timeout|HTTP 500|500 Internal Server Error" > /dev/null; then
    RETRY="Yes"
    if grep -q "$METHOD" "$RERUN"; then
      RERUN_RESULT="Fail"
    else
      RERUN_RESULT="Pass"
    fi
  else
    RETRY="No"
    RERUN_RESULT="N/A"
  fi

  echo "| ${CLASS}.${METHOD} | Fail | $RETRY | $RERUN_RESULT |" >> "$SUMMARY"
done

echo "" >> "$SUMMARY"

# Final verdict
if grep -q 'status="FAIL"' "$RERUN"; then
  echo "❌ Some retryable tests still failing after rerun" >> "$SUMMARY"
  exit 1
else
  echo "✅ All retryable failures passed after rerun" >> "$SUMMARY"
fi
