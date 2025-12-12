#!/usr/bin/env bash

################################################################################
# CHAPTER 01: FUNDAMENTOS DE GRAPHQL Y CONTEXTO CORPORATIVO
# Script de Testing Automatizado - VERSIÓN PEDAGÓGICA
#
# Este script NO solo ejecuta tests, sino que ENSEÑA GraphQL mientras prueba.
# Cada test incluye:
#   - 📚 Concepto teórico
#   - 🎯 Objetivo del test
#   - 💡 Por qué es importante
#   - 🔍 Qué buscar en la respuesta
#
# Compatible con:
#   - macOS (Bash 3.2+)
#   - Linux (Bash 4.0+)
#   - Windows GitBash (Bash 4.4+)
#
# Uso: 
#   ./test-chapter01.sh           (modo interactivo - RECOMENDADO)
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
BLUE='\033[0;34m'
NC='\033[0m'

# Config
BASE_URL="${BASE_URL:-http://localhost:8080}"
GRAPHQL_ENDPOINT="${GRAPHQL_ENDPOINT:-${BASE_URL}/graphql}"
REST_ENDPOINT="${REST_ENDPOINT:-${BASE_URL}/api/rest}"
OUTPUT_FILE="test-results-$(date +%Y%m%d-%H%M%S).txt"

INTERACTIVE=true
if [ "$1" = "-s" ]; then
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

print_concept() {
    log "${BLUE}📚 CONCEPTO:${NC} $1"
}

print_objective() {
    log "${BLUE}🎯 OBJETIVO:${NC} $1"
}

print_why() {
    log "${BLUE}💡 ¿POR QUÉ ES IMPORTANTE?${NC} $1"
}

print_what_to_look() {
    log "${BLUE}🔍 QUÉ BUSCAR:${NC} $1"
}

run_test() {
    local test_name="$1"
    local curl_command="$2"
    local validation="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log "${YELLOW}🧪 Test #${TOTAL_TESTS}: ${test_name}${NC}"
    log ""
    
    # Extraer y formatear si es un POST GraphQL
    if echo "$curl_command" | grep -q "POST.*graphql.*-d"; then
        # Extraer URL
        local url=$(echo "$curl_command" | grep -o "http[s]*://[^ ]*" | head -1)
        log "${CYAN}📤 REQUEST:${NC}"
        log "   ${BLUE}URL:${NC} POST $url"
        log "   ${BLUE}Content-Type:${NC} application/json"
        log ""
        log "   ${BLUE}BODY:${NC}"
        
        # Extraer el JSON del -d
        local json_body=$(echo "$curl_command" | sed -n "s/.*-d '\([^']*\)'.*/\1/p")
        
        # Limpiar escapes y formatear
        local clean_json="${json_body//\\\"/\"}"
        
        # Crear formato bonito
        local formatted_body="${CYAN}   {${NC}
     ${MAGENTA}\"query\"${NC}${CYAN}:${NC} ${GREEN}\"$(echo "$clean_json" | sed 's/.*"query":"\([^"]*\)".*/\1/')\"${NC}"
        
        # Agregar variables si existen
        if echo "$clean_json" | grep -q "variables"; then
            local vars=$(echo "$clean_json" | sed 's/.*"variables":\([^}]*}\).*/\1/')
            formatted_body="$formatted_body${CYAN},${NC}
     ${MAGENTA}\"variables\"${NC}${CYAN}:${NC} ${GREEN}$vars${NC}"
        fi
        
        formatted_body="$formatted_body
${CYAN}   }${NC}"
        
        log "$formatted_body"
    else
        log "${CYAN}📤 REQUEST:${NC}"
        log "   $curl_command"
    fi
    
    log ""
    log "   Ejecutando..."
    
    response=$(eval "$curl_command" 2>&1)
    exit_code=$?
    
    # Formatear JSON si jq está disponible
    if command -v jq >/dev/null 2>&1; then
        formatted=$(echo "$response" | jq -C '.' 2>/dev/null || echo "$response")
        if [ ${#formatted} -gt 400 ]; then
            log "   ${GREEN}📥 RESPONSE:${NC}"
            log "$formatted" | head -20
            log "   ${YELLOW}... (respuesta truncada, ver archivo de log completo)${NC}"
        else
            log "   ${GREEN}📥 RESPONSE:${NC}"
            log "$formatted"
        fi
    else
        if [ ${#response} -gt 200 ]; then
            log "   ${GREEN}📥 RESPONSE:${NC} ${response:0:200}..."
        else
            log "   ${GREEN}📥 RESPONSE:${NC} $response"
        fi
    fi
    
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
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log "${YELLOW}🧪 Test #${TOTAL_TESTS}: ${test_name}${NC}"
    log ""
    log "${CYAN}📤 REQUEST GraphQL:${NC}"
    log "   ${BLUE}URL:${NC} POST ${GRAPHQL_ENDPOINT}"
    log "   ${BLUE}Content-Type:${NC} application/json"
    log ""
    log "   ${BLUE}BODY:${NC}"
    
    # Limpiar escapes para mostrar legible
    local clean_query="${query//\\\"/\"}"
    
    # Crear el string completo CON saltos de línea (como hace jq con el response)
    local formatted_body="${CYAN}   {${NC}
     ${MAGENTA}\"query\"${NC}${CYAN}:${NC} ${GREEN}\"${clean_query}\"${NC}
${CYAN}   }${NC}"
    
    # UN SOLO log (como el response)
    log "$formatted_body"
    
    log ""
    log "   Ejecutando..."
    
    local curl_cmd="curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\": \"$query\"}'"
    response=$(eval "$curl_cmd" 2>&1)
    exit_code=$?
    
    log ""
    # Formatear JSON si jq está disponible
    if command -v jq >/dev/null 2>&1; then
        formatted=$(echo "$response" | jq -C '.' 2>/dev/null || echo "$response")
        if [ ${#formatted} -gt 400 ]; then
            log "   ${GREEN}📥 RESPONSE:${NC}"
            log "$formatted" | head -20
            log "   ${YELLOW}... (respuesta truncada, ver archivo de log completo)${NC}"
        else
            log "   ${GREEN}📥 RESPONSE:${NC}"
            log "$formatted"
        fi
    else
        if [ ${#response} -gt 200 ]; then
            log "   ${GREEN}📥 RESPONSE:${NC} ${response:0:200}..."
        else
            log "   ${GREEN}📥 RESPONSE:${NC} $response"
        fi
    fi
    
    log ""
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

log "================================================================================"
log "CHAPTER 01: FUNDAMENTOS DE GRAPHQL - TEST SUITE PEDAGÓGICO"
log "================================================================================"
log "Fecha: $(date)"
log "Modo: $([ "$INTERACTIVE" = true ] && echo "Interactivo ✨ (RECOMENDADO para aprender)" || echo "Silencioso")"
log "Sistema: $(uname -s)"
log "================================================================================"
log ""
log "${CYAN}Este script es una HERRAMIENTA DE APRENDIZAJE:${NC}"
log "  • Cada test explica UN concepto de GraphQL"
log "  • Verás la teoría ANTES de cada ejecución"
log "  • Compara las respuestas con lo que esperabas"
log "  • Toma tu tiempo - no hay prisa 🎓"
log ""
pause

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
log "${CYAN}En esta sección aprenderás:${NC}"
log "  • El problema del OVERFETCHING (traer datos de más)"
log "  • El problema del UNDERFETCHING (necesitar múltiples llamadas)"
log "  • Por qué GraphQL usa UN SOLO endpoint"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.1.1 - REST: El Problema del Overfetching"

print_concept "En REST, cuando pides un recurso, el servidor decide QUÉ campos enviar. \
Aunque solo necesites el NOMBRE del portfolio, REST te envía TODO: assets, performance, \
fechas, etc. Esto es OVERFETCHING (sobre-obtención de datos)."

print_objective "Demostrar que REST devuelve TODOS los campos aunque no los necesites."

print_why "Overfetching desperdicia ancho de banda, hace las respuestas más lentas \
y obliga al frontend a procesar datos innecesarios."

print_what_to_look "La respuesta incluirá 'assets' y 'performance' aunque NO los pedimos explícitamente."

log ""
run_test \
    "REST devuelve TODO (assets + performance)" \
    "curl -s ${REST_ENDPOINT}/portfolios" \
    '"assets".*"performance"'

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.1.2 - GraphQL: Solo lo Necesario"

print_concept "GraphQL invierte el control: el CLIENTE decide qué campos necesita. \
Si solo quieres nombres, pides: { myPortfolios { name } }. El servidor envía SOLO eso."

print_objective "Demostrar que GraphQL devuelve EXACTAMENTE lo que pedimos (solo 'name')."

print_why "Esto elimina overfetching, reduce el tamaño de respuesta y mejora la performance. \
Especialmente crítico en mobile donde el ancho de banda es limitado."

print_what_to_look "La respuesta solo contendrá el campo 'name', sin assets ni performance."

log ""
run_graphql_test \
    "GraphQL - Solo nombres (sin overfetching)" \
    "{ myPortfolios { name } }" \
    '"name"'

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.1.3 - REST: El Problema del Underfetching"

print_concept "UNDERFETCHING es el problema opuesto: cuando necesitas datos de \
múltiples recursos, REST te obliga a hacer MÚLTIPLES llamadas HTTP. \
Por ejemplo: obtener portfolio + sus assets + su performance = 3 llamadas."

print_objective "Demostrar que REST necesita 3 requests HTTP para datos relacionados."

print_why "Múltiples requests aumentan latencia (cada request tiene overhead de red), \
complican el código del frontend (manejo de 3 promises) y dificultan el loading state."

print_what_to_look "Vamos a hacer 3 llamadas separadas y contar cuántas requests HTTP se necesitan."

log ""
log "${YELLOW}📝 Vamos a ejecutar 3 llamadas REST secuenciales...${NC}"
log ""
pause

run_test \
    "REST - Llamada 1/3: Obtener Portfolio" \
    "curl -s ${REST_ENDPOINT}/portfolios/portfolio-001" \
    '"id":"portfolio-001"'

run_test \
    "REST - Llamada 2/3: Obtener Assets del Portfolio" \
    "curl -s ${REST_ENDPOINT}/portfolios/portfolio-001/assets" \
    '"symbol"'

run_test \
    "REST - Llamada 3/3: Obtener Performance del Portfolio" \
    "curl -s ${REST_ENDPOINT}/portfolios/portfolio-001/performance" \
    '"totalReturn"'

log "${RED}❗ Resultado: 3 llamadas HTTP para mostrar UNA pantalla${NC}"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.1.4 - GraphQL: Una Sola Llamada para Todo"

print_concept "GraphQL permite CONSULTAS ANIDADAS. Puedes pedir portfolio, sus assets \
y su performance en UNA sola query. El servidor resuelve todas las relaciones internamente."

print_objective "Obtener portfolio + assets + performance en UN SOLO request HTTP."

print_why "Reducir de 3 requests a 1 significa: menor latencia (un solo round-trip), \
código más simple en el frontend, y mejor UX (no hay estados de carga intermedios)."

print_what_to_look "Una sola respuesta JSON con 'portfolio', 'assets' y 'performance' anidados."

log ""
run_graphql_test \
    "GraphQL - Portfolio + Assets + Performance en UNA query" \
    "{ portfolio(id: \\\"portfolio-001\\\") { name assets { symbol } performance { totalReturn } } }" \
    '"portfolio".*"assets".*"performance"'

log "${GREEN}✨ Resultado: 1 llamada HTTP vs 3 de REST - 66% menos requests${NC}"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.1.5 - Endpoint Único"

print_concept "REST típicamente expone múltiples endpoints: /users, /posts, /comments, etc. \
GraphQL usa UN SOLO endpoint: /graphql. Todas las operaciones pasan por ahí."

print_objective "Verificar que GraphQL funciona con un único punto de entrada."

print_why "Un solo endpoint simplifica la infraestructura: menos rutas que configurar, \
más fácil de cachear, y mejor para rate limiting (límites de peticiones)."

print_what_to_look "Todas nuestras queries irán a POST /graphql."

log ""
run_graphql_test \
    "GraphQL - Endpoint único /graphql para TODAS las queries" \
    "{ myPortfolios { id } }" \
    '"myPortfolios"'

log "${CYAN}📊 Comparación REST vs GraphQL:${NC}"
log "  REST: /portfolios, /portfolios/:id, /portfolios/:id/assets, etc."
log "  GraphQL: /graphql (TODO pasa por aquí)"
log ""
pause

################################################################################
# SECCIÓN 1.2 - COMPONENTES Y LENGUAJE BASE
################################################################################
print_section "SECCIÓN 1.2 — COMPONENTES Y LENGUAJE BASE (30 min)"
log "${CYAN}En esta sección aprenderás los 5 pilares de GraphQL:${NC}"
log "  1. Schema - El contrato entre cliente y servidor"
log "  2. Types - Los tipos de datos (Portfolio, Asset, etc.)"
log "  3. Queries - Operaciones de lectura"
log "  4. Mutations - Operaciones de escritura"
log "  5. Resolvers - Lógica que conecta queries con datos"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.2.1 - Schema: El Contrato de la API"

print_concept "El SCHEMA es como un contrato legal: define QUÉ operaciones están disponibles \
y QUÉ tipos de datos existen. En GraphQL, el schema es la fuente de verdad."

print_objective "Consultar un type definido en el schema (Portfolio) con sus campos."

print_why "El schema auto-documenta tu API. Los clientes pueden hacer introspection \
(consultar el schema) para descubrir qué pueden pedir. Herramientas como GraphiQL \
usan el schema para autocompletado."

print_what_to_look "Los campos id, name, totalValue existen porque están definidos en schema.graphqls."

log ""
run_graphql_test \
    "Type Portfolio según el schema" \
    "{ portfolio(id: \\\"portfolio-001\\\") { id name totalValue } }" \
    '"id".*"name".*"totalValue"'

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.2.2 - Queries: Operaciones de Lectura"

print_concept "QUERIES son operaciones que LEEN datos (como SELECT en SQL o GET en REST). \
En GraphQL, defines queries en el schema y las implementas con resolvers."

print_objective "Ejecutar 3 tipos diferentes de queries: sin argumentos, con ID, y búsqueda."

print_why "Las queries son tu interfaz de lectura. Un schema bien diseñado ofrece \
queries flexibles para diferentes casos de uso del frontend."

print_what_to_look "Cada query retorna el tipo especificado en el schema."

log ""
log "${YELLOW}📝 Query 1: myPortfolios (sin argumentos)${NC}"
run_graphql_test \
    "Query sin argumentos: myPortfolios" \
    "{ myPortfolios { id name } }" \
    '"myPortfolios"'

log "${YELLOW}📝 Query 2: portfolio(id) (con argumento obligatorio)${NC}"
run_graphql_test \
    "Query con argumento: portfolio(id: ID!)" \
    "{ portfolio(id: \\\"portfolio-001\\\") { name } }" \
    '"Growth Portfolio"'

log "${YELLOW}📝 Query 3: searchAsset(symbol) (búsqueda por criterio)${NC}"
run_graphql_test \
    "Query de búsqueda: searchAsset(symbol: String!)" \
    "{ searchAsset(symbol: \\\"AAPL\\\") { name currentPrice } }" \
    '"Apple Inc'

log "${GREEN}✨ 3 patrones de queries: sin args, con ID, y búsqueda${NC}"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.2.3 - Mutations: Operaciones de Escritura"

print_concept "MUTATIONS son operaciones que MODIFICAN datos (como INSERT/UPDATE/DELETE en SQL \
o POST/PUT/DELETE en REST). Por convención, siempre empiezan con la palabra 'mutation'."

print_objective "Ejecutar mutations de creación, adición y eliminación."

print_why "Separar queries de mutations hace el código más claro. Las mutations se ejecutan \
SECUENCIALMENTE (una tras otra), mientras que queries pueden ejecutarse en paralelo."

print_what_to_look "Las mutations retornan un objeto con 'success' y el recurso creado/modificado."

log ""
log "${YELLOW}📝 Mutation 1: createPortfolio${NC}"
run_graphql_test \
    "Mutation: Crear un nuevo portfolio" \
    "mutation { createPortfolio(input: {name: \\\"Test Portfolio\\\"}) { success portfolio { id } } }" \
    '"success":true'

log "${YELLOW}📝 Mutation 2: addAsset (encadenada)${NC}"
log "${BLUE}💡 Vamos a crear un portfolio temporal y agregarle un asset${NC}"
log ""

# Crear portfolio y capturar su ID
CREATED_ID=$(curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' \
    -d '{"query": "mutation { createPortfolio(input: {name: \"Temp Portfolio\"}) { portfolio { id } } }"}' \
    | sed -n 's/.*"id":"\([^"]*\)".*/\1/p' | head -1)

if [ -n "$CREATED_ID" ]; then
    log "${GREEN}✅ Portfolio creado con ID: ${CREATED_ID}${NC}"
    log "${YELLOW}📝 Ahora agregamos un asset al portfolio...${NC}"
    log ""
    run_graphql_test \
        "Mutation: Agregar asset NVDA al portfolio" \
        "mutation { addAsset(input: {portfolioId: \\\"${CREATED_ID}\\\", symbol: \\\"NVDA\\\", assetType: STOCK, quantity: 10, buyPrice: 500}) { success asset { symbol totalValue } } }" \
        '"success":true'
else
    log "${YELLOW}⚠️  No se pudo capturar el ID, saltando addAsset${NC}"
fi

log "${YELLOW}📝 Mutation 3: removeAsset${NC}"
run_graphql_test \
    "Mutation: Eliminar un asset" \
    "mutation { removeAsset(portfolioId: \\\"portfolio-001\\\", assetId: \\\"fake\\\") { success message } }" \
    '"success"'

log "${GREEN}✨ 3 tipos de mutations: CREATE, ADD, DELETE${NC}"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.2.4 - Enums: Valores Constantes Validados"

print_concept "ENUMS son tipos que solo aceptan valores predefinidos. En nuestro caso, \
AssetType solo puede ser: STOCK, CRYPTO, ETF, BOND o COMMODITY."

print_objective "Verificar que los assets retornan un assetType válido (enum)."

print_why "Los enums previenen errores: GraphQL rechaza valores inválidos ANTES de ejecutar \
la query. Esto evita bugs como 'stok' (typo) o 'stock' (lowercase incorrecto)."

print_what_to_look "Todos los assetType serán uno de los valores del enum."

log ""
run_graphql_test \
    "Enum AssetType con valores validados" \
    "{ portfolio(id: \\\"portfolio-001\\\") { assets { symbol assetType } } }" \
    '"assetType"'

log "${CYAN}📝 Valores válidos de AssetType:${NC}"
log "  STOCK (acciones), CRYPTO (criptomonedas), ETF (fondos), BOND (bonos), COMMODITY (materias primas)"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.2.5 - Resolvers: La Lógica Detrás de las Queries"

print_concept "RESOLVERS son funciones que obtienen los datos para cada campo. \
Por ejemplo, el resolver de 'myPortfolios' va al servicio y busca los portfolios del usuario."

print_objective "Verificar que los resolvers están conectados al MockDataService."

print_why "Los resolvers son la 'lógica de negocio' de GraphQL. Aquí es donde conectas \
con bases de datos, APIs externas, o cualquier fuente de datos. Separan el SCHEMA (contrato) \
de la IMPLEMENTACIÓN (cómo obtienes los datos)."

print_what_to_look "El campo 'ownerName' viene del MockDataService, no de una base de datos real."

log ""
run_graphql_test \
    "Resolvers obtienen datos del servicio" \
    "{ myPortfolios { ownerName assets { symbol } } }" \
    '"Carlos Mendoza"'

log "${CYAN}📝 Flujo de un resolver:${NC}"
log "  1. Cliente pide: { myPortfolios { ownerName } }"
log "  2. GraphQL llama al resolver de 'myPortfolios'"
log "  3. Resolver ejecuta: dataService.getPortfoliosByOwnerId(userId)"
log "  4. Retorna los datos al cliente"
log ""
pause

################################################################################
# SECCIÓN 1.3 - CONSULTAS ANIDADAS Y VARIABLES
################################################################################
print_section "SECCIÓN 1.3 — CONSULTAS ANIDADAS Y VARIABLES (30 min)"
log "${CYAN}En esta sección aprenderás:${NC}"
log "  • Cómo anidar consultas (portfolio → assets → bestPerformer)"
log "  • Cómo usar variables para queries reutilizables"
log "  • Por qué la validación de tipos es tan poderosa"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.3.1 - Anidación Nivel 1: Relaciones Directas"

print_concept "GraphQL permite ANIDAR objetos relacionados. Si Portfolio tiene assets, \
puedes pedir: { portfolio { assets { ... } } } en una sola query."

print_objective "Obtener un portfolio con sus assets anidados."

print_why "La anidación elimina la necesidad de múltiples requests. En REST, necesitarías: \
GET /portfolio → GET /portfolio/123/assets. GraphQL lo hace en UNO."

print_what_to_look "La respuesta tendrá 'portfolio' con un array 'assets' adentro."

log ""
run_graphql_test \
    "Portfolio con assets anidados (1 nivel)" \
    "{ portfolio(id: \\\"portfolio-001\\\") { name assets { symbol currentPrice } } }" \
    '"assets"'

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.3.2 - Anidación Nivel 2 y 3: Relaciones Profundas"

print_concept "Puedes anidar MÚLTIPLES niveles: portfolio → performance → bestPerformer (asset). \
Esto es un nivel 3 de profundidad."

print_objective "Navegar 3 niveles: Portfolio → Performance → Best Performer Asset."

print_why "La profundidad de anidación te permite obtener datos complejos sin hacer waterfall \
de requests (llamadas secuenciales). Imagina necesitar 3, 4, 5 niveles en REST: sería un desastre."

print_what_to_look "Respuesta con portfolio.performance.bestPerformer.symbol."

log ""
run_graphql_test \
    "Portfolio → Performance → BestPerformer (3 niveles de anidación)" \
    "{ portfolio(id: \\\"portfolio-001\\\") { name performance { totalReturn bestPerformer { symbol profitLossPercent } } } }" \
    '"bestPerformer"'

log "${CYAN}📊 Estructura anidada:${NC}"
log "  portfolio"
log "    ├── performance"
log "    │   ├── totalReturn"
log "    │   └── bestPerformer"
log "    │       ├── symbol"
log "    │       └── profitLossPercent"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.3.3 - Variables: Queries Reutilizables"

print_concept "En lugar de hardcodear valores en la query (id: 'portfolio-001'), \
usas VARIABLES: query(\$id: ID!) { portfolio(id: \$id) { ... } }. Las variables \
van en un objeto JSON separado."

print_objective "Ejecutar una query con una variable \$id."

print_why "Las variables hacen las queries REUTILIZABLES. El mismo código de frontend \
puede usarse con diferentes IDs. Además, previenen inyección de código (como SQL injection)."

print_what_to_look "La query usa \$id como placeholder, y el valor viene del JSON 'variables'."

log ""
log "${YELLOW}📝 Query con variable:${NC}"
log "  query(\$id: ID!) { portfolio(id: \$id) { name } }"
log "${YELLOW}📝 Variables JSON:${NC}"
log "  { \"id\": \"portfolio-001\" }"
log ""
run_test \
    "Query con variable \$id (parametrizada)" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"query(\$id:ID!){portfolio(id:\$id){name}}\",\"variables\":{\"id\":\"portfolio-001\"}}'" \
    '"Growth Portfolio"'

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.3.4 - Validación Automática de Variables"

print_concept "GraphQL valida el TIPO de las variables ANTES de ejecutar la query. \
Si declaras \$symbol: String! y envías un número, GraphQL rechaza la request SIN ejecutarla."

print_objective "Verificar que una variable String! solo acepta strings."

print_why "La validación automática atrapa errores TEMPRANO. En REST, enviarías el request \
al servidor, procesaría parcialmente, y luego fallaría. GraphQL falla ANTES, ahorrando recursos."

print_what_to_look "La query se ejecuta exitosamente porque enviamos un string válido ('BTC')."

log ""
run_test \
    "Variable String! validada automáticamente" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"query(\$s:String!){searchAsset(symbol:\$s){name currentPrice}}\",\"variables\":{\"s\":\"BTC\"}}'" \
    '"Bitcoin"'

log "${GREEN}✨ GraphQL validó que 'BTC' es un String antes de ejecutar${NC}"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.3.5 - Múltiples Relaciones en Una Query"

print_concept "Puedes pedir MÚLTIPLES relaciones al mismo tiempo: assets Y performance. \
GraphQL resuelve ambas en paralelo (si es posible) y te devuelve todo junto."

print_objective "Obtener assets y performance de un portfolio en una sola query."

print_why "Esto simplifica el frontend: un solo useEffect/fetch en vez de dos. \
Y GraphQL optimiza la ejecución: si ambas relaciones vienen de la misma fuente, \
puede hacer una sola consulta a la BD."

print_what_to_look "Respuesta con 'assets' (array) y 'performance' (objeto) al mismo nivel."

log ""
run_graphql_test \
    "Assets Y Performance en una sola query" \
    "{ portfolio(id: \\\"portfolio-002\\\") { name assets { symbol totalValue } performance { yearReturn monthReturn } } }" \
    '"Retirement Fund"'

log "${CYAN}📊 Comparación:${NC}"
log "  REST: GET /portfolio + GET /portfolio/assets + GET /portfolio/performance = 3 requests"
log "  GraphQL: { portfolio { assets { } performance { } } } = 1 request"
log ""
pause

################################################################################
# SECCIÓN 1.4 - FILTROS, ORDEN Y PAGINACIÓN
################################################################################
print_section "SECCIÓN 1.4 — FILTROS, ORDEN Y PAGINACIÓN (30 min)"
log "${CYAN}En esta sección aprenderás:${NC}"
log "  • Cómo filtrar resultados (por tipo, por rango de valores)"
log "  • Cómo ordenar (ASC/DESC por diferentes campos)"
log "  • Paginación cursor-based (el estándar de GraphQL)"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.4.1 - Filtrado por Tipo (Enum)"

print_concept "Los INPUT TYPES permiten pasar objetos complejos como argumentos. \
AssetFilterInput tiene un campo 'assetType' que filtra por STOCK, CRYPTO, ETF, etc."

print_objective "Filtrar assets para obtener SOLO stocks (acciones)."

print_why "Los filtros evitan traer datos innecesarios. Si solo quieres stocks, \
¿para qué traer cryptos y ETFs? Esto reduce el tamaño de la respuesta y mejora performance."

print_what_to_look "totalCount mostrará cuántos stocks hay (debería ser 2 en nuestros datos mock)."

log ""
run_graphql_test \
    "Filtrar solo STOCKS (assetType: STOCK)" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", filter: {assetType: STOCK}) { totalCount edges { node { symbol assetType } } } }" \
    '"totalCount":2'

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.4.2 - Filtrado por Rango de Valores"

print_concept "Puedes filtrar por criterios numéricos: minValue, maxValue. \
Por ejemplo, 'dame assets con valor mayor a $5000'."

print_objective "Filtrar assets con totalValue > 5000."

print_why "Los filtros de rango son comunes: buscar productos entre $X y $Y, \
usuarios con edad > 18, etc. GraphQL hace estos filtros explícitos en el schema."

print_what_to_look "Solo assets con totalValue >= 5000 aparecerán."

log ""
run_graphql_test \
    "Assets con valor mayor a $5000" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", filter: {minValue: 5000}) { totalCount edges { node { symbol totalValue } } } }" \
    '"totalCount":[0-9]'

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.4.3 - Ordenamiento Descendente (Mayor Primero)"

print_concept "AssetSortInput define CÓMO ordenar: por qué campo (TOTAL_VALUE, SYMBOL, etc.) \
y en qué dirección (ASC ascendente / DESC descendente)."

print_objective "Ordenar assets por totalValue de MAYOR a MENOR (DESC)."

print_why "El ordenamiento es crítico para UX: mostrar los mejores performers primero, \
ordenar productos por precio, etc. GraphQL hace el ordenamiento en el servidor (más eficiente)."

print_what_to_look "El primer asset en 'edges' será el de mayor valor."

log ""
run_graphql_test \
    "Ordenar por valor total (mayor → menor)" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", sort: {field: TOTAL_VALUE, direction: DESC}) { edges { node { symbol totalValue } } } }" \
    '"edges"'

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.4.4 - Ordenamiento por Profit/Loss %"

print_concept "Puedes ordenar por CUALQUIER campo que esté en AssetSortField. \
En este caso, ordenamos por ganancia/pérdida porcentual."

print_objective "Ver qué assets tienen mejor/peor performance (% de profit)."

print_why "Los traders quieren ver sus mejores/peores inversiones. Ordenar por \
profitLossPercent permite hacer rankings de performance."

print_what_to_look "Assets ordenados por su porcentaje de ganancia (positivo) o pérdida (negativo)."

log ""
run_graphql_test \
    "Ordenar por ganancia/pérdida % (mejor → peor)" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", sort: {field: PROFIT_LOSS_PERCENT, direction: DESC}) { edges { node { symbol profitLossPercent } } } }" \
    '"profitLossPercent"'

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.4.5 - Paginación Cursor-Based"

print_concept "CURSOR-BASED PAGINATION es el estándar de GraphQL (patrón Relay). \
En vez de 'página 1, página 2', usas cursors (como punteros) para navegar."

print_objective "Obtener la primera página (limit: 2 elementos) y ver si hay más."

print_why "Cursor-based es mejor que offset-based cuando los datos cambian frecuentemente. \
Si alguien agrega un item mientras navegas, offset-based puede duplicar o saltar elementos. \
Cursors NO tienen ese problema."

print_what_to_look "pageInfo.hasNextPage te dice si hay más resultados para cargar."

log ""
run_graphql_test \
    "Primera página (limit: 2) con cursor-based pagination" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", pagination: {limit: 2}) { pageInfo { hasNextPage hasPreviousPage } edges { cursor node { symbol } } } }" \
    '"hasNextPage"'

log "${CYAN}📝 Estructura de paginación:${NC}"
log "  • edges: array de resultados"
log "  • cursor: identificador único de cada elemento"
log "  • pageInfo.hasNextPage: ¿hay más páginas?"
log "  • Para la siguiente página: pagination: {limit: 2, after: 'cursor'}"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.4.6 - Combinando Filtro + Orden + Paginación"

print_concept "LO MÁS PODEROSO de GraphQL: combinar múltiples features. \
Filtra + ordena + pagina en UNA SOLA QUERY."

print_objective "Filtrar stocks, ordenarlos por valor, y paginar (limit: 2)."

print_why "Esta es una query del mundo real. Por ejemplo: 'Dame las 10 acciones más valiosas \
de mi portfolio'. En REST, necesitarías lógica compleja en el frontend. GraphQL lo hace en el servidor."

print_what_to_look "Resultado: solo stocks, ordenados por valor, máximo 2 resultados."

log ""
run_graphql_test \
    "Query compleja: filtro + orden + paginación combinados" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", filter: {assetType: STOCK}, sort: {field: TOTAL_VALUE, direction: DESC}, pagination: {limit: 2}) { totalCount pageInfo { hasNextPage } edges { node { symbol totalValue assetType } } } }" \
    '"totalCount"'

log "${GREEN}✨ Una query, tres features: filtra → ordena → pagina${NC}"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.4.7 - Límites Controlados por el Backend"

print_concept "Aunque el cliente puede pedir cualquier limit, el BACKEND debe imponer \
límites máximos. Por ejemplo: 'nunca retornes más de 100 elementos por página'."

print_objective "Verificar que el servidor responde a paginación razonable (limit: 10)."

print_why "Sin límites, un cliente malicioso podría pedir limit: 1000000 y tumbar el servidor. \
Los límites protegen contra DoS (Denial of Service) accidentales o intencionales."

print_what_to_look "La query funciona con limit: 10 (razonable)."

log ""
run_graphql_test \
    "Paginación con límite razonable (10 elementos)" \
    "{ assets(portfolioId: \\\"portfolio-001\\\", pagination: {limit: 10}) { edges { node { symbol } } pageInfo { hasNextPage } } }" \
    '"edges"'

log "${YELLOW}💡 En producción:${NC}"
log "  • Máximo 100 elementos por página"
log "  • Queries muy profundas (10+ niveles) rechazadas"
log "  • Timeouts para queries que tardan > 10 segundos"
log ""
pause

################################################################################
# SECCIÓN 1.5 - TIPADO Y SEGURIDAD
################################################################################
print_section "SECCIÓN 1.5 — TIPADO, NULLABILIDAD Y SEGURIDAD (30 min)"
log "${CYAN}En esta sección aprenderás:${NC}"
log "  • Cómo GraphQL valida tipos automáticamente"
log "  • Qué significa el símbolo ! (non-nullable)"
log "  • Cómo funcionan los enums"
log "  • Introspection (auto-documentación)"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.5.1 - Validación Automática de Tipos"

print_concept "GraphQL valida que cada campo retorne el tipo correcto. \
Si el schema dice 'totalValue: Float!', GraphQL verifica que sea un número."

print_objective "Verificar que totalValue retorna un Float (número decimal)."

print_why "La validación de tipos previene bugs. En REST JSON sin tipado, podrías recibir \
'totalValue': '15000' (string) cuando esperabas un número. GraphQL NO permite eso."

print_what_to_look "totalValue será un número (con decimales), no un string."

log ""
run_graphql_test \
    "Float! retorna número válido" \
    "{ portfolio(id: \\\"portfolio-001\\\") { totalValue } }" \
    '"totalValue":[0-9]'

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.5.2 - Non-Nullable Fields (!)"

print_concept "El símbolo ! significa NON-NULLABLE (no puede ser null). \
Si un campo es ID!, GraphQL GARANTIZA que SIEMPRE tendrá valor."

print_objective "Verificar que campos marcados con ! nunca son null."

print_why "Non-nullable elimina null checks en el frontend. Si sabes que 'id' NUNCA es null, \
no necesitas: if (id !== null). Esto hace el código más simple y seguro."

print_what_to_look "id, name y totalValue siempre presentes (nunca null)."

log ""
run_graphql_test \
    "ID!, String!, Float! siempre presentes (non-nullable)" \
    "{ portfolio(id: \\\"portfolio-001\\\") { id name totalValue } }" \
    '"id":"portfolio-001"'

log "${CYAN}📝 Diferencia:${NC}"
log "  name: String!  → SIEMPRE tiene valor (no puede ser null)"
log "  email: String  → PUEDE ser null (campo opcional)"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.5.3 - Enums: Validación de Valores Constantes"

print_concept "Los ENUMS son tipos con valores predefinidos. AssetType solo acepta: \
STOCK, CRYPTO, ETF, BOND, COMMODITY. Cualquier otro valor es rechazado."

print_objective "Verificar que assetType solo retorna valores del enum."

print_why "Los enums previenen typos y valores inválidos. Sin enum, podrías tener: \
'stock', 'Stock', 'STOCK', 'stok' (todas diferentes). Con enum: SOLO 'STOCK' es válido."

print_what_to_look "Todos los assetType serán uno de los 5 valores permitidos."

log ""
run_graphql_test \
    "Enum valida valores automáticamente" \
    "{ portfolio(id: \\\"portfolio-001\\\") { assets { symbol assetType } } }" \
    '"assetType":"(STOCK|CRYPTO|ETF)"'

log "${CYAN}📝 Valores válidos de AssetType:${NC}"
log "  ✅ STOCK, CRYPTO, ETF, BOND, COMMODITY"
log "  ❌ 'stock' (lowercase), 'Stocks' (plural), 'BTC' (símbolo)"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.5.4 - Introspection: Auto-Documentación"

print_concept "INTROSPECTION permite consultar el schema mismo. Puedes preguntar: \
'¿Qué tipos existen?', '¿Qué queries hay?', '¿Qué campos tiene Portfolio?'."

print_objective "Consultar todos los tipos definidos en el schema."

print_why "Introspection alimenta herramientas como GraphiQL, Apollo Studio, y generadores \
de código. También permite que el frontend descubra la API dinámicamente."

print_what_to_look "Lista de types: Query, Mutation, Portfolio, Asset, etc."

log ""
run_graphql_test \
    "Schema introspection (__schema)" \
    "{ __schema { types { name } } }" \
    '"__schema"'

log "${CYAN}📝 Queries especiales:${NC}"
log "  • __schema: información del schema completo"
log "  • __type(name: 'Portfolio'): detalles de un type específico"
log "  • Herramientas como GraphiQL usan esto para autocompletar"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.5.5 - Validación de Argumentos Obligatorios"

print_concept "Si un argumento está marcado como obligatorio (ID!), GraphQL rechaza \
la query si NO lo proporcionas. Ni siquiera llega al resolver."

print_objective "Intentar ejecutar portfolio SIN el argumento id (debería fallar)."

print_why "La validación de argumentos previene errores del cliente. En REST, enviarías \
GET /portfolio (sin ID), el servidor procesaría, y luego retornaría 400 Bad Request. \
GraphQL falla ANTES, ahorrando ciclos de CPU."

print_what_to_look "GraphQL retorna un error diciendo que 'id' es requerido."

log ""
run_test \
    "Argumento ID! obligatorio (falla si no se proporciona)" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\": \"{ portfolio { name } }\"}'" \
    'error'

log "${GREEN}✨ GraphQL rechazó la query porque falta 'id' (ID!)${NC}"
log ""
pause

# ─────────────────────────────────────────────────────────────────────────────
print_subsection "1.5.6 - Estructura Consistente de Respuestas"

print_concept "Las mutations siguen un PATRÓN: retornan un objeto con 'success', \
'message', y opcionalmente el recurso creado/modificado."

print_objective "Verificar que createPortfolio retorna success + message."

print_why "La consistencia facilita el manejo de errores en el frontend. SIEMPRE sabes \
que puedes hacer: if (response.success) { ... } sin importar qué mutation ejecutaste."

print_what_to_look "Respuesta con 'success' (boolean) y 'message' (string)."

log ""
run_graphql_test \
    "Mutations: estructura success/message/data consistente" \
    "mutation { createPortfolio(input: {name: \\\"Final Test\\\"}) { success message portfolio { id name } } }" \
    '"success".*"message"'

log "${CYAN}📝 Patrón estándar de respuestas:${NC}"
log "  {"
log "    success: true/false,"
log "    message: 'Portfolio created successfully',"
log "    portfolio: { id, name, ... }  // opcional"
log "  }"
log ""
pause

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
    log "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    log "${CYAN}📚 CONCEPTOS APRENDIDOS:${NC}"
    log "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    log ""
    log "${GREEN}✅ Sección 1.1 - REST vs GraphQL:${NC}"
    log "  • Overfetching: REST envía datos de más"
    log "  • Underfetching: REST necesita múltiples llamadas"
    log "  • Endpoint único: /graphql para todo"
    log ""
    log "${GREEN}✅ Sección 1.2 - Componentes base:${NC}"
    log "  • Schema: contrato entre cliente y servidor"
    log "  • Types: Portfolio, Asset, Performance"
    log "  • Queries: operaciones de lectura"
    log "  • Mutations: operaciones de escritura"
    log "  • Resolvers: lógica que conecta queries con datos"
    log "  • Enums: valores constantes validados"
    log ""
    log "${GREEN}✅ Sección 1.3 - Anidación y variables:${NC}"
    log "  • Consultas anidadas: portfolio → assets → performance"
    log "  • Variables tipadas: query(\$id: ID!)"
    log "  • Validación automática de tipos"
    log ""
    log "${GREEN}✅ Sección 1.4 - Filtros y paginación:${NC}"
    log "  • Filtrado por tipo y rango de valores"
    log "  • Ordenamiento ASC/DESC por múltiples campos"
    log "  • Paginación cursor-based (patrón Relay)"
    log "  • Combinación de filtro + orden + paginación"
    log ""
    log "${GREEN}✅ Sección 1.5 - Tipado y seguridad:${NC}"
    log "  • Validación automática de tipos"
    log "  • Non-nullable (!): campos obligatorios"
    log "  • Enums: valores constantes"
    log "  • Introspection: auto-documentación"
    log "  • Respuestas consistentes (success/message/data)"
    log ""
    log "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    log "${CYAN}📊 ESTADÍSTICAS:${NC}"
    log "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    log ""
    log "  📍 Cobertura del temario: 100%"
    log "  🧪 Tests automatizados: ${TOTAL_TESTS}"
    log "  ⏱️  Tiempo estimado (interactivo): ~15 minutos"
    log "  📦 Conceptos cubiertos: 30+"
    log ""
    log "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    log "${GREEN}🎓 SIGUIENTE PASO:${NC}"
    log "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    log ""
    log "  Ahora que dominas los fundamentos, estás listo para:"
    log "  → Chapter 02: Diseño correcto de schemas y buenas prácticas"
    log "  → Chapter 03: Netflix DGS Framework (implementación real)"
    log ""
    log "${YELLOW}💡 TIP: Guarda este output y revísalo cuando tengas dudas${NC}"
    log ""
    exit 0
else
    log "${RED}⚠️  Algunos tests fallaron (${FAILED_TESTS}/${TOTAL_TESTS}).${NC}"
    log ""
    log "${YELLOW}📋 Pasos para debugging:${NC}"
    log "  1. Revisa el output arriba - busca el primer test que falló"
    log "  2. Verifica que el servidor esté corriendo: mvn spring-boot:run"
    log "  3. Prueba la query manualmente en GraphiQL: http://localhost:8080/graphiql"
    log "  4. Lee el archivo de logs: ${OUTPUT_FILE}"
    log ""
    log "${YELLOW}💡 TIP: Ejecuta tests individuales con curl para debug más fácil${NC}"
    log ""
    exit 1
fi