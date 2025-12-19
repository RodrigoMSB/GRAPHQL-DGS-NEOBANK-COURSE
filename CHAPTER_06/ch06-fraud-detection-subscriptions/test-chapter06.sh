#!/usr/bin/env bash

################################################################################
# CHAPTER 06: REAL-TIME FRAUD DETECTION - GRAPHQL SUBSCRIPTIONS
# Script de Testing Automatizado - VERSIÓN EDUCATIVA CON SCHEMAS COMPLETOS
################################################################################

export LC_ALL=C

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
MAGENTA='\033[1;35m'
CYAN='\033[0;36m'
BLUE='\033[0;34m'
WHITE='\033[1;37m'
GRAY='\033[0;90m'
NC='\033[0m'

GRAPHQL_URL="http://localhost:8080/graphql"
OUTPUT_FILE="test-results-chapter06-$(date +%Y%m%d-%H%M%S).txt"

INTERACTIVE=true
[ "$1" = "-s" ] && INTERACTIVE=false

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

log() { printf "%b\n" "$1" | tee -a "$OUTPUT_FILE"; }

pause() {
    if [ "$INTERACTIVE" = true ]; then
        log "${YELLOW}⏸️  Presiona Enter para continuar...${NC}"
        read -r
    fi
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

run_graphql_test() {
    local test_name="$1"
    local graphql_query="$2"
    local validation="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log "${YELLOW}🧪 Test #${TOTAL_TESTS}: ${test_name}${NC}"
    log ""
    log "${BLUE}📤 REQUEST:${NC}"
    log "${WHITE}   POST ${GRAPHQL_URL}${NC}"
    log "${WHITE}   Content-Type: application/json${NC}"
    log ""
    
    local display_query
    display_query=$(echo "$graphql_query" | sed 's/\\"/"/g')
    
    log "${BLUE}📋 BODY:${NC}"
    log "${GRAY}   {${NC}"
    log "${GRAY}     \"query\": \"${CYAN}${display_query}${GRAY}\"${NC}"
    log "${GRAY}   }${NC}"
    log ""
    log "${BLUE}⚡ Ejecutando...${NC}"
    
    response=$(curl -s -X POST "${GRAPHQL_URL}" \
        -H "Content-Type: application/json" \
        -d "{\"query\":\"$graphql_query\"}" 2>&1)
    exit_code=$?
    
    log ""
    log "${BLUE}📥 RESPONSE:${NC}"
    if command -v jq >/dev/null 2>&1; then
        echo "$response" | jq '.' 2>/dev/null | while IFS= read -r line; do
            log "${GREEN}   $line${NC}"
        done
    else
        log "${GREEN}   $response${NC}"
    fi
    
    log ""
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

check_service() {
    log "${YELLOW}🔍 Verificando servicio...${NC}"
    if curl -s -X POST "$GRAPHQL_URL" -H "Content-Type: application/json" -d '{"query":"{__typename}"}' > /dev/null 2>&1; then
        log "${GREEN}   ✅ Fraud Detection Service (8080): OK${NC}"
    else
        log "${RED}   ❌ Fraud Detection Service (8080): NO RESPONDE${NC}"
        log "${YELLOW}Ejecuta: ./mvnw spring-boot:run${NC}"
        exit 1
    fi
    log ""
}

################################################################################
# INICIO
################################################################################

clear
log "${CYAN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
log "${CYAN}║     📘 CHAPTER 06: REAL-TIME FRAUD DETECTION                                ║${NC}"
log "${CYAN}║              GraphQL Subscriptions + WebSockets                              ║${NC}"
log "${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
log ""

check_service
pause

################################################################################
# SECCIÓN 6.1 - INTRODUCCIÓN A SUBSCRIPTIONS
################################################################################

print_section "SECCIÓN 6.1 — ¿QUÉ SON LAS SUBSCRIPTIONS?"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Entender Subscriptions: la tercera operación de GraphQL.                  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│  📚 LAS 3 OPERACIONES DE GRAPHQL                                           │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  1️⃣  QUERY (Lectura)                                                       │${NC}"
log "${WHITE}│      Cliente: \"Dame los datos de X\"                                       │${NC}"
log "${WHITE}│      Servidor: \"Aquí tienes\" (una vez)                                    │${NC}"
log "${WHITE}│      Analogía: Pedir un café en la barra ☕                                │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  2️⃣  MUTATION (Escritura)                                                  │${NC}"
log "${WHITE}│      Cliente: \"Crea/Modifica/Elimina X\"                                   │${NC}"
log "${WHITE}│      Servidor: \"Listo, aquí está el resultado\" (una vez)                  │${NC}"
log "${WHITE}│      Analogía: Hacer un pedido en el mostrador 🛒                          │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  3️⃣  SUBSCRIPTION (Streaming) ← NUEVO                                      │${NC}"
log "${WHITE}│      Cliente: \"Avísame cada vez que pase X\"                               │${NC}"
log "${WHITE}│      Servidor: \"OK... evento 1... evento 2... evento 3...\" (continuo)     │${NC}"
log "${WHITE}│      Analogía: Suscribirte a notificaciones de tu banco 📱                │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🔄 POLLING vs SUBSCRIPTION - ¿Por qué importa?                            │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│  ❌ POLLING (la manera antigua y mala)                                     │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│     Cliente                              Servidor                          │${NC}"
log "${WHITE}│        │                                    │                              │${NC}"
log "${WHITE}│        │──── \"¿Hay fraude?\" ───────────────►│                              │${NC}"
log "${WHITE}│        │◄─── \"No\" ──────────────────────────│                              │${NC}"
log "${WHITE}│        │     (espera 2 seg)                 │                              │${NC}"
log "${WHITE}│        │──── \"¿Hay fraude?\" ───────────────►│                              │${NC}"
log "${WHITE}│        │◄─── \"No\" ──────────────────────────│                              │${NC}"
log "${WHITE}│        │     (espera 2 seg)                 │                              │${NC}"
log "${WHITE}│        │──── \"¿Hay fraude?\" ───────────────►│                              │${NC}"
log "${WHITE}│        │◄─── \"¡SÍ! Alerta crítica\" ─────────│  ← Detectó 2 seg tarde      │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│     PROBLEMAS: Desperdicio de recursos, latencia, carga innecesaria        │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│  ✅ SUBSCRIPTION (la manera moderna y eficiente)                           │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│     Cliente                              Servidor                          │${NC}"
log "${WHITE}│        │                                    │                              │${NC}"
log "${WHITE}│        │══ WebSocket (conexión abierta) ════│                              │${NC}"
log "${WHITE}│        │──── \"Avísame si hay fraude\" ──────►│                              │${NC}"
log "${WHITE}│        │     (silencio... conexión viva)    │                              │${NC}"
log "${WHITE}│        │     (pasan 5 minutos...)           │                              │${NC}"
log "${WHITE}│        │◄─── \"¡ALERTA! Fraude detectado\" ───│  ← INSTANTÁNEO 🔥           │${NC}"
log "${WHITE}│        │◄─── \"¡OTRA ALERTA!\" ───────────────│  ← INSTANTÁNEO 🔥           │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│     VENTAJAS: Tiempo real, eficiente, sin desperdicio                      │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 1: Query - Ver transacciones existentes
print_subsection "Test 1: Query - Transacciones existentes de una cuenta"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # fraud-schema.graphqls                                           │
   │                                                                     │
   │  \"\"\"                                                               │
   │  Transaction - Representa una transacción bancaria                 │
   │  Cada transacción es analizada por el sistema de detección         │
   │  \"\"\"                                                               │
   │  type Transaction {                                                │
   │    id: ID!                          # Identificador único          │
   │    accountId: String!               # Cuenta del cliente           │
   │    amount: Float!                   # Monto de la transacción      │
   │    currency: String!                # USD, EUR, MXN, etc.          │
   │    merchantName: String!            # Nombre del comercio          │
   │    category: String!                # Categoría (Shopping, etc.)   │
   │    location: String!                # Ubicación geográfica         │
   │    timestamp: String!               # Fecha/hora ISO               │
   │    riskScore: Float!                # Puntuación de riesgo (0-100) │
   │    status: TransactionStatus!       # Estado actual                │
   │  }                                                                 │
   │                                                                     │
   │  \"\"\"                                                               │
   │  Estados posibles de una transacción                               │
   │  \"\"\"                                                               │
   │  enum TransactionStatus {                                          │
   │    PENDING      # En proceso de análisis                           │
   │    APPROVED     # Aprobada (riskScore = 0)                         │
   │    REJECTED     # Rechazada por el sistema                         │
   │    FLAGGED      # Marcada como sospechosa (riskScore >= 50) ⚠️     │
   │  }                                                                 │
   │                                                                     │
   │  type Query {                                                       │
   │    \"\"\"                                                             │
   │    Obtener todas las transacciones de una cuenta                   │
   │    \"\"\"                                                             │
   │    transactions(accountId: String!): [Transaction!]!               │
   │  }                                                                 │
   └─────────────────────────────────────────────────────────────────────┘"

run_graphql_test "Query transactions de account-001" \
    '{ transactions(accountId: \"account-001\") { id amount merchantName status riskScore } }' \
    'transactions'

################################################################################
# SECCIÓN 6.2 - SISTEMA DE DETECCIÓN DE FRAUDE
################################################################################

print_section "SECCIÓN 6.2 — SISTEMA DE DETECCIÓN DE FRAUDE"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Entender las 6 reglas de detección de fraude y el sistema de scoring.     │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  🚨 REGLAS DE DETECCIÓN (Risk Scoring):                                    │${NC}"
log "${WHITE}│  ┌─────────────────────────────────────────────────────────────────┐       │${NC}"
log "${WHITE}│  │  #  │ REGLA                    │ PUNTOS │ DESCRIPCIÓN           │       │${NC}"
log "${WHITE}│  ├─────┼──────────────────────────┼────────┼───────────────────────┤       │${NC}"
log "${WHITE}│  │  1  │ Monto Inusual            │  +30   │ >3x promedio cuenta   │       │${NC}"
log "${WHITE}│  │  2  │ Ubicación Sospechosa     │  +40   │ Nigeria, Russia...    │       │${NC}"
log "${WHITE}│  │  3  │ Categoría de Alto Riesgo │  +25   │ Gambling, Crypto      │       │${NC}"
log "${WHITE}│  │  4  │ Velocity Check           │  +20   │ >3 txn en 5 min       │       │${NC}"
log "${WHITE}│  │  5  │ Hora Inusual             │  +15   │ 3 AM - 5 AM           │       │${NC}"
log "${WHITE}│  │  6  │ Monto Redondo            │  +10   │ Múltiplo de \$1000     │       │${NC}"
log "${WHITE}│  └─────────────────────────────────────────────────────────────────┘       │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  📊 NIVELES DE RIESGO (basado en suma de puntos):                          │${NC}"
log "${WHITE}│  ┌─────────────────────────────────────────────────────────────────┐       │${NC}"
log "${WHITE}│  │  Score 0-24   → LOW      → Monitor closely                     │       │${NC}"
log "${WHITE}│  │  Score 25-49  → MEDIUM   → Flag for manual review              │       │${NC}"
log "${WHITE}│  │  Score 50-79  → HIGH     → Require 2FA verification            │       │${NC}"
log "${WHITE}│  │  Score 80+    → CRITICAL → BLOCK immediately 🚫                │       │${NC}"
log "${WHITE}│  └─────────────────────────────────────────────────────────────────┘       │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 2: Mutation - Procesar transacción
print_subsection "Test 2: Mutation - Procesar una nueva transacción"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # fraud-schema.graphqls                                           │
   │                                                                     │
   │  \"\"\"                                                               │
   │  Input para procesar una nueva transacción                         │
   │  \"\"\"                                                               │
   │  input TransactionInput {                                          │
   │    accountId: String!               # Cuenta del cliente           │
   │    amount: Float!                   # Monto de la transacción      │
   │    currency: String!                # Moneda (USD, EUR, etc.)      │
   │    merchantName: String!            # Nombre del comercio          │
   │    category: String!                # Categoría del gasto          │
   │    location: String!                # Ciudad, País                 │
   │  }                                                                 │
   │                                                                     │
   │  \"\"\"                                                               │
   │  Respuesta del procesamiento de transacción                        │
   │  Incluye la transacción procesada y posible alerta de fraude       │
   │  \"\"\"                                                               │
   │  type TransactionResponse {                                        │
   │    success: Boolean!                # ¿Se procesó correctamente?   │
   │    message: String!                 # Mensaje descriptivo          │
   │    transaction: Transaction         # Transacción procesada        │
   │    fraudAlert: FraudAlert           # null si no hay fraude        │
   │  }                                                                 │
   │                                                                     │
   │  type Mutation {                                                    │
   │    \"\"\"                                                             │
   │    Procesar una nueva transacción                                  │
   │    Trigger: Ejecuta el análisis de fraude automáticamente          │
   │    \"\"\"                                                             │
   │    processTransaction(input: TransactionInput!): TransactionResponse!
   │  }                                                                 │
   │                                                                     │
   │  # FLUJO INTERNO:                                                   │
   │  # 1. Cliente envía mutation con datos de transacción              │
   │  # 2. FraudDetectionService aplica las 6 reglas                    │
   │  # 3. Calcula riskScore sumando puntos de reglas violadas          │
   │  # 4. Si riskScore > 0 → genera FraudAlert                         │
   │  # 5. Si hay suscriptores → les envía la alerta en tiempo real     │
   └─────────────────────────────────────────────────────────────────────┘"

log "${WHITE}   📝 ESCENARIO: Transacción pequeña en comercio conocido${NC}"
log "${WHITE}   💰 Monto: \$75 USD | 🏪 Comercio: Target | 📍 San Francisco, US${NC}"
log "${WHITE}   ℹ️  NOTA: El sistema puede detectar \"Velocity Check\" si hay muchas${NC}"
log "${WHITE}       transacciones recientes (>3 en 5 min), lo cual es normal en testing.${NC}"
log ""

run_graphql_test "Procesar transacción - Target \$75" \
    'mutation { processTransaction(input: {accountId: \"account-001\", amount: 75, currency: \"USD\", merchantName: \"Target\", category: \"Shopping\", location: \"San Francisco, US\"}) { success message transaction { id status riskScore } fraudAlert { id riskLevel reasons } } }' \
    'success.*true'

################################################################################
# SECCIÓN 6.3 - GENERACIÓN DE ALERTAS DE FRAUDE
################################################################################

print_section "SECCIÓN 6.3 — GENERACIÓN DE ALERTAS DE FRAUDE"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Probar que el sistema detecta fraude cuando se violan las reglas.         │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  FLUJO DE DETECCIÓN:                                                       │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│     ┌──────────────────┐                                                   │${NC}"
log "${WHITE}│     │ processTransaction│  ← Cliente envía mutation                       │${NC}"
log "${WHITE}│     └────────┬─────────┘                                                   │${NC}"
log "${WHITE}│              │                                                             │${NC}"
log "${WHITE}│              ▼                                                             │${NC}"
log "${WHITE}│     ┌──────────────────┐                                                   │${NC}"
log "${WHITE}│     │ FraudDetection   │  ← Aplica 6 reglas                               │${NC}"
log "${WHITE}│     │ Service          │  ← Suma puntos de cada regla violada             │${NC}"
log "${WHITE}│     └────────┬─────────┘                                                   │${NC}"
log "${WHITE}│              │                                                             │${NC}"
log "${WHITE}│              ▼                                                             │${NC}"
log "${WHITE}│     ┌──────────────────┐     ┌──────────────────┐                         │${NC}"
log "${WHITE}│     │ riskScore = 0?   │─NO─►│ Crear FraudAlert │                         │${NC}"
log "${WHITE}│     └────────┬─────────┘     │ + Publicar 📡    │                         │${NC}"
log "${WHITE}│          SÍ  │               └──────────────────┘                         │${NC}"
log "${WHITE}│              ▼                        │                                    │${NC}"
log "${WHITE}│     ┌──────────────────┐              │                                    │${NC}"
log "${WHITE}│     │ status: APPROVED │              ▼                                    │${NC}"
log "${WHITE}│     │ (sin alerta)     │     ┌──────────────────┐                         │${NC}"
log "${WHITE}│     └──────────────────┘     │ Suscriptores     │  ← Reciben alerta      │${NC}"
log "${WHITE}│                              │ reciben push 🔥  │     INSTANTÁNEAMENTE   │${NC}"
log "${WHITE}│                              └──────────────────┘                         │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 3: Fraude por monto alto + Wire Transfer
print_subsection "Test 3: Mutation - Fraude por Monto Alto + Categoría de Riesgo"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # fraud-schema.graphqls                                           │
   │                                                                     │
   │  \"\"\"                                                               │
   │  FraudAlert - Alerta de fraude generada en tiempo real             │
   │  Se crea cuando riskScore > 0 y se publica a suscriptores          │
   │  \"\"\"                                                               │
   │  type FraudAlert {                                                 │
   │    id: ID!                          # Identificador único          │
   │    transaction: Transaction!        # Transacción sospechosa       │
   │    riskLevel: RiskLevel!            # Nivel de riesgo calculado    │
   │    reasons: [String!]!              # Lista de reglas violadas     │
   │    detectedAt: String!              # Timestamp de detección       │
   │    recommendedAction: String!       # Acción sugerida al operador  │
   │  }                                                                 │
   │                                                                     │
   │  \"\"\"                                                               │
   │  Niveles de riesgo basados en el score acumulado                   │
   │  \"\"\"                                                               │
   │  enum RiskLevel {                                                  │
   │    LOW         # Score 0-24:  \"Monitor closely\"                   │
   │    MEDIUM      # Score 25-49: \"Flag for manual review\"            │
   │    HIGH        # Score 50-79: \"Require 2FA verification\"          │
   │    CRITICAL    # Score 80+:   \"BLOCK immediately\" 🚫              │
   │  }                                                                 │
   └─────────────────────────────────────────────────────────────────────┘"

log "${WHITE}   📝 ESCENARIO: Monto muy alto + categoría de riesgo (Wire Transfer)${NC}"
log "${WHITE}   💰 Monto: \$5,000 USD (monto redondo → +10 pts)${NC}"
log "${WHITE}   🏪 Comercio: Unknown Merchant${NC}"
log "${WHITE}   📁 Categoría: Wire Transfer (alto riesgo → +25 pts)${NC}"
log "${WHITE}   📊 Risk Score esperado: 35+ → MEDIUM o superior${NC}"
log ""

run_graphql_test "Fraude detectado - Monto alto + Wire Transfer" \
    'mutation { processTransaction(input: {accountId: \"account-001\", amount: 5000, currency: \"USD\", merchantName: \"Unknown Merchant\", category: \"Wire Transfer\", location: \"San Francisco, US\"}) { success message transaction { id status riskScore } fraudAlert { id riskLevel reasons recommendedAction } } }' \
    'riskLevel.*MEDIUM|riskLevel.*HIGH|riskLevel.*CRITICAL'

# Test 4: Fraude CRITICAL - Múltiples factores
print_subsection "Test 4: Mutation - Fraude CRITICAL (Múltiples Factores)"

log "${WHITE}   📝 ESCENARIO: ¡Todas las alarmas encendidas! 🚨${NC}"
log "${WHITE}   💰 Monto: \$8,000 USD (monto redondo → +10 pts)${NC}"
log "${WHITE}   🎰 Categoría: Gambling (alto riesgo → +25 pts)${NC}"
log "${WHITE}   📍 Ubicación: Lagos, Nigeria (país sospechoso → +40 pts)${NC}"
log "${WHITE}   📊 Risk Score esperado: 75+ → HIGH o CRITICAL${NC}"
log "${WHITE}   🚫 Acción esperada: Require 2FA o BLOCK immediately${NC}"
log ""

run_graphql_test "Fraude CRITICAL - Gambling + Nigeria + Alto monto" \
    'mutation { processTransaction(input: {accountId: \"account-001\", amount: 8000, currency: \"USD\", merchantName: \"Online Casino\", category: \"Gambling\", location: \"Lagos, Nigeria\"}) { success message transaction { id status riskScore } fraudAlert { id riskLevel reasons recommendedAction } } }' \
    'CRITICAL|HIGH'

################################################################################
# SECCIÓN 6.4 - CÓMO FUNCIONAN LAS SUBSCRIPTIONS
################################################################################

print_section "SECCIÓN 6.4 — CÓMO FUNCIONAN LAS SUBSCRIPTIONS (PASO A PASO)"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Entender EXACTAMENTE cómo funcionan las Subscriptions bajo el capó.       │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│  📱 ANALOGÍA: Notificaciones de WhatsApp                                   │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Imagina que las Subscriptions son como WhatsApp:                          │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  1. Abres WhatsApp (te SUSCRIBES a mensajes)                               │${NC}"
log "${WHITE}│  2. No tienes que refrescar cada 2 segundos                                │${NC}"
log "${WHITE}│  3. Cuando alguien te escribe, te llega INSTANTÁNEAMENTE                   │${NC}"
log "${WHITE}│  4. Puedes recibir múltiples mensajes sin hacer nada                       │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  En GraphQL Subscriptions:                                                 │${NC}"
log "${WHITE}│  1. Cliente ejecuta \"subscription { fraudAlertDetected... }\"              │${NC}"
log "${WHITE}│  2. Servidor mantiene conexión WebSocket abierta                           │${NC}"
log "${WHITE}│  3. Cuando hay fraude, servidor EMPUJA la alerta al cliente               │${NC}"
log "${WHITE}│  4. Cliente recibe múltiples alertas sin hacer requests                    │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🔧 ARQUITECTURA TÉCNICA - Los 5 pasos del flujo                           │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  PASO 1: Cliente se suscribe                                               │${NC}"
log "${WHITE}│  ─────────────────────────────                                             │${NC}"
log "${WHITE}│  subscription {                                                            │${NC}"
log "${WHITE}│    fraudAlertDetected(accountId: \"account-001\") {                         │${NC}"
log "${WHITE}│      id riskLevel reasons                                                  │${NC}"
log "${WHITE}│    }                                                                       │${NC}"
log "${WHITE}│  }                                                                         │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  PASO 2: Servidor crea un \"Flux\" (stream reactivo)                        │${NC}"
log "${WHITE}│  ───────────────────────────────────────────────                           │${NC}"
log "${WHITE}│  El servidor NO retorna datos inmediatamente.                              │${NC}"
log "${WHITE}│  Retorna un Flux que \"emitirá\" datos cuando los haya.                     │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  PASO 3: Alguien procesa transacción sospechosa                            │${NC}"
log "${WHITE}│  ────────────────────────────────────────────────                          │${NC}"
log "${WHITE}│  mutation { processTransaction(input: {...casino, Nigeria...}) }           │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  PASO 4: Sistema detecta fraude y PUBLICA alerta                           │${NC}"
log "${WHITE}│  ─────────────────────────────────────────────────                         │${NC}"
log "${WHITE}│  fraudAlertPublisher.publishFraudAlert(alert)  // ← Aquí se dispara       │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  PASO 5: Todos los suscriptores reciben la alerta INSTANTÁNEAMENTE 🔥     │${NC}"
log "${WHITE}│  ──────────────────────────────────────────────────────────────────        │${NC}"
log "${WHITE}│  Sin polling, sin refresh, sin delay.                                      │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

print_subsection "Schema de Subscriptions"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # fraud-schema.graphqls                                           │
   │                                                                     │
   │  \"\"\"                                                               │
   │  Subscription - Notificaciones en tiempo real                      │
   │                                                                     │
   │  PROTOCOLO: WebSocket                                              │
   │  URL: ws://localhost:8080/subscriptions                            │
   │                                                                     │
   │  A diferencia de Query/Mutation que son request-response,          │
   │  Subscription mantiene una conexión abierta y envía datos          │
   │  cada vez que ocurre un evento relevante.                          │
   │  \"\"\"                                                               │
   │  type Subscription {                                               │
   │                                                                     │
   │    \"\"\"                                                             │
   │    fraudAlertDetected - Recibe alertas de fraude en tiempo real    │
   │                                                                     │
   │    Parámetro accountId: Filtra alertas solo para esa cuenta        │
   │    (No recibes alertas de otras cuentas)                           │
   │                                                                     │
   │    Se dispara cuando:                                              │
   │    - Alguien ejecuta processTransaction()                          │
   │    - El sistema detecta fraude (riskScore > 0)                     │
   │    - La transacción pertenece a TU cuenta                          │
   │    \"\"\"                                                             │
   │    fraudAlertDetected(accountId: String!): FraudAlert!             │
   │                                                                     │
   │    \"\"\"                                                             │
   │    transactionStatusChanged - Cambios de estado en tiempo real     │
   │                                                                     │
   │    Se dispara cuando una transacción cambia de estado:             │
   │    PENDING → APPROVED (transacción limpia)                         │
   │    PENDING → FLAGGED (fraude detectado)                            │
   │    \"\"\"                                                             │
   │    transactionStatusChanged(accountId: String!): Transaction!      │
   │  }                                                                 │
   └─────────────────────────────────────────────────────────────────────┘"

log "${WHITE}☕ JAVA (implementación DGS):${NC}"
log "${GRAY}   // FraudSubscriptionResolver.java - El resolver de subscriptions${NC}"
log "${GRAY}   ${NC}"
log "${GRAY}   @DgsSubscription  // ← Anotación especial de DGS para subscriptions${NC}"
log "${GRAY}   public Flux<FraudAlert> fraudAlertDetected(@InputArgument String accountId) {${NC}"
log "${GRAY}       // Retorna un Flux (stream) que emitirá alertas cuando las haya${NC}"
log "${GRAY}       return fraudAlertPublisher.getFraudAlertFlux(accountId);${NC}"
log "${GRAY}   }${NC}"
log ""
log "${GRAY}   // ═══════════════════════════════════════════════════════════════════${NC}"
log "${GRAY}   // FraudAlertPublisher.java - El \"publicador\" de eventos${NC}"
log "${GRAY}   // ═══════════════════════════════════════════════════════════════════${NC}"
log ""
log "${GRAY}   // Sink = \"lavabo\" donde se vierten las alertas${NC}"
log "${GRAY}   // multicast = múltiples suscriptores pueden escuchar${NC}"
log "${GRAY}   private final Sinks.Many<FraudAlert> fraudAlertSink = ${NC}"
log "${GRAY}       Sinks.many().multicast().onBackpressureBuffer();${NC}"
log ""
log "${GRAY}   // Cuando hay fraude, se llama este método${NC}"
log "${GRAY}   public void publishFraudAlert(FraudAlert alert) {${NC}"
log "${GRAY}       fraudAlertSink.tryEmitNext(alert);  // ← DISPARA a todos los suscriptores${NC}"
log "${GRAY}   }${NC}"
log ""
log "${GRAY}   // Los suscriptores obtienen un Flux filtrado por su cuenta${NC}"
log "${GRAY}   public Flux<FraudAlert> getFraudAlertFlux(String accountId) {${NC}"
log "${GRAY}       return fraudAlertSink.asFlux()${NC}"
log "${GRAY}           .filter(alert -> alert.getTransaction()${NC}"
log "${GRAY}                                 .getAccountId()${NC}"
log "${GRAY}                                 .equals(accountId));  // ← Solo TUS alertas${NC}"
log "${GRAY}   }${NC}"
log ""
pause

################################################################################
# SECCIÓN 6.5 - CÓMO PROBAR SUBSCRIPTIONS EN GRAPHIQL
################################################################################

print_section "SECCIÓN 6.5 — CÓMO PROBAR SUBSCRIPTIONS EN GRAPHIQL"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  ⚠️  IMPORTANTE: Las Subscriptions requieren WebSocket                     │${NC}"
log "${WHITE}│  No se pueden probar con curl (que usa HTTP normal).                       │${NC}"
log "${WHITE}│  Hay que usar GraphiQL o un cliente WebSocket.                             │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│  PASO 1: Abrir GraphiQL                                                    │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Abre en tu navegador: http://localhost:8080/                              │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│  PASO 2: Abrir DOS pestañas en GraphiQL                                    │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ┌─────────────────────────┐  ┌─────────────────────────┐                  │${NC}"
log "${WHITE}│  │  PESTAÑA 1              │  │  PESTAÑA 2              │                  │${NC}"
log "${WHITE}│  │  (Suscriptor)           │  │  (Generador de fraude)  │                  │${NC}"
log "${WHITE}│  │                         │  │                         │                  │${NC}"
log "${WHITE}│  │  Aquí escucharás        │  │  Aquí crearás           │                  │${NC}"
log "${WHITE}│  │  las alertas            │  │  transacciones          │                  │${NC}"
log "${WHITE}│  └─────────────────────────┘  └─────────────────────────┘                  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│  PASO 3: En PESTAÑA 1, ejecutar la Subscription                            │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Copia y pega esto:                                                        │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${CYAN}│  subscription {                                                            │${NC}"
log "${CYAN}│    fraudAlertDetected(accountId: \"account-001\") {                         │${NC}"
log "${CYAN}│      id                                                                    │${NC}"
log "${CYAN}│      riskLevel                                                             │${NC}"
log "${CYAN}│      reasons                                                               │${NC}"
log "${CYAN}│      recommendedAction                                                     │${NC}"
log "${CYAN}│      detectedAt                                                            │${NC}"
log "${CYAN}│      transaction {                                                         │${NC}"
log "${CYAN}│        id                                                                  │${NC}"
log "${CYAN}│        amount                                                              │${NC}"
log "${CYAN}│        merchantName                                                        │${NC}"
log "${CYAN}│        location                                                            │${NC}"
log "${CYAN}│      }                                                                     │${NC}"
log "${CYAN}│    }                                                                       │${NC}"
log "${CYAN}│  }                                                                         │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Presiona ▶️ PLAY                                                           │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Verás: \"Listening...\" - La subscription está activa, esperando eventos   │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│  PASO 4: En PESTAÑA 2, crear una transacción fraudulenta                   │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Copia y pega esto:                                                        │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${CYAN}│  mutation {                                                                │${NC}"
log "${CYAN}│    processTransaction(input: {                                             │${NC}"
log "${CYAN}│      accountId: \"account-001\"                                              │${NC}"
log "${CYAN}│      amount: 15000                                                         │${NC}"
log "${CYAN}│      currency: \"USD\"                                                       │${NC}"
log "${CYAN}│      merchantName: \"Suspicious Crypto Exchange\"                            │${NC}"
log "${CYAN}│      category: \"Cryptocurrency\"                                            │${NC}"
log "${CYAN}│      location: \"Moscow, Russia\"                                            │${NC}"
log "${CYAN}│    }) {                                                                    │${NC}"
log "${CYAN}│      success                                                               │${NC}"
log "${CYAN}│      message                                                               │${NC}"
log "${CYAN}│      fraudAlert { id riskLevel }                                           │${NC}"
log "${CYAN}│    }                                                                       │${NC}"
log "${CYAN}│  }                                                                         │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Presiona ▶️ PLAY                                                           │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│  PASO 5: ¡Observa la magia! 🔥                                             │${NC}"
log "${WHITE}│  ════════════════════════════════════════════════════════════════════════  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Vuelve a la PESTAÑA 1...                                                  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ¡La alerta apareció INSTANTÁNEAMENTE! Sin refrescar, sin polling.         │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Esto es el poder de las Subscriptions:                                    │${NC}"
log "${WHITE}│  • El servidor EMPUJÓ los datos al cliente                                 │${NC}"
log "${WHITE}│  • No tuviste que hacer ningún request adicional                           │${NC}"
log "${WHITE}│  • La latencia es prácticamente CERO                                       │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  PRUEBA MÁS:                                                               │${NC}"
log "${WHITE}│  Ejecuta más mutations fraudulentas en Pestaña 2.                          │${NC}"
log "${WHITE}│  Cada una aparecerá automáticamente en Pestaña 1.                          │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

################################################################################
# SECCIÓN 6.6 - HISTORIAL Y TESTS ADICIONALES
################################################################################

print_section "SECCIÓN 6.6 — HISTORIAL DE ALERTAS Y TESTS ADICIONALES"

# Test 5: Query - Historial de alertas
print_subsection "Test 5: Query - Historial de Alertas de Fraude"

print_schema "   ┌─────────────────────────────────────────────────────────────────────┐
   │  # fraud-schema.graphqls                                           │
   │                                                                     │
   │  type Query {                                                       │
   │    \"\"\"                                                             │
   │    Obtener historial de alertas de fraude de una cuenta            │
   │                                                                     │
   │    Retorna todas las alertas generadas previamente.                │
   │    Útil para:                                                       │
   │    - Auditoría de seguridad                                        │
   │    - Dashboard de monitoreo                                        │
   │    - Reportes de compliance                                        │
   │    \"\"\"                                                             │
   │    fraudAlerts(accountId: String!): [FraudAlert!]!                 │
   │  }                                                                 │
   │                                                                     │
   │  # ═══════════════════════════════════════════════════════════════ │
   │  # DIFERENCIA CLAVE:                                                │
   │  # ═══════════════════════════════════════════════════════════════ │
   │  #                                                                  │
   │  # Query fraudAlerts:                                               │
   │  #   → Dame TODAS las alertas PASADAS (histórico)                  │
   │  #   → Request-Response (una vez)                                  │
   │  #                                                                  │
   │  # Subscription fraudAlertDetected:                                 │
   │  #   → Avísame las NUEVAS alertas (futuro)                         │
   │  #   → Stream continuo (múltiples eventos)                         │
   │  # ═══════════════════════════════════════════════════════════════ │
   └─────────────────────────────────────────────────────────────────────┘"

log "${WHITE}   📝 ESCENARIO: Consultar el historial de alertas generadas${NC}"
log "${WHITE}   🚨 Esperamos ver las alertas de los Tests 3 y 4${NC}"
log ""

run_graphql_test "Historial de alertas de fraude" \
    '{ fraudAlerts(accountId: \"account-001\") { id riskLevel reasons recommendedAction transaction { id amount merchantName } } }' \
    'fraudAlerts'

# Test 6: Otra transacción fraudulenta
print_subsection "Test 6: Mutation - Fraude Cryptocurrency + Russia"

log "${WHITE}   📝 Este test genera otra alerta que los suscriptores recibirían${NC}"
log "${WHITE}   💰 Monto: \$12,000 USD (monto redondo → +10 pts)${NC}"
log "${WHITE}   🪙 Categoría: Cryptocurrency (alto riesgo → +25 pts)${NC}"
log "${WHITE}   📍 Ubicación: Moscow, Russia (país sospechoso → +40 pts)${NC}"
log "${WHITE}   📊 Risk Score esperado: 75+ → HIGH o CRITICAL${NC}"
log ""

run_graphql_test "Fraude - Crypto + Russia + Monto alto" \
    'mutation { processTransaction(input: {accountId: \"account-001\", amount: 12000, currency: \"USD\", merchantName: \"CryptoExchange\", category: \"Cryptocurrency\", location: \"Moscow, Russia\"}) { success message transaction { id status riskScore } fraudAlert { id riskLevel reasons } } }' \
    'riskLevel.*HIGH|riskLevel.*CRITICAL'

################################################################################
# RESUMEN FINAL
################################################################################

print_section "📊 RESUMEN DE TESTS - CHAPTER 06"

PASS_RATE=0
[ $TOTAL_TESTS -gt 0 ] && PASS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))

log "${CYAN}Tests Totales:    ${TOTAL_TESTS}${NC}"
log "${GREEN}Tests Exitosos:   ${PASSED_TESTS}${NC}"
log "${RED}Tests Fallidos:   ${FAILED_TESTS}${NC}"
log "${YELLOW}Tasa de Éxito:    ${PASS_RATE}%${NC}"
log ""

if [ $FAILED_TESTS -eq 0 ]; then
    log "${GREEN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    log "${GREEN}║                   🎉 ¡TODOS LOS TESTS PASARON! 🎉                           ║${NC}"
    log "${GREEN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
else
    log "${YELLOW}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    log "${YELLOW}║                   ⚠️  ALGUNOS TESTS FALLARON ⚠️                            ║${NC}"
    log "${YELLOW}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
fi

log ""
log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  📚 RESUMEN: CONCEPTOS CLAVE DE ESTE CAPÍTULO                              │${NC}"
log "${WHITE}├─────────────────────────────────────────────────────────────────────────────┤${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  1️⃣  SUBSCRIPTIONS = La tercera operación de GraphQL                       │${NC}"
log "${WHITE}│      • Query = leer (una vez)                                              │${NC}"
log "${WHITE}│      • Mutation = escribir (una vez)                                       │${NC}"
log "${WHITE}│      • Subscription = escuchar (streaming continuo)                        │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  2️⃣  WEBSOCKET = El protocolo de transporte                                │${NC}"
log "${WHITE}│      • HTTP = request/response (se cierra)                                 │${NC}"
log "${WHITE}│      • WebSocket = conexión persistente (se mantiene abierta)              │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  3️⃣  FLUX = Stream reactivo de datos                                       │${NC}"
log "${WHITE}│      • Puede emitir 0, 1, o N elementos                                    │${NC}"
log "${WHITE}│      • Los suscriptores reciben cada elemento cuando se emite              │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  4️⃣  SINKS.MANY = El \"publicador\" de eventos                              │${NC}"
log "${WHITE}│      • multicast() = múltiples suscriptores                                │${NC}"
log "${WHITE}│      • tryEmitNext() = publica un evento a todos                           │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}├─────────────────────────────────────────────────────────────────────────────┤${NC}"
log "${WHITE}│  📐 SCHEMA DE SUBSCRIPTIONS                                                │${NC}"
log "${WHITE}├─────────────────────────────────────────────────────────────────────────────┤${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  type Subscription {                                                       │${NC}"
log "${WHITE}│    fraudAlertDetected(accountId: String!): FraudAlert!                     │${NC}"
log "${WHITE}│    transactionStatusChanged(accountId: String!): Transaction!              │${NC}"
log "${WHITE}│  }                                                                         │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}├─────────────────────────────────────────────────────────────────────────────┤${NC}"
log "${WHITE}│  🚨 REGLAS DE DETECCIÓN DE FRAUDE                                          │${NC}"
log "${WHITE}├─────────────────────────────────────────────────────────────────────────────┤${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  +30 pts │ Monto > 3x promedio de la cuenta                                │${NC}"
log "${WHITE}│  +40 pts │ Ubicación sospechosa (Nigeria, Russia, China)                   │${NC}"
log "${WHITE}│  +25 pts │ Categoría de alto riesgo (Gambling, Crypto)                     │${NC}"
log "${WHITE}│  +20 pts │ Velocity check (>3 transacciones en 5 min)                      │${NC}"
log "${WHITE}│  +15 pts │ Hora inusual (3 AM - 5 AM)                                      │${NC}"
log "${WHITE}│  +10 pts │ Monto redondo sospechoso (múltiplo de \$1000)                   │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Score 0-24  → LOW      │ Score 50-79 → HIGH                               │${NC}"
log "${WHITE}│  Score 25-49 → MEDIUM   │ Score 80+   → CRITICAL 🚫                        │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
log "${CYAN}📄 Log completo guardado en: ${OUTPUT_FILE}${NC}"
log ""

exit $FAILED_TESTS