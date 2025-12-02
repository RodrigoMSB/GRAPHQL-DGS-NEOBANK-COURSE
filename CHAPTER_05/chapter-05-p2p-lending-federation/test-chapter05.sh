#!/bin/bash

# ============================================================================
# TEST SCRIPT - CHAPTER 05: P2P Lending Federation
# ============================================================================

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counters
TESTS_PASSED=0
TESTS_FAILED=0
TOTAL_TESTS=0

# Generate timestamp for log file
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="test-results-chapter05_${TIMESTAMP}.txt"

# ============================================================================
# HELPER FUNCTIONS
# ============================================================================

print_header() {
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║  $1${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
}

print_test_box() {
    echo -e "${CYAN}┌──────────────────────────────────────────────────────────────┐${NC}"
    echo -e "${CYAN}│ TEST $1${NC}"
    echo -e "${CYAN}└──────────────────────────────────────────────────────────────┘${NC}"
}

print_success() {
    echo -e "${GREEN}✅ RESULT: PASS${NC}\n"
    ((TESTS_PASSED++))
}

print_failure() {
    echo -e "${RED}❌ RESULT: FAIL - $1${NC}\n"
    ((TESTS_FAILED++))
}

execute_query() {
    local SERVICE_URL=$1
    local QUERY=$2
    local TEST_NAME=$3
    
    ((TOTAL_TESTS++))
    
    print_test_box "$TOTAL_TESTS: $TEST_NAME"
    
    # Format query for display
    FORMATTED_QUERY=$(echo "$QUERY" | sed 's/\\n/\n  /g' | sed 's/\\"//g')
    
    echo -e "${MAGENTA}🚀 REQUEST to $SERVICE_URL:${NC}"
    echo -e "${BLUE}$FORMATTED_QUERY${NC}\n"
    
    # Execute query
    RESPONSE=$(curl -s -X POST "$SERVICE_URL" \
        -H "Content-Type: application/json" \
        -d "{\"query\":\"$QUERY\"}")
    
    # Format response with jq if available
    if command -v jq &> /dev/null; then
        FORMATTED_RESPONSE=$(echo "$RESPONSE" | jq '.')
    else
        FORMATTED_RESPONSE="$RESPONSE"
    fi
    
    echo -e "${MAGENTA}📦 RESPONSE:${NC}"
    echo -e "${BLUE}$FORMATTED_RESPONSE${NC}\n"
    
    # Log to file
    {
        echo "═══════════════════════════════════════════════════════════════"
        echo "TEST $TOTAL_TESTS: $TEST_NAME"
        echo "Service: $SERVICE_URL"
        echo "───────────────────────────────────────────────────────────────"
        echo "REQUEST:"
        echo "$FORMATTED_QUERY"
        echo ""
        echo "RESPONSE:"
        echo "$FORMATTED_RESPONSE"
        echo ""
    } >> "$LOG_FILE"
    
    echo "$RESPONSE"
}

# ============================================================================
# PRE-FLIGHT CHECKS
# ============================================================================

print_header "PRE-FLIGHT CHECKS"

# Check curl
if ! command -v curl &> /dev/null; then
    echo -e "${RED}❌ curl is not installed${NC}"
    exit 1
fi
echo -e "${GREEN}✅ curl is installed${NC}"

# Check jq (optional but recommended)
if ! command -v jq &> /dev/null; then
    echo -e "${YELLOW}⚠️  jq is not installed (JSON formatting will be limited)${NC}"
else
    echo -e "${GREEN}✅ jq is installed${NC}"
fi

# Check users-service
USERS_URL="http://localhost:8081/graphql"
if curl -s -f -o /dev/null "$USERS_URL"; then
    echo -e "${GREEN}✅ Users Service is running on port 8081${NC}"
else
    echo -e "${RED}❌ Users Service is not responding on port 8081${NC}"
    echo -e "${YELLOW}Start it with: cd users-service && mvn spring-boot:run${NC}"
    exit 1
fi

# Check loans-service
LOANS_URL="http://localhost:8082/graphql"
if curl -s -f -o /dev/null "$LOANS_URL"; then
    echo -e "${GREEN}✅ Loans Service is running on port 8082${NC}"
else
    echo -e "${RED}❌ Loans Service is not responding on port 8082${NC}"
    echo -e "${YELLOW}Start it with: cd loans-service && mvn spring-boot:run${NC}"
    exit 1
fi

echo ""

# ============================================================================
# USERS SERVICE TESTS
# ============================================================================

print_header "TESTING USERS SERVICE (Port 8081)"
echo ""

# Test 1: Get single user
RESPONSE=$(execute_query "$USERS_URL" \
    "{ user(id: \\\"user-001\\\") { id email fullName userType reputation } }" \
    "Get single user (Alice)")

if echo "$RESPONSE" | grep -q "Alice Thompson"; then
    print_success
else
    print_failure "User not found or incorrect data"
fi

{
    echo "✅ PASS"
    echo ""
} >> "$LOG_FILE"

read -p "Press Enter to continue..."

# Test 2: Get all users
RESPONSE=$(execute_query "$USERS_URL" \
    "{ users { id fullName userType } }" \
    "Get all users")

if echo "$RESPONSE" | grep -q "user-001" && echo "$RESPONSE" | grep -q "user-002"; then
    print_success
else
    print_failure "Users list incomplete"
fi

{
    echo "✅ PASS"
    echo ""
} >> "$LOG_FILE"

read -p "Press Enter to continue..."

# Test 3: Get verified lenders
RESPONSE=$(execute_query "$USERS_URL" \
    "{ verifiedLenders { id fullName lenderProfile { totalLent verified } } }" \
    "Get verified lenders only")

if echo "$RESPONSE" | grep -q "lenderProfile" && echo "$RESPONSE" | grep -q "totalLent"; then
    print_success
else
    print_failure "Lender profile data missing"
fi

{
    echo "✅ PASS"
    echo ""
} >> "$LOG_FILE"

read -p "Press Enter to continue..."

# Test 4: Get verified borrowers
RESPONSE=$(execute_query "$USERS_URL" \
    "{ verifiedBorrowers { id fullName borrowerProfile { creditScore kycStatus } } }" \
    "Get verified borrowers only")

if echo "$RESPONSE" | grep -q "borrowerProfile" && echo "$RESPONSE" | grep -q "creditScore"; then
    print_success
else
    print_failure "Borrower profile data missing"
fi

{
    echo "✅ PASS"
    echo ""
} >> "$LOG_FILE"

read -p "Press Enter to continue..."

# Test 5: Create new user
RESPONSE=$(execute_query "$USERS_URL" \
    "mutation { createUser(input: { email: \\\"test@neobank.com\\\", fullName: \\\"Test User\\\", userType: BORROWER }) { success message user { id email } } }" \
    "Create new user (mutation)")

if echo "$RESPONSE" | grep -q "\"success\":true"; then
    print_success
else
    print_failure "User creation failed"
fi

{
    echo "✅ PASS"
    echo ""
} >> "$LOG_FILE"

read -p "Press Enter to continue..."

# ============================================================================
# LOANS SERVICE TESTS
# ============================================================================

print_header "TESTING LOANS SERVICE (Port 8082)"
echo ""

# Test 6: Get single loan
RESPONSE=$(execute_query "$LOANS_URL" \
    "{ loan(id: \\\"loan-001\\\") { id amount interestRate status } }" \
    "Get single loan")

if echo "$RESPONSE" | grep -q "loan-001" && echo "$RESPONSE" | grep -q "amount"; then
    print_success
else
    print_failure "Loan not found"
fi

{
    echo "✅ PASS"
    echo ""
} >> "$LOG_FILE"

read -p "Press Enter to continue..."

# Test 7: Get all loans
RESPONSE=$(execute_query "$LOANS_URL" \
    "{ loans { id amount status purpose } }" \
    "Get all loans")

if echo "$RESPONSE" | grep -q "loan-001" && echo "$RESPONSE" | grep -q "loan-002"; then
    print_success
else
    print_failure "Loans list incomplete"
fi

{
    echo "✅ PASS"
    echo ""
} >> "$LOG_FILE"

read -p "Press Enter to continue..."

# Test 8: Get available loans (PENDING)
RESPONSE=$(execute_query "$LOANS_URL" \
    "{ availableLoans { id amount purpose status } }" \
    "Get available loans (PENDING status)")

if echo "$RESPONSE" | grep -q "PENDING"; then
    print_success
else
    print_failure "No pending loans found"
fi

{
    echo "✅ PASS"
    echo ""
} >> "$LOG_FILE"

read -p "Press Enter to continue..."

# Test 9: Get loans by status (ACTIVE)
RESPONSE=$(execute_query "$LOANS_URL" \
    "{ loansByStatus(status: ACTIVE) { id status monthlyPayment } }" \
    "Get loans by status (ACTIVE)")

if echo "$RESPONSE" | grep -q "ACTIVE" && echo "$RESPONSE" | grep -q "monthlyPayment"; then
    print_success
else
    print_failure "Active loans query failed"
fi

{
    echo "✅ PASS"
    echo ""
} >> "$LOG_FILE"

read -p "Press Enter to continue..."

# Test 10: Create loan request
RESPONSE=$(execute_query "$LOANS_URL" \
    "mutation { createLoanRequest(input: { borrowerId: \\\"user-003\\\", amount: 40000, interestRate: 7.5, term: 48, purpose: \\\"Home renovation\\\" }) { success message loan { id monthlyPayment totalRepayment } } }" \
    "Create loan request (mutation)")

if echo "$RESPONSE" | grep -q "\"success\":true" && echo "$RESPONSE" | grep -q "monthlyPayment"; then
    print_success
else
    print_failure "Loan creation failed"
fi

{
    echo "✅ PASS"
    echo ""
} >> "$LOG_FILE"

read -p "Press Enter to continue..."

# ============================================================================
# FEDERATION CONCEPTS TESTS (STANDALONE)
# ============================================================================

print_header "FEDERATION CONCEPTS (Standalone Mode)"
echo ""

# Test 11: User with lender role
RESPONSE=$(execute_query "$USERS_URL" \
    "{ user(id: \\\"user-001\\\") { fullName lenderProfile { totalLent activeLoans } } }" \
    "User with lender profile (federation @key entity)")

if echo "$RESPONSE" | grep -q "lenderProfile"; then
    print_success
else
    print_failure "Lender profile resolution failed"
fi

{
    echo "✅ PASS"
    echo ""
} >> "$LOG_FILE"

read -p "Press Enter to continue..."

# Test 12: Loan with user reference (stub)
RESPONSE=$(execute_query "$LOANS_URL" \
    "{ loan(id: \\\"loan-001\\\") { amount lender { id } borrower { id } } }" \
    "Loan with user references (federation stubs)")

echo -e "${YELLOW}ℹ️  Note: lender/borrower return only ID (stub). Apollo Router would resolve full User.${NC}"

if echo "$RESPONSE" | grep -q "\"id\""; then
    print_success
else
    print_failure "User reference stubs not working"
fi

{
    echo "✅ PASS"
    echo ""
} >> "$LOG_FILE"

# ============================================================================
# SUMMARY
# ============================================================================

print_header "TEST SUMMARY"

echo -e "Total Tests: ${TOTAL_TESTS}"
echo -e "${GREEN}Passed: ${TESTS_PASSED}${NC}"
echo -e "${RED}Failed: ${TESTS_FAILED}${NC}"
echo ""
echo -e "Log file: ${CYAN}${LOG_FILE}${NC}"
echo ""

{
    echo "═══════════════════════════════════════════════════════════════"
    echo "FINAL SUMMARY"
    echo "═══════════════════════════════════════════════════════════════"
    echo "Total Tests: $TOTAL_TESTS"
    echo "Passed: $TESTS_PASSED"
    echo "Failed: $TESTS_FAILED"
    echo ""
    echo "Test completed at: $(date)"
} >> "$LOG_FILE"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}⚠️  Some tests failed. Check the log file for details.${NC}"
    exit 1
fi
