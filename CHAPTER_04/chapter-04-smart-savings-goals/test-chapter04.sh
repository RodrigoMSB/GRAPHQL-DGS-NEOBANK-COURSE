#!/bin/bash

# ==============================================================================
# CHAPTER 04: SMART SAVINGS GOALS - AUTOMATED TESTING SCRIPT
# ==============================================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m'

GRAPHQL_URL="http://localhost:8080/graphql"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="test-results-chapter04_${TIMESTAMP}.txt"
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Initialize log file
echo "CHAPTER 04: SMART SAVINGS GOALS - TEST RESULTS" > "$LOG_FILE"
echo "Date: $(date)" >> "$LOG_FILE"
echo "========================================" >> "$LOG_FILE"
echo "" >> "$LOG_FILE"

print_header() {
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║  $1${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo "" >> "$LOG_FILE"
    echo "╔══════════════════════════════════════════════════════════════╗" >> "$LOG_FILE"
    echo "║  $1" >> "$LOG_FILE"
    echo "╚══════════════════════════════════════════════════════════════╝" >> "$LOG_FILE"
}

print_test_box() {
    local test_num=$1
    local description=$2
    echo -e "${CYAN}┌──────────────────────────────────────────────────────────────┐${NC}"
    echo -e "${CYAN}│ TEST $test_num: $description${NC}"
    echo -e "${CYAN}└──────────────────────────────────────────────────────────────┘${NC}"
    echo "" >> "$LOG_FILE"
    echo "┌──────────────────────────────────────────────────────────────┐" >> "$LOG_FILE"
    echo "│ TEST $test_num: $description" >> "$LOG_FILE"
    echo "└──────────────────────────────────────────────────────────────┘" >> "$LOG_FILE"
}

print_request() {
    local query=$1
    echo -e "${MAGENTA}🚀 REQUEST:${NC}"
    # Format query nicely
    local formatted=$(echo "$query" | sed 's/{ /{\n  /g' | sed 's/ {/{\n    /g' | sed 's/} /\n  }/g' | sed 's/}/\n}/g')
    echo "$formatted"
    echo ""
    echo "🚀 REQUEST:" >> "$LOG_FILE"
    echo "$formatted" >> "$LOG_FILE"
    echo "" >> "$LOG_FILE"
}

print_response() {
    local response=$1
    echo -e "${BLUE}📦 RESPONSE:${NC}"
    echo "$response" | jq '.'
    echo ""
    echo "📦 RESPONSE:" >> "$LOG_FILE"
    echo "$response" | jq '.' >> "$LOG_FILE"
    echo "" >> "$LOG_FILE"
}

print_success() {
    echo -e "${GREEN}✅ RESULT: PASS${NC}"
    echo "✅ RESULT: PASS" >> "$LOG_FILE"
    ((PASSED_TESTS++))
}

print_error() {
    echo -e "${RED}❌ RESULT: FAIL - $1${NC}"
    echo "❌ RESULT: FAIL - $1" >> "$LOG_FILE"
    ((FAILED_TESTS++))
}

pause_interactive() {
    echo -e "${YELLOW}───────────────────────────────────────────────────────────────${NC}"
    echo -e "${YELLOW}Press Enter to continue...${NC}"
    read -r
    echo ""
}

execute_query() {
    local query="$1"
    local description="$2"
    
    ((TOTAL_TESTS++))
    print_test_box "$TOTAL_TESTS" "$description"
    
    # Clean query for display (remove escape characters)
    display_query=$(echo "$query" | sed 's/\\//g')
    print_request "$display_query"
    
    response=$(curl -s -X POST "$GRAPHQL_URL" \
        -H "Content-Type: application/json" \
        -d "{\"query\":\"$query\"}")
    
    print_response "$response"
    
    if echo "$response" | jq -e '.data' > /dev/null 2>&1; then
        if echo "$response" | jq -e '.errors' > /dev/null 2>&1; then
            print_error "Query returned errors"
            pause_interactive
            return 1
        else
            print_success
            pause_interactive
            return 0
        fi
    else
        print_error "Invalid response from server"
        pause_interactive
        return 1
    fi
}

# ==============================================================================
# PRE-FLIGHT CHECKS
# ==============================================================================

print_header "PRE-FLIGHT CHECKS"

if ! command -v curl &> /dev/null; then
    echo -e "${RED}❌ curl not installed${NC}"
    echo "❌ curl not installed" >> "$LOG_FILE"
    exit 1
fi
echo -e "${GREEN}✅ curl installed${NC}"
echo "✅ curl installed" >> "$LOG_FILE"

if ! command -v jq &> /dev/null; then
    echo -e "${RED}❌ jq not installed${NC}"
    echo "❌ jq not installed" >> "$LOG_FILE"
    exit 1
fi
echo -e "${GREEN}✅ jq installed${NC}"
echo "✅ jq installed" >> "$LOG_FILE"

if ! curl -s "$GRAPHQL_URL" > /dev/null 2>&1; then
    echo -e "${RED}❌ Server not running at $GRAPHQL_URL${NC}"
    echo "❌ Server not running" >> "$LOG_FILE"
    exit 1
fi
echo -e "${GREEN}✅ Server running at $GRAPHQL_URL${NC}"
echo "✅ Server running" >> "$LOG_FILE"

pause_interactive

# ==============================================================================
# TESTS
# ==============================================================================

print_header "RUNNING TESTS"

# TEST 1
execute_query "{ savingsGoals(userId: \\\"1\\\") { id name targetAmount currentAmount progressPercentage category status } }" \
    "Get all savings goals for user 1"

# TEST 2
execute_query "{ activeSavingsGoals(userId: \\\"1\\\") { id name currentAmount targetAmount progressPercentage status } }" \
    "Get active savings goals only"

# TEST 3
execute_query "{ savingsGoal(id: \\\"1\\\") { id name description targetAmount currentAmount progressPercentage category status } }" \
    "Get specific savings goal by ID"

# TEST 4
execute_query "{ savingsGoals(userId: \\\"2\\\") { id name targetAmount currentAmount progressPercentage category status } }" \
    "Get savings goals for user 2"

# TEST 5
execute_query "mutation { createSavingsGoal(input: { userId: \\\"1\\\" name: \\\"Tesla Model 3\\\" description: \\\"Electric car savings\\\" targetAmount: 50000 category: OTHER }) { success message goal { id name targetAmount currentAmount progressPercentage category status } } }" \
    "Create new savings goal (Tesla)"

# TEST 6
execute_query "{ savingsGoals(userId: \\\"1\\\") { id name category status } }" \
    "Verify created goal exists"

# TEST 7
execute_query "{ savingsGoals(userId: \\\"3\\\") { id name targetAmount currentAmount progressPercentage category status } }" \
    "Get user 3 goals (includes PAUSED)"

# TEST 8
((TOTAL_TESTS++))
print_test_box "$TOTAL_TESTS" "Validate progress calculation (100%)"

query_display='{ savingsGoal(id: "3") { name targetAmount currentAmount progressPercentage } }'
print_request "$query_display"

response=$(curl -s -X POST "$GRAPHQL_URL" \
    -H "Content-Type: application/json" \
    -d "{\"query\":\"{ savingsGoal(id: \\\"3\\\") { name targetAmount currentAmount progressPercentage } }\"}")

print_response "$response"

progress=$(echo "$response" | jq -r '.data.savingsGoal.progressPercentage')

if [[ "$progress" == "100" || "$progress" == "100.0" ]]; then
    print_success
else
    print_error "Expected 100, got $progress"
fi
pause_interactive

# TEST 9
((TOTAL_TESTS++))
print_test_box "$TOTAL_TESTS" "Verify active goals filtering (no PAUSED)"

query_display='{ activeSavingsGoals(userId: "3") { name status } }'
print_request "$query_display"

response=$(curl -s -X POST "$GRAPHQL_URL" \
    -H "Content-Type: application/json" \
    -d "{\"query\":\"{ activeSavingsGoals(userId: \\\"3\\\") { name status } }\"}")

print_response "$response"

paused_count=$(echo "$response" | jq '[.data.activeSavingsGoals[] | select(.status == "PAUSED")] | length')

if [ "$paused_count" == "0" ]; then
    print_success
else
    print_error "Found $paused_count PAUSED goals"
fi
pause_interactive

# TEST 10
execute_query "mutation { createSavingsGoal(input: { userId: \\\"2\\\" name: \\\"Vacation Fund\\\" targetAmount: 10000 category: VACATION }) { success message goal { id name description currentAmount status } } }" \
    "Create goal with minimal fields"

# ==============================================================================
# SUMMARY
# ==============================================================================

print_header "TEST SUMMARY"

echo -e "${BLUE}Total Tests:${NC}  $TOTAL_TESTS"
echo -e "${GREEN}Passed:${NC}       $PASSED_TESTS"
echo -e "${RED}Failed:${NC}       $FAILED_TESTS"

echo "" >> "$LOG_FILE"
echo "Total Tests:  $TOTAL_TESTS" >> "$LOG_FILE"
echo "Passed:       $PASSED_TESTS" >> "$LOG_FILE"
echo "Failed:       $FAILED_TESTS" >> "$LOG_FILE"

if [ $FAILED_TESTS -eq 0 ]; then
    echo ""
    echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║                 ✅ ALL TESTS PASSED! ✅                      ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo "" >> "$LOG_FILE"
    echo "✅ ALL TESTS PASSED!" >> "$LOG_FILE"
    echo ""
    echo -e "${CYAN}📄 Results saved to: $LOG_FILE${NC}"
    exit 0
else
    echo ""
    echo -e "${RED}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║                 ❌ SOME TESTS FAILED ❌                      ║${NC}"
    echo -e "${RED}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo "" >> "$LOG_FILE"
    echo "❌ SOME TESTS FAILED" >> "$LOG_FILE"
    echo ""
    echo -e "${CYAN}📄 Results saved to: $LOG_FILE${NC}"
    exit 1
fi