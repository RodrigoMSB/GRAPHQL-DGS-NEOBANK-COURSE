#!/bin/bash

# ============================================================================
# START SCRIPT - CHAPTER 05: Federation with Apollo Router
# ============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_header() {
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║  $1${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
}

# ============================================================================
# STEP 1: PRE-FLIGHT CHECKS
# ============================================================================

print_header "PRE-FLIGHT CHECKS"

# Check Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker is installed${NC}"

# Check Docker Compose
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${RED}❌ Docker Compose is not installed${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker Compose is installed${NC}"

# Check if ports are available
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo -e "${YELLOW}⚠️  Port 8080 is in use. Apollo Router may fail to start.${NC}"
fi

if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo -e "${YELLOW}⚠️  Port 8081 is in use. Users Service may fail to start.${NC}"
fi

if lsof -Pi :8082 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo -e "${YELLOW}⚠️  Port 8082 is in use. Loans Service may fail to start.${NC}"
fi

echo ""

# ============================================================================
# STEP 2: BUILD AND START SERVICES
# ============================================================================

print_header "BUILDING AND STARTING SERVICES"

echo -e "${BLUE}Building Docker images...${NC}"
docker-compose build --no-cache

echo ""
echo -e "${BLUE}Starting services...${NC}"
docker-compose up -d

echo ""
echo -e "${GREEN}✅ Services started!${NC}"
echo ""

# ============================================================================
# STEP 3: WAIT FOR SERVICES TO BE READY
# ============================================================================

print_header "WAITING FOR SERVICES TO BE READY"

echo -e "${YELLOW}This may take 30-60 seconds...${NC}"
echo ""

# Wait for users-service
echo -e "${CYAN}Waiting for Users Service (8081)...${NC}"
until curl -s http://localhost:8081/graphql > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e "${GREEN}✅ Users Service is ready!${NC}"

# Wait for loans-service
echo -e "${CYAN}Waiting for Loans Service (8082)...${NC}"
until curl -s http://localhost:8082/graphql > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e "${GREEN}✅ Loans Service is ready!${NC}"

# Wait for apollo-router
echo -e "${CYAN}Waiting for Apollo Router (8080)...${NC}"
until curl -s http://localhost:8080/health > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e "${GREEN}✅ Apollo Router is ready!${NC}"

echo ""

# ============================================================================
# STEP 4: TEST FEDERATION
# ============================================================================

print_header "TESTING FEDERATION"

echo -e "${CYAN}Testing federated query...${NC}"
echo ""

QUERY='{"query":"{ user(id: \"user-001\") { id fullName email loansAsBorrower { id amount status } } }"}'

RESPONSE=$(curl -s -X POST http://localhost:8080/graphql \
    -H "Content-Type: application/json" \
    -d "$QUERY")

if echo "$RESPONSE" | grep -q "Alice Thompson"; then
    echo -e "${GREEN}✅ Federation is working!${NC}"
    echo ""
    echo -e "${BLUE}Response:${NC}"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
else
    echo -e "${RED}❌ Federation test failed${NC}"
    echo "$RESPONSE"
fi

echo ""

# ============================================================================
# SUMMARY
# ============================================================================

print_header "SERVICES READY"

echo ""
echo -e "${GREEN}All services are up and running!${NC}"
echo ""
echo -e "${CYAN}Available endpoints:${NC}"
echo -e "  ${YELLOW}Apollo Router (Unified):${NC}  http://localhost:8080/graphql"
echo -e "  ${YELLOW}Users Service:${NC}            http://localhost:8081/graphql"
echo -e "  ${YELLOW}Loans Service:${NC}            http://localhost:8082/graphql"
echo ""
echo -e "${CYAN}GraphQL Playground:${NC}"
echo -e "  ${YELLOW}Apollo Router:${NC}            http://localhost:8080/"
echo -e "  ${YELLOW}Users Service:${NC}            http://localhost:8081/graphiql"
echo -e "  ${YELLOW}Loans Service:${NC}            http://localhost:8082/graphiql"
echo ""
echo -e "${CYAN}Useful commands:${NC}"
echo -e "  ${YELLOW}View logs:${NC}                docker-compose logs -f"
echo -e "  ${YELLOW}Stop services:${NC}            docker-compose down"
echo -e "  ${YELLOW}Restart services:${NC}         docker-compose restart"
echo ""
echo -e "${GREEN}Happy testing! 🚀${NC}"
echo ""
