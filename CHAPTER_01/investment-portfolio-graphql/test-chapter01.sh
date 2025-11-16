#!/usr/bin/env bash

################################################################################
# CHAPTER 01: FUNDAMENTOS DE GRAPHQL Y CONTEXTO CORPORATIVO
# Script de Testing Automatizado - VERSIÓN PORTABLE
#
# Compatible con:
#   - macOS (Bash 3.2+)
#   - Linux (Bash 4.0+)
#   - Windows GitBash (Bash 4.4+)
#
# Uso: 
#   ./test-chapter01.sh           (modo interactivo)
#   ./test-chapter01.sh -s        (modo silencioso)
################################################################################

# Forzar locale consistente para comandos de fecha
export LC_ALL=C

# Colores (usando printf en lugar de echo -e para compatibilidad Mac)
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
MAGENTA='\033[1;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# Config
BASE_URL="http://localhost:8080"
GRAPHQL_ENDPOINT="${BASE_URL}/graphql"
REST_ENDPOINT="${BASE_URL}/api/rest"
OUTPUT_FILE="test-results-$(date +%Y%m%d-%H%M%S).txt"

INTERACTIVE=true
if [ "$1" = "-s" ]; then  # Cambiado de == a = para POSIX
    INTERACTIVE=false
fi

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Función para imprimir con colores (compatible con Mac y Linux)
print_colored() {
    printf "%b\n" "$1" | tee -a "$OUTPUT_FILE"
}

pause() {
    if [ "$INTERACTIVE" = true ]; then
        print_colored "${YELLOW}⏸️  Presiona Enter para continuar...${NC}"
        read -r
    else
        sleep 0.5
    fi
}

log() {
    print_colored "$1"
}

print_section() {
    log ""
    log "${CYAN}================================================================================${NC}"
    log "${CYAN}$1${NC}"
    log "${CYAN}================================================================================${NC}"
    log ""
}

print_subsection() {
    log ""
    log "${MAGENTA}────────────────────────────────────────────────────────────────────────────${NC}"
    log "${MAGENTA}$1${NC}"
    log "${MAGENTA}────────────────────────────────────────────────────────────────────────────${NC}"
}

run_test() {
    local test_name="$1"
    local curl_command="$2"
    local validation="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log "${YELLOW}🧪 Test #${TOTAL_TESTS}: ${test_name}${NC}"
    log "   Ejecutando..."
    
    response=$(eval "$curl_command" 2>&1)
    exit_code=$?
    
    # Substring portátil (funciona en Mac y Linux)
    if [ ${#response} -gt 200 ]; then
        log "   📄 Respuesta: ${response:0:200}..."
    else
        log "   📄 Respuesta: $response"
    fi
    
    # grep -E es más portable que grep -P
    if [ $exit_code -eq 0 ] && echo "$response" | grep -qE "$validation"; then
        log "   ${GREEN}✅ PASSED${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        log "   ${RED}❌ FAILED${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    log ""
    pause
}

run_graphql_test() {
    local test_name="$1"
    local query="$2"
    local validation="$3"
    
    local curl_cmd="curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\": \"$query\"}'"
    run_test "$test_name" "$curl_cmd" "$validation"
}

log "================================================================================"
log "CHAPTER 01: FUNDAMENTOS DE GRAPHQL - TEST SUITE"
log "================================================================================"
log "Fecha: $(date)"
log "Modo: $([ "$INTERACTIVE" = true ] && echo "Interactivo" || echo "Silencioso")"
log "Sistema: $(uname -s)"
log "================================================================================"
log ""

log "${YELLOW}🔍 Verificando servidor...${NC}"
if ! curl -s "${BASE_URL}" > /dev/null 2>&1; then
    log "${RED}❌ ERROR: Servidor no disponible en ${BASE_URL}${NC}"
    log "${YELLOW}💡 Inicia el servidor: mvn spring-boot:run${NC}"
    exit 1
fi
log "${GREEN}✅ Servidor OK${NC}"
log ""
pause

################################################################################
# SECCIÓN 1.1 - DE REST A GRAPHQL
################################################################################
print_section "SECCIÓN 1.1 — DE REST A GRAPHQL (30 min)"
log "Objetivo: Comparar REST vs GraphQL, overfetching/underfetching"

print_subsection "1.1.1 - REST: Overfetching"
run_test \
    "REST devuelve TODO (assets + performance)" \
    "curl -s ${REST_ENDPOINT}/portfolios" \
    '"assets".*"performance"'

print_subsection "1.1.2 - GraphQL: Solo lo necesario"
run_graphql_test \
    "GraphQL - Solo nombres (sin overfetching)" \
    "{ myPortfolios { name } }" \
    '"name"'

print_subsection "1.1.3 - REST: Underfetching (3 llamadas)"
log "${YELLOW}📝 REST necesita 3 llamadas HTTP para portfolio + assets + performance${NC}"
run_test \
    "REST - Llamada 1: Portfolio" \
    "curl -s ${REST_ENDPOINT}/portfolios/portfolio-001" \
    '"id":"portfolio-001"'

run_test \
    "REST - Llamada 2: Assets" \
    "curl -s ${REST_ENDPOINT}/portfolios/portfolio-001/assets" \
    '"symbol"'

run_test \
    "REST - Llamada 3: Performance" \
    "curl -s ${REST_ENDPOINT}/portfolios/portfolio-001/performance" \
    '"totalReturn"'

print_subsection "1.1.4 - GraphQL: Una sola llamada"
run_graphql_test \
    "GraphQL - Portfolio + Assets + Performance en UNA query" \
    "{ portfolio(id: \\\"portfolio-001\\\") { name assets { symbol } performance { totalReturn } } }" \
    '"portfolio".*"assets".*"performance"'

print_subsection "1.1.5 - Endpoint único"
log "${YELLOW}📝 GraphQL usa UN endpoint (/graphql) vs múltiples REST${NC}"
run_graphql_test \
    "GraphQL - Endpoint único /graphql" \
    "{ myPortfolios { id } }" \
    '"myPortfolios"'

################################################################################
# SECCIÓN 1.2 - COMPONENTES Y LENGUAJE BASE
################################################################################
print_section "SECCIÓN 1.2 — COMPONENTES Y LENGUAJE BASE (30 min)"
log "Objetivo: Schema, Types, Queries, Mutations, Resolvers"

print_subsection "1.2.1 - Schema: Types"
run_graphql_test \
    "Portfolio type según schema" \
    "{ portfolio(id: \\\"portfolio-001\\\") { id name totalValue } }" \
    '"id".*"name".*"totalValue"'

print_subsection "1.2.2 - Queries"
run_graphql_test \
    "Query simple: myPortfolios" \
    "{ myPortfolios { id name } }" \
    '"myPortfolios"'

run_graphql_test \
    "Query con argumento: portfolio(id)" \
    "{ portfolio(id: \\\"portfolio-001\\\") { name } }" \
    '"Growth Portfolio"'

run_graphql_test \
    "Query de búsqueda: searchAsset" \
    "{ searchAsset(symbol: \\\"AAPL\\\") { name currentPrice } }" \
    '"Apple Inc'

print_subsection "1.2.3 - Mutations"
run_graphql_test \
    "Mutation: createPortfolio" \
    "mutation { createPortfolio(input: {name: \\\"Test Portfolio\\\"}) { success portfolio { id } } }" \
    '"success":true'

# Extracción de ID compatible con Mac y Linux
CREATED_ID=$(curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' \
    -d '{"query": "mutation { createPortfolio(input: {name: \"Temp\"}) { portfolio { id } } }"}' \
    | sed -n 's/.*"id":"\([^"]*\)".*/\1/p' | head -1)

if [ -n "$CREATED_ID" ]; then
    run_graphql_test \
        "Mutation: addAsset" \
        "mutation { addAsset(input: {portfolioId: \\\"${CREATED_ID}\\\", symbol: \\\"NVDA\\\", assetType: STOCK, quantity: 10, buyPrice: 500}) { success } }" \
        '"success":true'
fi

run_graphql_test \
    "Mutation: removeAsset" \
    "mutation { removeAsset(portfolioId: \\\"portfolio-001\\\", assetId: \\\"fake\\\") { success } }" \
    '"success"'

print_subsection "1.2.4 - Enums"
run_graphql_test \
    "Enum AssetType" \
    "{ portfolio(id: \\\"portfolio-001\\\") { assets { assetType } } }" \
    '"assetType"'

print_subsection "1.2.5 - Resolvers"
log "${YELLOW}📝 Resolvers conectan queries con datos (MockDataService)${NC}"
run_graphql_test \
    "Resolvers obtienen datos del servicio" \
    "{ myPortfolios { ownerName } }" \
    '"Carlos Mendoza"'

################################################################################
# SECCIÓN 1.3 - CONSULTAS ANIDADAS Y VARIABLES
################################################################################
print_section "SECCIÓN 1.3 — CONSULTAS ANIDADAS Y VARIABLES (30 min)"
log "Objetivo: Anidación y parametrización con variables"

print_subsection "1.3.1 - Anidación nivel 1"
run_graphql_test \
    "Portfolio con assets anidados" \
    "{ portfolio(id: \\\"portfolio-001\\\") { name assets { symbol } } }" \
    '"assets"'

print_subsection "1.3.2 - Anidación nivel 2"
run_graphql_test \
    "Portfolio → Performance → BestPerformer (3 niveles)" \
    "{ portfolio(id: \\\"portfolio-001\\\") { performance { bestPerformer { symbol } } } }" \
    '"bestPerformer"'

print_subsection "1.3.3 - Variables"
run_test \
    "Query con variable \$id" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"query(\$id:ID!){portfolio(id:\$id){name}}\",\"variables\":{\"id\":\"portfolio-001\"}}'" \
    '"Growth Portfolio"'

print_subsection "1.3.4 - Validación de variables"
log "${YELLOW}📝 Variables validadas automáticamente (tipado fuerte)${NC}"
run_test \
    "Variable String! validada" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"query(\$s:String!){searchAsset(symbol:\$s){name}}\",\"variables\":{\"s\":\"BTC\"}}'" \
    '"Bitcoin"'

print_subsection "1.3.5 - Múltiples relaciones"
run_graphql_test \
    "Assets Y Performance en una query" \
    "{ portfolio(id: \\\"portfolio-002\\\") { name assets { symbol } performance { yearReturn } } }" \
    '"Retirement Fund"'

################################################################################
# SECCIÓN 1.4 - FILTROS, ORDEN Y PAGINACIÓN
################################################################################
print_section "SECCIÓN 1.4 — FILTROS, ORDEN Y PAGINACIÓN (30 min)"
log "Objetivo: Filtrado, ordenamiento y paginación cursor-based"

print_subsection "1.4.1 - Filtrado por tipo"
run_graphql_test \
    "Filtrar solo STOCKS" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", filter: {assetType: STOCK}) { totalCount } }" \
    '"totalCount":2'

print_subsection "1.4.2 - Filtrado por rango"
run_graphql_test \
    "Assets con valor > \$5000" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", filter: {minValue: 5000}) { totalCount } }" \
    '"totalCount":[0-9]'

print_subsection "1.4.3 - Ordenamiento DESC"
run_graphql_test \
    "Ordenar por valor (mayor primero)" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", sort: {field: TOTAL_VALUE, direction: DESC}) { edges { node { symbol } } } }" \
    '"edges"'

print_subsection "1.4.4 - Ordenar por profit"
run_graphql_test \
    "Ordenar por ganancia/pérdida %" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", sort: {field: PROFIT_LOSS_PERCENT, direction: DESC}) { edges { node { profitLossPercent } } } }" \
    '"profitLossPercent"'

print_subsection "1.4.5 - Paginación cursor-based"
run_graphql_test \
    "Primera página (limit: 2)" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", pagination: {limit: 2}) { pageInfo { hasNextPage } } }" \
    '"hasNextPage"'

print_subsection "1.4.6 - Filtro + Orden + Paginación"
run_graphql_test \
    "Query compleja: filtro + orden + paginación" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", filter: {assetType: STOCK}, sort: {field: TOTAL_VALUE, direction: DESC}, pagination: {limit: 2}) { totalCount } }" \
    '"totalCount"'

print_subsection "1.4.7 - Límites controlados"
log "${YELLOW}📝 Backend limita resultados para evitar queries costosas${NC}"
run_graphql_test \
    "Paginación con límite razonable" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", pagination: {limit: 10}) { edges { node { symbol } } } }" \
    '"edges"'

################################################################################
# SECCIÓN 1.5 - TIPADO Y SEGURIDAD
################################################################################
print_section "SECCIÓN 1.5 — TIPADO, NULLABILIDAD Y SEGURIDAD (30 min)"
log "Objetivo: Validación automática y seguridad"

print_subsection "1.5.1 - Validación de tipos"
run_graphql_test \
    "Float! retorna número válido" \
    "{ portfolio(id: \\\"portfolio-001\\\") { totalValue } }" \
    '"totalValue":[0-9]'

print_subsection "1.5.2 - Non-nullable (!)"
log "${YELLOW}📝 Campos con ! nunca retornan null${NC}"
run_graphql_test \
    "ID!, String!, Float! siempre presentes" \
    "{ portfolio(id: \\\"portfolio-001\\\") { id name totalValue } }" \
    '"id":"portfolio-001"'

print_subsection "1.5.3 - Enum validation"
log "${YELLOW}📝 AssetType: STOCK, CRYPTO, ETF, BOND, COMMODITY${NC}"
run_graphql_test \
    "Enum valida valores automáticamente" \
    "{ portfolio(id: \\\"portfolio-001\\\") { assets { assetType } } }" \
    '"assetType":"(STOCK|CRYPTO|ETF)"'

print_subsection "1.5.4 - Introspection"
run_graphql_test \
    "Schema introspection (auto-documentación)" \
    "{ __schema { types { name } } }" \
    '"__schema"'

print_subsection "1.5.5 - Validación de argumentos"
log "${YELLOW}📝 GraphQL valida argumentos antes de ejecutar${NC}"
run_test \
    "Argumento ID! obligatorio" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\": \"{ portfolio { name } }\"}'" \
    'error'

print_subsection "1.5.6 - Respuesta consistente"
run_graphql_test \
    "Mutations: estructura success/message/data" \
    "mutation { createPortfolio(input: {name: \\\"Final\\\"}) { success message } }" \
    '"success".*"message"'

################################################################################
# RESUMEN FINAL
################################################################################
print_section "📊 RESUMEN DE RESULTADOS"

log "${CYAN}Total de tests ejecutados: ${TOTAL_TESTS}${NC}"
log "${GREEN}✅ Tests exitosos: ${PASSED_TESTS}${NC}"
log "${RED}❌ Tests fallidos: ${FAILED_TESTS}${NC}"
log ""
log "${YELLOW}📄 Resultados guardados en: ${OUTPUT_FILE}${NC}"
log ""

if [ $FAILED_TESTS -eq 0 ]; then
    log "${GREEN}🎉 ¡TODOS LOS TESTS PASARON! Chapter 01 completo y funcional.${NC}"
    log ""
    log "${CYAN}Cobertura del temario:${NC}"
    log "  ✅ Sección 1.1 - REST vs GraphQL (7 tests)"
    log "  ✅ Sección 1.2 - Componentes base (9 tests)"
    log "  ✅ Sección 1.3 - Anidación y variables (5 tests)"
    log "  ✅ Sección 1.4 - Filtros y paginación (7 tests)"
    log "  ✅ Sección 1.5 - Tipado y seguridad (6 tests)"
    log ""
    log "${CYAN}TOTAL: 34 tests automatizados${NC}"
    log ""
    exit 0
else
    log "${RED}⚠️  Algunos tests fallaron. Revisa el output arriba.${NC}"
    log ""
    exit 1
fi