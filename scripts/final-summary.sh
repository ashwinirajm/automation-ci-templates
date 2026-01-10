#!/bin/bash
set -e

# ----------------------------
# File locations
# ----------------------------
INITIAL="test-output/testng-results-initial.xml"
RERUN="target/surefire-reports/testng-results.xml"
SUMMARY="$GITHUB_STEP_SUMMARY"

# ----------------------------
# Header
# ----------------------------
echo "### 🧪 Final Test Summary" >> "$SUMMARY"
echo "" >> "$SUMMARY"
echo "| Test | Initial Result | Retryable | Rerun Result |" >> "$SUMMARY"
echo "|------|----------------|-----------|--------------|" >> "$SUMMARY"

# ----------------------------
# Safety checks
# ----------------------------
if [ ! -f "$INITIAL" ]; then
  echo "| Initial results missing | ❌ | N/A | N/A |" >> "$SUMMARY"
  exit 1
fi

# ----------------------------
# Collect failed tests from INITIAL run
# ----------------------------
mapfile -t FAILED_TESTS < <(grep 'status="FAIL"' "$INITIAL" || true)

# ----------------------------
# No failures at all
# ----------------------------
if [ ${#FAILED_TESTS[@]} -eq 0 ]; then
  echo "| All tests passed | Pass | N/A | N/A |" >> "$SUMMARY"
  echo "" >> "$SUMMARY"
  echo "✅ All tests passed in initial run" >> "$SUMMARY"
  exit 0
fi

# ----------------------------
# Process each failed test
# ----------------------------
for line in "${FAILED_TESTS[@]}"; do
  CLASS=$(echo "$line" | sed -n 's/.*class="\([^"]*\)".*/\1/p')
  METHOD=$(echo "$line" | sed -n 's/.*name="\([^"]*\)".*/\1/p')

  # Extract failure message reliably
  MESSAGE=$(awk -v method="$METHOD" '
    $0 ~ "<test-method" && $0 ~ "name=\""method"\"" {in_block=1}
    in_block && /<message>/ {
      gsub(/.*<message>|<\/message>.*/, "", $0)
      print
      exit
    }
    in_block && /<\/test-method>/ {in_block=0}
  ' "$INITIAL")

  # Determine retry eligibility
  if echo "$MESSAGE" | grep -Ei "timeout|HTTP 500|500 Internal Server Error" >/dev/null; then
    RETRYABLE="Yes"

    if [ -f "$RERUN" ] && grep -q "name=\"$METHOD\"" "$RERUN"; then
      RERUN_RESULT="Fail"
    else
      RERUN_RESULT="Pass"
    fi
  else
    RETRYABLE="No"
    RERUN_RESULT="N/A"
  fi

  echo "| ${CLASS}.${METHOD} | Fail | ${RETRYABLE} | ${RERUN_RESULT} |" >> "$SUMMARY"
done

# ----------------------------
# Final verdict
# ----------------------------
echo "" >> "$SUMMARY"

if [ -f "$RERUN" ] && grep -q 'status="FAIL"' "$RERUN"; then
  echo "❌ Some retryable tests still failing after rerun" >> "$SUMMARY"
  exit 1
else
  echo "✅ All retryable failures passed after rerun" >> "$SUMMARY"
fi
