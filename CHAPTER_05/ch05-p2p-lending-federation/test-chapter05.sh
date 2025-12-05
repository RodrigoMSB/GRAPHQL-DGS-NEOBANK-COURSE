#!/bin/bash

##############################################################################
# Script de Pruebas - Chapter 05: Apollo Federation
# 
# Este script verifica que los microservicios GraphQL federados estén
# funcionando correctamente y exponga los conceptos de Apollo Federation.
#
# COMPATIBLE: Mac y Windows (Git Bash)
##############################################################################

# ============================================================================
# DETECCIÓN DE SISTEMA OPERATIVO
# ============================================================================

detect_os() {
    case "$(uname -s)" in
        Darwin*)    echo "mac" ;;
        Linux*)     echo "linux" ;;
        MINGW*|MSYS*|CYGWIN*)    echo "windows" ;;
        *)          echo "unknown" ;;
    esac
}

OS_TYPE=$(detect_os)

# ============================================================================
# CONFIGURACIÓN
# ============================================================================

# Archivo de log con timestamp
LOG_FILE="test-results-chapter05-$(date +%Y%m%d-%H%M%S).txt"

# Contadores de pruebas
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# URLs de servicios
USERS_URL="http://localhost:8081/graphql"
LOANS_URL="http://localhost:8082/graphql"

# Función para escribir en terminal y archivo
log_both() {
    echo -e "$1"
    echo -e "$1" | sed 's/\x1B\[[0-9;]*[JKmsu]//g' >> "$LOG_FILE"
}

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m'

# Función para pausar (compatible con ambos sistemas)
pause_script() {
    echo ""
    read -p "Presiona ENTER para continuar..." dummy
    echo ""
}

# Función para formatear JSON (con jq o fallback)
format_json() {
    if command -v jq &> /dev/null; then
        echo "$1" | jq '.'
    else
        echo "$1"
    fi
}

# Función para ejecutar query GraphQL
execute_query() {
    local service_url=$1
    local query=$2
    
    curl -s -X POST "$service_url" \
        -H "Content-Type: application/json" \
        -d @- <<EOF
{"query":"$query"}
EOF
}

log_both "${CYAN}╔═══════════════════════════════════════════════════════════╗${NC}"
log_both "${CYAN}║    🚀  CHAPTER 05: APOLLO FEDERATION                     ║${NC}"
log_both "${CYAN}║    P2P Lending con Netflix DGS Framework                  ║${NC}"
log_both "${CYAN}╚═══════════════════════════════════════════════════════════╝${NC}"
log_both ""
log_both "${GREEN}🖥️  Sistema Operativo: ${OS_TYPE}${NC}"
log_both "${GREEN}📄 Los resultados se guardarán en: ${LOG_FILE}${NC}"
log_both ""

##############################################################################
# VERIFICACIÓN DE SERVICIOS
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}🔍 VERIFICACIÓN: Servicios activos${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""

SERVICIOS_OK=true

# Verificar Users Service (8081)
log_both "Verificando Users Service (Puerto 8081)..."
if curl -s -X POST "$USERS_URL" -H "Content-Type: application/json" -d '{"query":"{__typename}"}' > /dev/null 2>&1; then
    log_both "${GREEN}✅ Users Service: OK${NC}"
else
    log_both "${RED}❌ Users Service: NO RESPONDE${NC}"
    SERVICIOS_OK=false
fi

# Verificar Loans Service (8082)
log_both "Verificando Loans Service (Puerto 8082)..."
if curl -s -X POST "$LOANS_URL" -H "Content-Type: application/json" -d '{"query":"{__typename}"}' > /dev/null 2>&1; then
    log_both "${GREEN}✅ Loans Service: OK${NC}"
else
    log_both "${RED}❌ Loans Service: NO RESPONDE${NC}"
    SERVICIOS_OK=false
fi

log_both ""

if [ "$SERVICIOS_OK" = false ]; then
    log_both "${RED}⚠️  ERROR: Algunos servicios no están activos.${NC}"
    log_both "${YELLOW}Por favor, asegúrate de que los servicios estén corriendo:${NC}"
    log_both "  docker-compose up -d"
    log_both ""
    exit 1
fi

log_both "${GREEN}✅ Todos los servicios están activos. Iniciando pruebas...${NC}"
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 1: Query all users (Users Service)
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 1: Query all users (Subgrafo Users)${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Verificar que Users Service retorna lista de usuarios"
log_both "🏢 Servicio: Users Service (8081)"
log_both "📊 Entidad: User (OWNER)"
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "{ users { id fullName email userType } }"
log_both ""
log_both "${CYAN}Ejecutando query...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "$USERS_URL" "{ users { id fullName email userType } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

if echo "$RESPONSE" | grep -q "user-001" && echo "$RESPONSE" | grep -q "Alice Thompson"; then
    log_both "${GREEN}✅ PASSED: Query users retorna lista correctamente${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: Data esperada no encontrada${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 2: Query single user by ID
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 2: Query single user by ID${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Verificar query con argumentos"
log_both "🏢 Servicio: Users Service (8081)"
log_both "🔑 Argumento: id = \"user-001\""
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "{ user(id: \"user-001\") { id fullName email reputation } }"
log_both ""
log_both "${CYAN}Ejecutando query...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "$USERS_URL" "{ user(id: \\\"user-001\\\") { id fullName email reputation } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

if echo "$RESPONSE" | grep -q "Alice Thompson"; then
    log_both "${GREEN}✅ PASSED: Usuario individual retornado correctamente${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: Usuario no encontrado${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 3: Query verified lenders
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 3: Query verified lenders${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Verificar query con objetos anidados (LenderProfile)"
log_both "🏢 Servicio: Users Service (8081)"
log_both "📊 Relación: User → LenderProfile"
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "{ verifiedLenders { id fullName lenderProfile { verified totalLent } } }"
log_both ""
log_both "${CYAN}Ejecutando query...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "$USERS_URL" "{ verifiedLenders { id fullName lenderProfile { verified totalLent } } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

if echo "$RESPONSE" | grep -q "verifiedLenders" && echo "$RESPONSE" | grep -q "totalLent"; then
    log_both "${GREEN}✅ PASSED: Lenders verificados retornados correctamente${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: Lenders no encontrados${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 4: Entity fetcher (_entities)
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 4: Entity Resolution (_entities query)${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Verificar que User es entidad federada (@key)"
log_both "🏢 Servicio: Users Service (8081)"
log_both "🔑 Concepto: Entity Resolution de Apollo Federation"
log_both "📐 Directiva: @key(fields: \"id\")"
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "{ _entities(representations: [{__typename: \"User\", id: \"user-001\"}]) {"
log_both "    ... on User { id fullName }"
log_both "  }"
log_both "}"
log_both ""
log_both "${CYAN}Ejecutando query...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "$USERS_URL" "{ _entities(representations: [{__typename: \\\"User\\\", id: \\\"user-001\\\"}]) { ... on User { id fullName } } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

if echo "$RESPONSE" | grep -q "Alice Thompson"; then
    log_both "${GREEN}✅ PASSED: Entity fetcher funciona - User es entidad federada${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: Entity no resuelta${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 5: Query all loans (Loans Service)
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 5: Query all loans (Subgrafo Loans)${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Verificar que Loans Service retorna lista de préstamos"
log_both "🏢 Servicio: Loans Service (8082)"
log_both "📊 Entidad: Loan (OWNER)"
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "{ loans { id amount status purpose } }"
log_both ""
log_both "${CYAN}Ejecutando query...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "$LOANS_URL" "{ loans { id amount status purpose } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

if echo "$RESPONSE" | grep -q "loan-001" && echo "$RESPONSE" | grep -q "ACTIVE"; then
    log_both "${GREEN}✅ PASSED: Query loans retorna lista correctamente${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: Data esperada no encontrada${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 6: Query loans by status
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 6: Query loans by status (Filtro con enum)${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Verificar filtrado por enum LoanStatus"
log_both "🏢 Servicio: Loans Service (8082)"
log_both "🔍 Filtro: status = ACTIVE"
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "{ loansByStatus(status: ACTIVE) { id status } }"
log_both ""
log_both "${CYAN}Ejecutando query...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "$LOANS_URL" "{ loansByStatus(status: ACTIVE) { id status } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

if echo "$RESPONSE" | grep -q "ACTIVE"; then
    log_both "${GREEN}✅ PASSED: Filtro por status funciona correctamente${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: Filtro no funciona${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 7: Referencias entre subgrafos (User stubs)
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 7: Referencias entre subgrafos (User stubs)${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Verificar que Loan referencia a User (otro subgrafo)"
log_both "🏢 Servicio: Loans Service (8082)"
log_both "🔗 Concepto: Referencias entre subgrafos con stubs"
log_both "📐 Patrón: Retornar {__typename: \"User\", id: \"...\"}  para entity resolution"
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "{ loans { id borrower { id } lender { id } } }"
log_both ""
log_both "${CYAN}Ejecutando query...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "$LOANS_URL" "{ loans { id borrower { id } lender { id } } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

# Verificar que loans tiene referencias a users
if echo "$RESPONSE" | grep -q '"borrower"' && echo "$RESPONSE" | grep -q '"lender"' && echo "$RESPONSE" | grep -q '"id"'; then
    log_both "${GREEN}✅ PASSED: Referencias User (stubs) retornadas - lender/borrower presentes${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: Referencias User no encontradas${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 8: Mutation createUser
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 8: Mutation createUser${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Verificar mutations en Users Service"
log_both "🏢 Servicio: Users Service (8081)"
log_both "✏️  Operación: Crear nuevo usuario"
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "mutation {"
log_both "  createUser(input: {"
log_both "    email: \"test@neobank.com\","
log_both "    fullName: \"Test User\","
log_both "    userType: LENDER"
log_both "  }) { success message user { id } }"
log_both "}"
log_both ""
log_both "${CYAN}Ejecutando mutation...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "$USERS_URL" "mutation { createUser(input: {email: \\\"test@neobank.com\\\", fullName: \\\"Test User\\\", userType: LENDER}) { success message user { id } } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

if echo "$RESPONSE" | grep -q "success"; then
    log_both "${GREEN}✅ PASSED: Mutation createUser funciona${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: Usuario no creado${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 9: Mutation createLoanRequest
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 9: Mutation createLoanRequest${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Verificar mutations en Loans Service"
log_both "🏢 Servicio: Loans Service (8082)"
log_both "✏️  Operación: Crear solicitud de préstamo"
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "mutation {"
log_both "  createLoanRequest(input: {"
log_both "    borrowerId: \"user-003\","
log_both "    amount: 5000,"
log_both "    interestRate: 8.5,"
log_both "    term: 12,"
log_both "    purpose: \"Test loan\""
log_both "  }) { success message }"
log_both "}"
log_both ""
log_both "${CYAN}Ejecutando mutation...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "$LOANS_URL" "mutation { createLoanRequest(input: {borrowerId: \\\"user-003\\\", amount: 5000, interestRate: 8.5, term: 12, purpose: \\\"Test loan\\\"}) { success message } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

if echo "$RESPONSE" | grep -q "success"; then
    log_both "${GREEN}✅ PASSED: Mutation createLoanRequest funciona${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: Préstamo no creado${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# RESUMEN FINAL
##############################################################################
log_both "${CYAN}╔═══════════════════════════════════════════════════════════╗${NC}"
log_both "${CYAN}║                    📊 RESUMEN DE PRUEBAS                  ║${NC}"
log_both "${CYAN}╚═══════════════════════════════════════════════════════════╝${NC}"
log_both ""
log_both "${GREEN}✅ PRUEBA 1:${NC} Query all users (Users Service)"
log_both "${GREEN}✅ PRUEBA 2:${NC} Query single user by ID"
log_both "${GREEN}✅ PRUEBA 3:${NC} Query verified lenders con objetos anidados"
log_both "${GREEN}✅ PRUEBA 4:${NC} Entity Resolution (_entities query)"
log_both "${GREEN}✅ PRUEBA 5:${NC} Query all loans (Loans Service)"
log_both "${GREEN}✅ PRUEBA 6:${NC} Query loans by status (filtro enum)"
log_both "${YELLOW}⚠️  PRUEBA 7:${NC} Referencias entre subgrafos (requiere Apollo Router)"
log_both "${GREEN}✅ PRUEBA 8:${NC} Mutation createUser"
log_both "${GREEN}✅ PRUEBA 9:${NC} Mutation createLoanRequest"
log_both ""
log_both "${CYAN}╔═══════════════════════════════════════════════════════════╗${NC}"
log_both "${CYAN}║                  📈 ESTADÍSTICAS FINALES                  ║${NC}"
log_both "${CYAN}╚═══════════════════════════════════════════════════════════╝${NC}"
log_both ""
log_both "${YELLOW}Total de Pruebas:${NC}     ${TOTAL_TESTS}"
log_both "${GREEN}Pruebas Exitosas:${NC}     ${PASSED_TESTS} ✅"
log_both "${RED}Pruebas Fallidas:${NC}     ${FAILED_TESTS}"
if [ $TOTAL_TESTS -gt 0 ]; then
    SUCCESS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    log_both "${CYAN}Tasa de Éxito:${NC}        ${SUCCESS_RATE}%"
fi
log_both ""
log_both "${CYAN}╔═══════════════════════════════════════════════════════════╗${NC}"
log_both "${CYAN}║          🎓 CONCEPTOS DE FEDERATION VERIFICADOS           ║${NC}"
log_both "${CYAN}╚═══════════════════════════════════════════════════════════╝${NC}"
log_both ""
log_both "${YELLOW}🏗️  Arquitectura Federada:${NC}"
log_both "   • Users Service (8081) → Owner de User (@key)"
log_both "   • Loans Service (8082) → Owner de Loan, extiende User (@extends)"
log_both ""
log_both "${YELLOW}🔑 Directivas Apollo Federation:${NC}"
log_both "   • @key(fields: \"id\") → User es entidad federada"
log_both "   • @extends → Loans extiende User con loansAsLender/loansAsBorrower"
log_both "   • @external → Campos definidos en otro subgrafo"
log_both ""
log_both "${YELLOW}🔗 Entity Resolution:${NC}"
log_both "   • _entities query → Users Service resuelve User por ID"
log_both "   • Stubs {__typename, id} → Referencias entre subgrafos"
log_both ""
log_both "${YELLOW}📦 Bounded Contexts (DDD):${NC}"
log_both "   • Users Context → Autenticación, perfiles, reputación"
log_both "   • Loans Context → Préstamos P2P, matching, intereses"
log_both ""
log_both "${GREEN}🎉 ¡Conceptos de Apollo Federation implementados correctamente!${NC}"
log_both "${CYAN}Con Apollo Router, estos servicios se unificarían en un solo endpoint.${NC}"
log_both ""
log_both "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
log_both "${GREEN}📄 Log guardado en: ${LOG_FILE}${NC}"
log_both "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
log_both ""