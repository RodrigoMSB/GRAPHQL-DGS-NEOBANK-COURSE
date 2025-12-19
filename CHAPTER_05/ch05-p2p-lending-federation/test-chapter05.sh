#!/usr/bin/env bash

################################################################################
# CHAPTER 05: APOLLO FEDERATION - P2P LENDING MARKETPLACE
# Script de Testing Automatizado - VERSIÓN EDUCATIVA
#
# Compatible con:
#   - macOS (Bash 3.2+)
#   - Linux (Bash 4.0+)
#   - Windows GitBash (Bash 4.4+)
#
# Uso: 
#   ./test-chapter05.sh           (modo interactivo)
#   ./test-chapter05.sh -s        (modo silencioso)
################################################################################

export LC_ALL=C

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
MAGENTA='\033[1;35m'
CYAN='\033[0;36m'
BLUE='\033[0;34m'
WHITE='\033[1;37m'
GRAY='\033[0;90m'
NC='\033[0m'

# Config
USERS_URL="http://localhost:8081/graphql"
LOANS_URL="http://localhost:8082/graphql"
GATEWAY_URL="http://localhost:8080/graphql"
OUTPUT_FILE="test-results-chapter05-$(date +%Y%m%d-%H%M%S).txt"

INTERACTIVE=true
if [ "$1" = "-s" ]; then
    INTERACTIVE=false
fi

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

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

print_schema() {
    log ""
    log "${WHITE}📐 SCHEMA:${NC}"
    log "${GRAY}$1${NC}"
    log ""
}

print_java() {
    log "${WHITE}☕ JAVA (implementación):${NC}"
    log "${GRAY}$1${NC}"
    log ""
}

# Función para ejecutar tests con REQUEST y BODY visibles y formateados
run_graphql_test() {
    local test_name="$1"
    local service_url="$2"
    local service_name="$3"
    local graphql_query="$4"
    local validation="$5"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log "${YELLOW}🧪 Test #${TOTAL_TESTS}: ${test_name}${NC}"
    log ""
    
    # Mostrar el REQUEST
    log "${BLUE}📤 REQUEST:${NC}"
    log "${WHITE}   POST ${service_url}${NC}"
    log "${WHITE}   Content-Type: application/json${NC}"
    log "${WHITE}   Service: ${service_name}${NC}"
    log ""
    
    # Mostrar el BODY JSON formateado correctamente
    local display_query
    display_query=$(echo "$graphql_query" | sed 's/\\"/"/g')
    
    log "${BLUE}📋 BODY:${NC}"
    log "${GRAY}   {${NC}"
    log "${GRAY}     \"query\": \"${CYAN}${display_query}${GRAY}\"${NC}"
    log "${GRAY}   }${NC}"
    log ""
    
    # Ejecutar curl
    log "${BLUE}⚡ Ejecutando...${NC}"
    response=$(curl -s -X POST "${service_url}" \
        -H "Content-Type: application/json" \
        -d "{\"query\":\"$graphql_query\"}" 2>&1)
    exit_code=$?
    
    # Mostrar respuesta formateada
    log ""
    log "${BLUE}📥 RESPONSE:${NC}"
    if command -v jq >/dev/null 2>&1; then
        formatted=$(echo "$response" | jq '.' 2>/dev/null || echo "$response")
        echo "$formatted" | while IFS= read -r line; do
            log "${GREEN}   $line${NC}"
        done
    else
        log "${GREEN}   $response${NC}"
    fi
    
    log ""
    
    # Validar
    if [ $exit_code -eq 0 ] && echo "$response" | grep -qE "$validation"; then
        log "${GREEN}   ✅ PASSED${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        log "${RED}   ❌ FAILED${NC}"
        log "${RED}   Expected pattern: $validation${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    log ""
    pause
}

check_services() {
    log "${YELLOW}🔍 Verificando que los servicios estén corriendo...${NC}"
    log ""
    
    local all_ok=true
    
    # Users Service
    if curl -s -X POST "$USERS_URL" -H "Content-Type: application/json" -d '{"query":"{__typename}"}' > /dev/null 2>&1; then
        log "${GREEN}   ✅ Users Service (8081): OK${NC}"
    else
        log "${RED}   ❌ Users Service (8081): NO RESPONDE${NC}"
        all_ok=false
    fi
    
    # Loans Service
    if curl -s -X POST "$LOANS_URL" -H "Content-Type: application/json" -d '{"query":"{__typename}"}' > /dev/null 2>&1; then
        log "${GREEN}   ✅ Loans Service (8082): OK${NC}"
    else
        log "${RED}   ❌ Loans Service (8082): NO RESPONDE${NC}"
        all_ok=false
    fi
    
    log ""
    
    if [ "$all_ok" = false ]; then
        log "${RED}❌ ERROR: Algunos servicios no están activos.${NC}"
        log "${YELLOW}Por favor ejecuta:${NC}"
        log "${WHITE}   cd ch05-p2p-lending-federation${NC}"
        log "${WHITE}   docker-compose up -d --build${NC}"
        log ""
        exit 1
    fi
    
    log "${GREEN}✅ Todos los servicios están activos!${NC}"
    log ""
}

################################################################################
# HEADER
################################################################################

clear
log "${CYAN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
log "${CYAN}║                                                                              ║${NC}"
log "${CYAN}║        📘 CHAPTER 05: APOLLO FEDERATION - P2P LENDING                       ║${NC}"
log "${CYAN}║                     Testing Automatizado Completo                            ║${NC}"
log "${CYAN}║                                                                              ║${NC}"
log "${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
log ""
log "${YELLOW}Feature: P2P Lending Marketplace (Préstamos entre Personas)${NC}"
log "${YELLOW}Arquitectura: Apollo Federation con 2 Subgrafos${NC}"
log "${YELLOW}Duración: 1.75 horas${NC}"
log "${YELLOW}Log: ${OUTPUT_FILE}${NC}"
log ""

check_services
pause

################################################################################
# SECCIÓN 5.1 - INTRODUCCIÓN A APOLLO FEDERATION
################################################################################

print_section "SECCIÓN 5.1 — ¿QUÉ ES APOLLO FEDERATION?"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Entender por qué necesitamos Federation y cómo funciona.                  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  PROBLEMA: Schema monolítico gigante                                       │${NC}"
log "${WHITE}│  ┌─────────────────────────────────────┐                                   │${NC}"
log "${WHITE}│  │  UN SOLO SERVIDOR GRAPHQL           │                                   │${NC}"
log "${WHITE}│  │  • 500+ tipos                       │                                   │${NC}"
log "${WHITE}│  │  • 50 desarrolladores               │  ← Caos, conflictos, bloqueos    │${NC}"
log "${WHITE}│  │  • Un deploy rompe todo             │                                   │${NC}"
log "${WHITE}│  └─────────────────────────────────────┘                                   │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  SOLUCIÓN: Federation - Dividir en microservicios                          │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│                    ┌─────────────────────┐                                 │${NC}"
log "${WHITE}│                    │   Apollo Gateway    │  ← UN endpoint para clientes   │${NC}"
log "${WHITE}│                    └──────────┬──────────┘                                 │${NC}"
log "${WHITE}│              ┌────────────────┼────────────────┐                           │${NC}"
log "${WHITE}│              ▼                ▼                ▼                           │${NC}"
log "${WHITE}│      ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                    │${NC}"
log "${WHITE}│      │Users Service│  │Loans Service│  │Pay Service  │                    │${NC}"
log "${WHITE}│      │ (Equipo A)  │  │ (Equipo B)  │  │ (Equipo C)  │                    │${NC}"
log "${WHITE}│      └─────────────┘  └─────────────┘  └─────────────┘                    │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  BENEFICIOS:                                                               │${NC}"
log "${WHITE}│  ✅ Cada equipo maneja su servicio                                         │${NC}"
log "${WHITE}│  ✅ Deploy independiente                                                   │${NC}"
log "${WHITE}│  ✅ Escalar por servicio                                                   │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 1: Query básica a Users Service
print_subsection "Test 1: Query al Subgrafo USERS - Lista de usuarios"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # users-schema.graphqls (Users Service - Puerto 8081)             │
   │  # Este servicio es OWNER del tipo User                            │
   │                                                                     │
   │  extend schema                                                      │
   │    @link(url: \"https://specs.apollo.dev/federation/v2.3\",          │
   │          import: [\"@key\", \"@shareable\"])                           │
   │                                                                     │
   │  \"\"\"                                                               │
   │  User - Entidad principal del dominio Users                        │
   │  Marcada con @key para permitir referencias desde otros subgrafos  │
   │  \"\"\"                                                               │
   │  type User @key(fields: \"id\") {      ← ENTIDAD FEDERADA           │
   │    id: ID!                            ← Campo clave (@key)         │
   │    email: String!                                                  │
   │    fullName: String!                                               │
   │    userType: UserType!                # LENDER | BORROWER | BOTH   │
   │    lenderProfile: LenderProfile                                    │
   │    borrowerProfile: BorrowerProfile                                │
   │    reputation: Float!                                              │
   │  }                                                                 │
   │                                                                     │
   │  type Query {                                                       │
   │    user(id: ID!): User                                             │
   │    users: [User!]!                    ← ESTA QUERY                 │
   │    verifiedLenders: [User!]!                                       │
   │  }                                                                 │
   └─────────────────────────────────────────────────────────────────────┘"

run_graphql_test "Query users - Subgrafo Users" \
    "$USERS_URL" \
    "Users Service (8081)" \
    '{ users { id fullName email userType } }' \
    'user-001.*Alice'

# Test 2: Query por ID
print_subsection "Test 2: Query User por ID con argumentos"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # users-schema.graphqls                                           │
   │                                                                     │
   │  type Query {                                                       │
   │    user(id: ID!): User                ← ESTA QUERY (con argumento) │
   │    users: [User!]!                                                 │
   │    verifiedLenders: [User!]!                                       │
   │  }                                                                 │
   │                                                                     │
   │  # IMPORTANTE: Este servicio SOLO conoce Users.                    │
   │  # No tiene acceso a Loans - cada subgrafo es independiente.       │
   └─────────────────────────────────────────────────────────────────────┘"

run_graphql_test "Query user por ID" \
    "$USERS_URL" \
    "Users Service (8081)" \
    '{ user(id: \"user-001\") { id fullName email reputation } }' \
    'Alice Thompson'

################################################################################
# SECCIÓN 5.2 - DIRECTIVA @KEY (ENTIDADES FEDERADAS)
################################################################################

print_section "SECCIÓN 5.2 — DIRECTIVA @key: ENTIDADES FEDERADAS"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Entender la directiva @key que marca entidades federadas.                 │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ¿QUÉ ES @key?                                                             │${NC}"
log "${WHITE}│  Marca un tipo como \"entidad\" que puede ser referenciada desde otros      │${NC}"
log "${WHITE}│  servicios. Es como el \"pasaporte\" del tipo.                              │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ANALOGÍA:                                                                 │${NC}"
log "${WHITE}│  @key es como el número de pasaporte de una persona.                       │${NC}"
log "${WHITE}│  No importa en qué país estés, con ese número te identifican.              │${NC}"
log "${WHITE}│  En Federation, @key permite que otros servicios \"encuentren\"             │${NC}"
log "${WHITE}│  la entidad usando solo su identificador.                                  │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 3: Objetos anidados
print_subsection "Test 3: Query con tipos anidados (LenderProfile)"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # users-schema.graphqls                                           │
   │                                                                     │
   │  type User @key(fields: \"id\") {                                    │
   │    id: ID!                                                         │
   │    fullName: String!                                               │
   │    lenderProfile: LenderProfile       ← TIPO ANIDADO              │
   │    borrowerProfile: BorrowerProfile   ← TIPO ANIDADO              │
   │  }                                                                 │
   │                                                                     │
   │  \"\"\"                                                               │
   │  Perfil de prestamista (Lender)                                    │
   │  NOTA: NO tiene @key - NO es entidad federada                      │
   │  Solo Users Service puede resolver este tipo                       │
   │  \"\"\"                                                               │
   │  type LenderProfile {                                              │
   │    totalLent: Float!                  # Total prestado             │
   │    activeLoans: Int!                  # Préstamos activos          │
   │    averageReturn: Float!              # Retorno promedio           │
   │    riskTolerance: RiskTolerance!      # CONSERVATIVE|MODERATE|AGG  │
   │    verified: Boolean!                 # ¿Verificado?               │
   │  }                                                                 │
   │                                                                     │
   │  type Query {                                                       │
   │    verifiedLenders: [User!]!          ← ESTA QUERY                 │
   │  }                                                                 │
   └─────────────────────────────────────────────────────────────────────┘"

run_graphql_test "Query verifiedLenders con LenderProfile" \
    "$USERS_URL" \
    "Users Service (8081)" \
    '{ verifiedLenders { id fullName lenderProfile { verified totalLent activeLoans } } }' \
    'verifiedLenders.*totalLent'

# Test 4: Entity Resolution (_entities)
print_subsection "Test 4: Entity Resolution con _entities query"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # QUERY ESPECIAL DE FEDERATION: _entities                         │
   │  #                                                                  │
   │  # Esta query NO la defines tú - Apollo Federation la genera       │
   │  # automáticamente para cada tipo marcado con @key                 │
   │                                                                     │
   │  type Query {                                                       │
   │    # Queries normales que defines tú:                              │
   │    user(id: ID!): User                                             │
   │    users: [User!]!                                                 │
   │                                                                     │
   │    # Query GENERADA por Federation (no aparece en tu schema):      │
   │    _entities(representations: [_Any!]!): [_Entity]!                │
   │  }                                                                 │
   │                                                                     │
   │  # ¿CÓMO FUNCIONA?                                                  │
   │  # 1. Loans Service retorna stub: {__typename:\"User\", id:\"001\"}  │
   │  # 2. Gateway detecta que User pertenece a Users Service           │
   │  # 3. Gateway llama: _entities(representations: [{...}])           │
   │  # 4. Users Service resuelve y retorna User completo               │
   └─────────────────────────────────────────────────────────────────────┘"

print_java "   @DgsEntityFetcher(name = \"User\")           // Resuelve User por @key
   public User resolveUser(Map<String, Object> values) {
       String id = (String) values.get(\"id\");  // Extrae el campo @key
       return usersService.getUserById(id);
   }"

run_graphql_test "Entity Resolution - _entities query" \
    "$USERS_URL" \
    "Users Service (8081)" \
    '{ _entities(representations: [{__typename: \"User\", id: \"user-001\"}]) { ... on User { id fullName email } } }' \
    'Alice Thompson'

################################################################################
# SECCIÓN 5.3 - SEGUNDO SUBGRAFO: LOANS SERVICE
################################################################################

print_section "SECCIÓN 5.3 — SEGUNDO SUBGRAFO: LOANS SERVICE"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Entender cómo el segundo subgrafo define sus propios tipos.               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ARQUITECTURA DE PUERTOS:                                                  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ┌─────────────────────────────────────────────────────────────────┐       │${NC}"
log "${WHITE}│  │  Users Service    →  Puerto 8081  →  Owner de User            │       │${NC}"
log "${WHITE}│  │  Loans Service    →  Puerto 8082  →  Owner de Loan            │       │${NC}"
log "${WHITE}│  │  Gateway          →  Puerto 8080  →  Unifica ambos            │       │${NC}"
log "${WHITE}│  └─────────────────────────────────────────────────────────────────┘       │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Cada servicio es COMPLETAMENTE INDEPENDIENTE:                             │${NC}"
log "${WHITE}│  • Base de datos propia                                                    │${NC}"
log "${WHITE}│  • Deploy propio                                                           │${NC}"
log "${WHITE}│  • Equipo propio                                                           │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 5: Query al Loans Service
print_subsection "Test 5: Query al Subgrafo LOANS - Lista de préstamos"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # loans-schema.graphqls (Loans Service - Puerto 8082)             │
   │  # Este servicio es OWNER del tipo Loan                            │
   │                                                                     │
   │  extend schema                                                      │
   │    @link(url: \"https://specs.apollo.dev/federation/v2.3\",          │
   │          import: [\"@key\", \"@external\", \"@extends\"])               │
   │                                                                     │
   │  \"\"\"                                                               │
   │  Loan - Entidad principal del dominio Loans (P2P Lending)          │
   │  \"\"\"                                                               │
   │  type Loan @key(fields: \"id\") {      ← ENTIDAD FEDERADA           │
   │    id: ID!                                                         │
   │    amount: Float!                     # Monto del préstamo         │
   │    interestRate: Float!               # Tasa de interés anual      │
   │    term: Int!                         # Plazo en meses             │
   │    status: LoanStatus!                # Estado del préstamo        │
   │    purpose: String!                   # Para qué es                │
   │    lender: User                       # Prestamista (nullable)     │
   │    borrower: User!                    # Prestatario                │
   │    monthlyPayment: Float!             # Cuota mensual              │
   │  }                                                                 │
   │                                                                     │
   │  type Query {                                                       │
   │    loans: [Loan!]!                    ← ESTA QUERY                 │
   │    loan(id: ID!): Loan                                             │
   │    loansByStatus(status: LoanStatus!): [Loan!]!                    │
   │  }                                                                 │
   └─────────────────────────────────────────────────────────────────────┘"

run_graphql_test "Query loans - Subgrafo Loans" \
    "$LOANS_URL" \
    "Loans Service (8082)" \
    '{ loans { id amount status purpose interestRate term } }' \
    'loan-001.*ACTIVE'

# Test 6: Filtro por Enum
print_subsection "Test 6: Query con filtro por Enum (LoanStatus)"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # loans-schema.graphqls                                           │
   │                                                                     │
   │  enum LoanStatus {                                                 │
   │    PENDING       # Esperando financiamiento (sin lender)           │
   │    FUNDED        # Financiado, esperando activación                │
   │    ACTIVE        # Préstamo activo, pagos en curso                 │
   │    COMPLETED     # Pagado completamente                            │
   │    DEFAULTED     # Incumplimiento de pago                          │
   │  }                                                                 │
   │                                                                     │
   │  type Query {                                                       │
   │    loans: [Loan!]!                                                 │
   │    loansByStatus(status: LoanStatus!): [Loan!]!  ← ESTA QUERY     │
   │    availableLoans: [Loan!]!           # Shortcut: solo PENDING     │
   │  }                                                                 │
   └─────────────────────────────────────────────────────────────────────┘"

run_graphql_test "Query loans filtrado por status ACTIVE" \
    "$LOANS_URL" \
    "Loans Service (8082)" \
    '{ loansByStatus(status: ACTIVE) { id amount status } }' \
    'ACTIVE'

################################################################################
# SECCIÓN 5.4 - DIRECTIVA @EXTENDS (EXTENDER TIPOS)
################################################################################

print_section "SECCIÓN 5.4 — DIRECTIVA @extends: EXTENDER TIPOS DE OTROS SERVICIOS"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Entender cómo un servicio AGREGA CAMPOS a tipos de otro servicio.         │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  DIRECTIVAS CLAVE:                                                         │${NC}"
log "${WHITE}│  ┌─────────────────────────────────────────────────────────────────┐       │${NC}"
log "${WHITE}│  │  @extends   \"Voy a extender un tipo que NO es mío\"             │       │${NC}"
log "${WHITE}│  │  @external  \"Este campo ya existe en otro servicio\"            │       │${NC}"
log "${WHITE}│  └─────────────────────────────────────────────────────────────────┘       │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ANALOGÍA:                                                                 │${NC}"
log "${WHITE}│  Es como agregar una extensión a una casa que no construiste.              │${NC}"
log "${WHITE}│  La casa original (User) la hizo Users Service.                            │${NC}"
log "${WHITE}│  Loans Service le agrega un \"cuarto nuevo\" (loansAsLender).               │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 7: Referencias entre subgrafos (Stubs)
print_subsection "Test 7: Referencias entre subgrafos (User Stubs)"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # loans-schema.graphqls                                           │
   │  # Loans Service EXTIENDE el tipo User (que pertenece a Users)     │
   │                                                                     │
   │  \"\"\"                                                               │
   │  User - Referencia externa desde el subgrafo Users                 │
   │  Usamos @extends para agregar campos al tipo User                  │
   │  \"\"\"                                                               │
   │  type User @key(fields: \"id\") @extends {    ← NO SOY OWNER        │
   │    id: ID! @external                         ← Campo de Users     │
   │    \"\"\"                                                             │
   │    Préstamos donde el usuario es prestamista                       │
   │    Campo agregado por el subgrafo Loans                            │
   │    \"\"\"                                                             │
   │    loansAsLender: [Loan!]!                   ← CAMPO NUEVO         │
   │    \"\"\"                                                             │
   │    Préstamos donde el usuario es prestatario                       │
   │    \"\"\"                                                             │
   │    loansAsBorrower: [Loan!]!                 ← CAMPO NUEVO         │
   │  }                                                                 │
   │                                                                     │
   │  # Y en el tipo Loan, referenciamos a User:                        │
   │  type Loan @key(fields: \"id\") {                                    │
   │    id: ID!                                                         │
   │    amount: Float!                                                  │
   │    \"\"\"                                                             │
   │    Prestamista - Referencia a User del otro subgrafo               │
   │    Nullable porque préstamos PENDING no tienen lender              │
   │    \"\"\"                                                             │
   │    lender: User                              ← REF (nullable)      │
   │    \"\"\"                                                             │
   │    Prestatario - Referencia a User del otro subgrafo               │
   │    \"\"\"                                                             │
   │    borrower: User!                           ← REF (obligatorio)   │
   │  }                                                                 │
   └─────────────────────────────────────────────────────────────────────┘"

print_java "   // Loans Service retorna \"stubs\" - solo {__typename, id}
   // El Gateway después resuelve el User completo con _entities
   @DgsData(parentType = \"Loan\", field = \"borrower\")
   public Map<String, Object> borrower(DataFetchingEnvironment dfe) {
       Loan loan = dfe.getSource();
       return Map.of(\"__typename\", \"User\", \"id\", loan.getBorrowerId());
   }"

run_graphql_test "Loans con referencias a Users (stubs)" \
    "$LOANS_URL" \
    "Loans Service (8082)" \
    '{ loans { id amount borrower { id } lender { id } } }' \
    'borrower.*id.*lender'

################################################################################
# SECCIÓN 5.5 - MUTATIONS EN FEDERATION
################################################################################

print_section "SECCIÓN 5.5 — MUTATIONS EN SERVICIOS FEDERADOS"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Las mutations funcionan igual que en GraphQL normal.                      │${NC}"
log "${WHITE}│  Cada servicio define y ejecuta sus propias mutations.                     │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  REGLA IMPORTANTE:                                                         │${NC}"
log "${WHITE}│  ┌─────────────────────────────────────────────────────────────────┐       │${NC}"
log "${WHITE}│  │  Una mutation pertenece a UN SOLO servicio.                     │       │${NC}"
log "${WHITE}│  │  NO se puede dividir una mutation entre servicios.              │       │${NC}"
log "${WHITE}│  │                                                                  │       │${NC}"
log "${WHITE}│  │  ✅ createUser    → Users Service                               │       │${NC}"
log "${WHITE}│  │  ✅ createLoan    → Loans Service                               │       │${NC}"
log "${WHITE}│  │  ❌ createUserWithLoan → NO SE PUEDE (cruza servicios)          │       │${NC}"
log "${WHITE}│  └─────────────────────────────────────────────────────────────────┘       │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 8: Mutation createUser
print_subsection "Test 8: Mutation createUser (Users Service)"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # users-schema.graphqls                                           │
   │                                                                     │
   │  input CreateUserInput {                                           │
   │    email: String!                     # Email del usuario          │
   │    fullName: String!                  # Nombre completo            │
   │    userType: UserType!                # LENDER | BORROWER | BOTH   │
   │  }                                                                 │
   │                                                                     │
   │  type UserResponse {                                               │
   │    success: Boolean!                  # ¿Operación exitosa?        │
   │    message: String!                   # Mensaje descriptivo        │
   │    user: User                         # Usuario creado (si éxito)  │
   │  }                                                                 │
   │                                                                     │
   │  type Mutation {                                                    │
   │    createUser(input: CreateUserInput!): UserResponse!  ← ESTA     │
   │  }                                                                 │
   └─────────────────────────────────────────────────────────────────────┘"

run_graphql_test "Mutation createUser" \
    "$USERS_URL" \
    "Users Service (8081)" \
    'mutation { createUser(input: {email: \"nuevo@test.com\", fullName: \"Nuevo Usuario\", userType: LENDER}) { success message user { id fullName } } }' \
    'success.*true'

# Test 9: Mutation createLoanRequest
print_subsection "Test 9: Mutation createLoanRequest (Loans Service)"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # loans-schema.graphqls                                           │
   │                                                                     │
   │  # FLUJO DE NEGOCIO P2P LENDING:                                   │
   │  # 1. Borrower solicita préstamo → createLoanRequest → PENDING     │
   │  # 2. Lender financia            → fundLoan          → FUNDED      │
   │  # 3. Se activa                                      → ACTIVE      │
   │  # 4. Borrower paga cuotas                                         │
   │  # 5. Completa                                       → COMPLETED   │
   │                                                                     │
   │  input CreateLoanInput {                                           │
   │    borrowerId: ID!                    # Quién pide el préstamo     │
   │    amount: Float!                     # Monto solicitado           │
   │    interestRate: Float!               # Tasa de interés anual      │
   │    term: Int!                         # Plazo en meses             │
   │    purpose: String!                   # Para qué es el préstamo    │
   │  }                                                                 │
   │                                                                     │
   │  type LoanResponse {                                               │
   │    success: Boolean!                                               │
   │    message: String!                                                │
   │    loan: Loan                                                      │
   │  }                                                                 │
   │                                                                     │
   │  type Mutation {                                                    │
   │    createLoanRequest(input: CreateLoanInput!): LoanResponse!       │
   │    fundLoan(loanId: ID!, lenderId: ID!): LoanResponse!             │
   │  }                                                                 │
   └─────────────────────────────────────────────────────────────────────┘"

run_graphql_test "Mutation createLoanRequest" \
    "$LOANS_URL" \
    "Loans Service (8082)" \
    'mutation { createLoanRequest(input: {borrowerId: \"user-003\", amount: 5000, interestRate: 8.5, term: 12, purpose: \"Test loan federation\"}) { success message loan { id amount status } } }' \
    'success.*true'

################################################################################
# RESUMEN FINAL
################################################################################

print_section "📊 RESUMEN DE TESTS - CHAPTER 05"

PASS_RATE=0
if [ $TOTAL_TESTS -gt 0 ]; then
    PASS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
fi

log "${CYAN}Tests Totales:    ${TOTAL_TESTS}${NC}"
log "${GREEN}Tests Exitosos:   ${PASSED_TESTS}${NC}"
log "${RED}Tests Fallidos:   ${FAILED_TESTS}${NC}"
log "${YELLOW}Tasa de Éxito:    ${PASS_RATE}%${NC}"
log ""

if [ $FAILED_TESTS -eq 0 ]; then
    log "${GREEN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    log "${GREEN}║                                                                              ║${NC}"
    log "${GREEN}║                   🎉 ¡TODOS LOS TESTS PASARON! 🎉                           ║${NC}"
    log "${GREEN}║                                                                              ║${NC}"
    log "${GREEN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
else
    log "${YELLOW}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    log "${YELLOW}║                                                                              ║${NC}"
    log "${YELLOW}║                   ⚠️  ALGUNOS TESTS FALLARON ⚠️                            ║${NC}"
    log "${YELLOW}║                                                                              ║${NC}"
    log "${YELLOW}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
fi

log ""
log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  📚 RESUMEN: DIRECTIVAS DE APOLLO FEDERATION                               │${NC}"
log "${WHITE}├─────────────────────────────────────────────────────────────────────────────┤${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  @key(fields: \"id\")     Marca tipo como entidad federada                  │${NC}"
log "${WHITE}│                          Permite referencias desde otros servicios         │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  @extends                Extiende un tipo definido en otro servicio        │${NC}"
log "${WHITE}│                          \"No soy owner, pero agrego campos\"               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  @external               Referencia un campo de otro servicio              │${NC}"
log "${WHITE}│                          \"Este campo existe, pero no es mío\"              │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}├─────────────────────────────────────────────────────────────────────────────┤${NC}"
log "${WHITE}│  📐 SCHEMAS DE ESTE CAPÍTULO                                               │${NC}"
log "${WHITE}├─────────────────────────────────────────────────────────────────────────────┤${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  users-schema.graphqls (8081):                                             │${NC}"
log "${WHITE}│    type User @key(fields: \"id\") { ... }     ← OWNER                      │${NC}"
log "${WHITE}│    type LenderProfile { ... }                                              │${NC}"
log "${WHITE}│    type BorrowerProfile { ... }                                            │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  loans-schema.graphqls (8082):                                             │${NC}"
log "${WHITE}│    type Loan @key(fields: \"id\") { ... }     ← OWNER                      │${NC}"
log "${WHITE}│    type User @key @extends {                  ← EXTIENDE                  │${NC}"
log "${WHITE}│      id: ID! @external                                                     │${NC}"
log "${WHITE}│      loansAsLender: [Loan!]!                  ← Campo nuevo               │${NC}"
log "${WHITE}│      loansAsBorrower: [Loan!]!                ← Campo nuevo               │${NC}"
log "${WHITE}│    }                                                                       │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
log "${CYAN}📄 Log completo guardado en: ${OUTPUT_FILE}${NC}"
log ""
log "${WHITE}ARQUITECTURA FINAL:${NC}"
log "${YELLOW}  ┌─────────────────────────────────────────────────────────────────────┐${NC}"
log "${YELLOW}  │                        Apollo Gateway (8080)                        │${NC}"
log "${YELLOW}  │                      UN endpoint para clientes                      │${NC}"
log "${YELLOW}  └─────────────────────────────┬───────────────────────────────────────┘${NC}"
log "${YELLOW}                                │                                        ${NC}"
log "${YELLOW}                ┌───────────────┴───────────────┐                        ${NC}"
log "${YELLOW}                ▼                               ▼                        ${NC}"
log "${YELLOW}  ┌─────────────────────────┐     ┌─────────────────────────┐           ${NC}"
log "${YELLOW}  │   Users Service (8081)  │     │   Loans Service (8082)  │           ${NC}"
log "${YELLOW}  │   • User @key           │     │   • Loan @key           │           ${NC}"
log "${YELLOW}  │   • LenderProfile       │     │   • User @extends       │           ${NC}"
log "${YELLOW}  │   • BorrowerProfile     │     │     - loansAsLender     │           ${NC}"
log "${YELLOW}  └─────────────────────────┘     │     - loansAsBorrower   │           ${NC}"
log "${YELLOW}                                  └─────────────────────────┘           ${NC}"
log ""

exit $FAILED_TESTS