#!/usr/bin/env bash

################################################################################
# CHAPTER 03: IMPLEMENTACIÓN DE GRAPHQL CON DGS (NETFLIX JAVA)
# Script de Testing Automatizado - VERSIÓN EDUCATIVA
#
# Compatible con:
#   - macOS (Bash 3.2+)
#   - Linux (Bash 4.0+)
#   - Windows GitBash (Bash 4.4+)
#
# Uso: 
#   ./test-chapter03.sh           (modo interactivo)
#   ./test-chapter03.sh -s        (modo silencioso)
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
WHITE='\033[1;37m'
GRAY='\033[0;90m'
NC='\033[0m'

# Config
BASE_URL="http://localhost:8080"
GRAPHQL_ENDPOINT="${BASE_URL}/graphql"
OUTPUT_FILE="test-results-chapter03-$(date +%Y%m%d-%H%M%S).txt"

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

# Función para mostrar explicación educativa
print_concept() {
    log ""
    log "${WHITE}📚 CONCEPTO:${NC}"
    log "${GRAY}$1${NC}"
    log ""
}

print_java_code() {
    log "${WHITE}☕ CÓDIGO JAVA:${NC}"
    log "${YELLOW}$1${NC}"
    log ""
}

# Función para ejecutar tests con REQUEST visible y formateado
run_graphql_test() {
    local test_name="$1"
    local graphql_query="$2"
    local validation="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log "${YELLOW}🧪 Test #${TOTAL_TESTS}: ${test_name}${NC}"
    log ""
    
    # Mostrar el REQUEST
    log "${BLUE}📤 REQUEST:${NC}"
    log "${WHITE}   POST ${GRAPHQL_ENDPOINT}${NC}"
    log "${WHITE}   Content-Type: application/json${NC}"
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
    response=$(curl -s -X POST "${GRAPHQL_ENDPOINT}" \
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

# Función especial para tests sin body GraphQL
run_simple_test() {
    local test_name="$1"
    local curl_command="$2"
    local validation="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log "${YELLOW}🧪 Test #${TOTAL_TESTS}: ${test_name}${NC}"
    log ""
    log "${BLUE}📤 REQUEST:${NC}"
    log "${WHITE}   $curl_command${NC}"
    log ""
    log "${BLUE}⚡ Ejecutando...${NC}"
    
    response=$(eval "$curl_command" 2>&1)
    exit_code=$?
    
    log ""
    log "${BLUE}📥 RESPONSE:${NC}"
    if [ ${#response} -gt 200 ]; then
        log "${GREEN}   ${response:0:200}...${NC}"
    else
        log "${GREEN}   $response${NC}"
    fi
    
    log ""
    
    if [ $exit_code -eq 0 ] && echo "$response" | grep -qE "$validation"; then
        log "${GREEN}   ✅ PASSED${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        log "${RED}   ❌ FAILED${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    log ""
    pause
}

check_server() {
    log "${YELLOW}🔍 Verificando que el servidor esté corriendo...${NC}"
    
    if curl -s "${GRAPHQL_ENDPOINT}" > /dev/null 2>&1; then
        log "${GREEN}✅ Servidor corriendo en ${BASE_URL}${NC}"
        log ""
        return 0
    else
        log "${RED}❌ ERROR: Servidor NO está corriendo en ${BASE_URL}${NC}"
        log "${YELLOW}Por favor inicia el servidor con: mvn spring-boot:run${NC}"
        log ""
        exit 1
    fi
}

################################################################################
# HEADER
################################################################################

clear
log "${CYAN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
log "${CYAN}║                                                                              ║${NC}"
log "${CYAN}║         📘 CHAPTER 03: IMPLEMENTACIÓN DE GRAPHQL CON DGS                    ║${NC}"
log "${CYAN}║                     Testing Automatizado Completo                            ║${NC}"
log "${CYAN}║                                                                              ║${NC}"
log "${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
log ""
log "${YELLOW}Feature: Cashback Rewards System${NC}"
log "${YELLOW}Framework: Netflix DGS (Domain Graph Service)${NC}"
log "${YELLOW}Duración: 2.5 horas (5 secciones × 30 min)${NC}"
log "${YELLOW}Log: ${OUTPUT_FILE}${NC}"
log ""

check_server

pause

################################################################################
# SECCIÓN 3.1 - FRAMEWORK DGS Y ESTRUCTURA DE PROYECTO (30 min)
################################################################################

print_section "SECCIÓN 3.1 — INTRODUCCIÓN AL FRAMEWORK DGS Y ESTRUCTURA DE PROYECTO"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Entender qué es Netflix DGS y cómo se configura un proyecto GraphQL.      │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  DGS (Domain Graph Service) es el framework de Netflix para GraphQL en     │${NC}"
log "${WHITE}│  Java/Kotlin. Proporciona:                                                  │${NC}"
log "${WHITE}│    • Auto-descubrimiento de schemas en /resources/schema/                  │${NC}"
log "${WHITE}│    • Anotaciones simples: @DgsComponent, @DgsQuery, @DgsMutation           │${NC}"
log "${WHITE}│    • GraphiQL UI integrado para testing                                    │${NC}"
log "${WHITE}│    • DataLoader para optimización N+1                                      │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  📦 DEPENDENCIA MAVEN:                                                     │${NC}"
log "${WHITE}│  <dependency>                                                              │${NC}"
log "${WHITE}│      <groupId>com.netflix.graphql.dgs</groupId>                           │${NC}"
log "${WHITE}│      <artifactId>graphql-dgs-spring-boot-starter</artifactId>             │${NC}"
log "${WHITE}│  </dependency>                                                             │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 1: Endpoint GraphQL disponible
print_subsection "Test 1: Endpoint /graphql disponible"

print_concept "   DGS expone automáticamente el endpoint /graphql cuando agregas la dependencia.
   No necesitas configurar nada manualmente - es 'convention over configuration'.
   
   La query { __typename } es la forma más simple de verificar que GraphQL responde.
   Siempre retorna el nombre del tipo raíz: 'Query'."

run_graphql_test "Endpoint GraphQL responde" \
    '{ __typename }' \
    '"__typename".*"Query"'

# Test 2: GraphiQL UI disponible
print_subsection "Test 2: GraphiQL UI disponible en /graphiql"

print_concept "   GraphiQL es un IDE interactivo para GraphQL que viene incluido con DGS.
   Permite:
     • Escribir queries con autocompletado
     • Ver la documentación del schema
     • Ejecutar queries y ver resultados
     • Explorar el schema con el 'Docs' panel
   
   En producción, puedes deshabilitarlo con:
   dgs.graphql.graphiql.enabled=false"

run_simple_test "GraphiQL UI accesible" \
    "curl -s ${BASE_URL}/graphiql" \
    'graphiql'

# Test 3: Introspection habilitada
print_subsection "Test 3: Introspection habilitada"

print_concept "   Introspection permite a los clientes preguntar al servidor qué tipos,
   queries y mutations están disponibles. Es lo que hace funcionar el autocompletado.
   
   __schema y __type son queries especiales de introspection.
   
   ⚠️  En producción, considera deshabilitar introspection por seguridad:
   dgs.graphql.introspection.enabled=false"

run_graphql_test "Introspection query funciona" \
    '{ __schema { types { name } } }' \
    '"__schema".*"types"'

# Test 4: Schema cargado automáticamente
print_subsection "Test 4: Schema GraphQL cargado desde resources/schema/"

print_concept "   DGS busca archivos *.graphqls en src/main/resources/schema/
   y los combina automáticamente en un solo schema.
   
   📁 ESTRUCTURA TÍPICA:
   src/main/resources/schema/
   ├── schema.graphqls      (tipos principales)
   ├── user.graphqls        (tipos de usuario)
   └── rewards.graphqls     (tipos de rewards)
   
   Todos se combinan en un único schema ejecutable."

run_graphql_test "Tipos del schema disponibles" \
    '{ __type(name: \"User\") { name fields { name } } }' \
    '"name".*"User".*"fields"'

# Test 5: Enums registrados
print_subsection "Test 5: Enums del schema (RewardTier, RewardStatus, etc.)"

print_concept "   Los Enums en GraphQL definen un conjunto cerrado de valores válidos.
   DGS los mapea automáticamente a Java enums.
   
   SCHEMA:                         JAVA:
   enum RewardTier {               public enum RewardTier {
     BRONZE                            BRONZE,
     SILVER                            SILVER,
     GOLD                              GOLD,
     PLATINUM                          PLATINUM
   }                               }"

run_graphql_test "Enum RewardTier disponible" \
    '{ __type(name: \"RewardTier\") { enumValues { name } } }' \
    '"enumValues".*"BRONZE".*"SILVER"'

# Test 6: DGS funcionando
print_subsection "Test 6: Verificar que DGS esté funcionando correctamente"

print_concept "   Esta query simple verifica que toda la cadena funciona:
   
   1. Cliente envía query → 
   2. DGS parsea el GraphQL →
   3. DGS encuentra el resolver →
   4. Resolver ejecuta lógica →
   5. DGS serializa respuesta →
   6. Cliente recibe JSON"

run_graphql_test "DGS procesa queries correctamente" \
    '{ user(id: \"user-001\") { id } }' \
    '"data".*"user"'

################################################################################
# SECCIÓN 3.2 - DEFINICIÓN DEL SCHEMA Y GENERACIÓN AUTOMÁTICA DE CLASES (30 min)
################################################################################

print_section "SECCIÓN 3.2 — DEFINICIÓN DEL SCHEMA Y MAPEO A CLASES JAVA"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Entender cómo el schema GraphQL se mapea a clases Java.                   │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  El schema es el CONTRATO de tu API. Define:                               │${NC}"
log "${WHITE}│    • Types → Clases Java (POJOs/Records)                                   │${NC}"
log "${WHITE}│    • Enums → Java Enums                                                    │${NC}"
log "${WHITE}│    • Input → Clases Java para parámetros de mutations                      │${NC}"
log "${WHITE}│    • Query → Métodos con @DgsQuery                                         │${NC}"
log "${WHITE}│    • Mutation → Métodos con @DgsMutation                                   │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  MAPEO SCHEMA → JAVA:                                                      │${NC}"
log "${WHITE}│  ┌──────────────────────┬────────────────────────────────┐                │${NC}"
log "${WHITE}│  │ GraphQL              │ Java                           │                │${NC}"
log "${WHITE}│  ├──────────────────────┼────────────────────────────────┤                │${NC}"
log "${WHITE}│  │ type User            │ class User                     │                │${NC}"
log "${WHITE}│  │ ID!                  │ String                         │                │${NC}"
log "${WHITE}│  │ String               │ String                         │                │${NC}"
log "${WHITE}│  │ Float                │ Double / BigDecimal            │                │${NC}"
log "${WHITE}│  │ Int                  │ Integer                        │                │${NC}"
log "${WHITE}│  │ Boolean              │ Boolean                        │                │${NC}"
log "${WHITE}│  │ [Type]               │ List<Type>                     │                │${NC}"
log "${WHITE}│  │ Type!                │ @NonNull Type                  │                │${NC}"
log "${WHITE}│  └──────────────────────┴────────────────────────────────┘                │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 7: Schema define tipo User completo
print_subsection "Test 7: Tipo User definido en schema con todos sus campos"

print_concept "   El tipo User en el schema define la estructura de datos que el cliente
   puede solicitar. Cada campo tiene un tipo y una nullabilidad.
   
   SCHEMA:                           JAVA:
   type User {                       public class User {
     id: ID!                             private String id;
     email: String!                      private String email;
     fullName: String!                   private String fullName;
     tier: RewardTier!                   private RewardTier tier;
     rewards: [Reward!]!                 private List<Reward> rewards;
   }                                 }"

run_graphql_test "Schema tiene tipo User con estructura completa" \
    '{ __type(name: \"User\") { name fields { name type { name } } } }' \
    '"name".*"User".*"id".*"email".*"fullName".*"tier"'

# Test 8: Schema define tipo Reward completo
print_subsection "Test 8: Tipo Reward definido en schema con todos sus campos"

print_concept "   Reward es otra entidad del dominio. Nota cómo tiene:
   - Campos escalares (amount, description)
   - Campos enum (status, category)
   - Relación con User (campo 'user')
   
   Las relaciones se resuelven con @DgsData (veremos en sección 3.3)."

run_graphql_test "Schema tiene tipo Reward con estructura completa" \
    '{ __type(name: \"Reward\") { name fields { name type { name } } } }' \
    '"name".*"Reward".*"amount".*"status".*"category"'

# Test 9: Queries básicas definidas
print_subsection "Test 9: Queries básicas definidas en el schema"

print_concept "   El tipo Query es especial - define los 'entry points' de lectura.
   Es como definir los endpoints GET de un REST API.
   
   SCHEMA:                           JAVA RESOLVER:
   type Query {                      @DgsComponent
     user(id: ID!): User             public class QueryResolver {
     users: [User!]!                     @DgsQuery
     usersByTier(tier: RewardTier!)      public User user(@InputArgument String id)
   }                                 }"

run_graphql_test "Query type tiene operaciones básicas" \
    '{ __type(name: \"Query\") { fields { name } } }' \
    '"fields".*"user".*"usersByTier"'

# Test 10: Mutations básicas definidas
print_subsection "Test 10: Mutations básicas definidas en el schema"

print_concept "   El tipo Mutation define los 'entry points' de escritura.
   Es como definir los endpoints POST/PUT/DELETE de un REST API.
   
   SCHEMA:                                    JAVA RESOLVER:
   type Mutation {                            @DgsComponent
     createReward(input: CreateRewardInput!)  public class MutationResolver {
     redeemCashback(input: RedeemInput!)          @DgsMutation
   }                                              public Reward createReward(...)"

run_graphql_test "Mutation type tiene operaciones básicas" \
    '{ __type(name: \"Mutation\") { fields { name } } }' \
    '"fields".*"createReward".*"redeemCashback"'

# Test 11: Custom Scalars definidos (DateTime)
print_subsection "Test 11: Custom Scalar DateTime definido en schema"

print_concept "   GraphQL tiene pocos tipos escalares básicos (String, Int, Float, Boolean, ID).
   Para tipos como DateTime o Money, creamos Custom Scalars.
   
   SCHEMA:                           JAVA:
   scalar DateTime                   @DgsScalar(name = \"DateTime\")
                                     public class DateTimeScalar implements 
   type Reward {                         Coercing<LocalDateTime, String> {
     createdAt: DateTime!                // serialize() y parseValue()
   }                                 }"

run_graphql_test "DateTime scalar registrado" \
    '{ __type(name: \"DateTime\") { name kind } }' \
    '"name".*"DateTime"'

# Test 12: Input types generados funcionan
print_subsection "Test 12: Input types (CreateRewardInput) generados por codegen"

print_concept "   Los Input types son diferentes de los Output types.
   Solo pueden contener escalares, enums y otros inputs (no types).
   
   SCHEMA:                                JAVA:
   input CreateRewardInput {              public class CreateRewardInput {
     userId: ID!                              private String userId;
     transactionAmount: Float!                private Double transactionAmount;
     category: TransactionCategory!           private TransactionCategory category;
   }                                      }"

run_graphql_test "Input type CreateRewardInput funciona correctamente" \
    'mutation { createReward(input: { userId: \"user-001\", transactionId: \"txn-test-001\", transactionAmount: 500.0, category: TRAVEL, description: \"Test\" }) { id amount } }' \
    '"id".*"amount"'

################################################################################
# SECCIÓN 3.3 - IMPLEMENTACIÓN DE RESOLVERS CON @DgsData (30 min)
################################################################################

print_section "SECCIÓN 3.3 — IMPLEMENTACIÓN DE RESOLVERS CON @DgsData"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Aprender a implementar resolvers usando las anotaciones DGS.              │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ANOTACIONES PRINCIPALES:                                                  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  @DgsComponent    → Marca la clase como componente DGS                     │${NC}"
log "${WHITE}│  @DgsQuery        → Resuelve un campo del tipo Query                       │${NC}"
log "${WHITE}│  @DgsMutation     → Resuelve un campo del tipo Mutation                    │${NC}"
log "${WHITE}│  @DgsData         → Resuelve un campo anidado de cualquier tipo            │${NC}"
log "${WHITE}│  @InputArgument   → Inyecta un argumento de la query/mutation              │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  FLUJO DE RESOLUCIÓN:                                                      │${NC}"
log "${WHITE}│  ┌────────────────────────────────────────────────────────────────────┐   │${NC}"
log "${WHITE}│  │  { user(id:\"1\") { fullName rewards { amount } } }                │   │${NC}"
log "${WHITE}│  │         │              │           │                               │   │${NC}"
log "${WHITE}│  │         ▼              │           ▼                               │   │${NC}"
log "${WHITE}│  │    @DgsQuery           │      @DgsData                             │   │${NC}"
log "${WHITE}│  │    user(id)  ──────────┼────▶ rewards(user)                        │   │${NC}"
log "${WHITE}│  │                        │                                           │   │${NC}"
log "${WHITE}│  │         ◄──────────────┘                                           │   │${NC}"
log "${WHITE}│  │    Resolver de User retorna User, DGS llama al resolver de rewards │   │${NC}"
log "${WHITE}│  └────────────────────────────────────────────────────────────────────┘   │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 13: @DgsQuery simple
print_subsection "Test 13: Resolver con @DgsQuery (query simple)"

print_concept "   @DgsQuery marca un método que resuelve un campo del tipo Query.
   El nombre del método debe coincidir con el nombre del campo en el schema.
   
   SCHEMA:                       JAVA:
   type Query {                  @DgsComponent
     user(id: ID!): User         public class UserResolver {
   }                                 
                                     @DgsQuery
                                     public User user(@InputArgument String id) {
                                         return userService.findById(id);
                                     }
                                 }"

run_graphql_test "Query user con @DgsQuery" \
    '{ user(id: \"user-002\") { fullName tier } }' \
    '"fullName".*"tier"'

# Test 14: @DgsQuery con argumentos/filtros
print_subsection "Test 14: Resolver con @DgsQuery y @InputArgument"

print_concept "   @InputArgument inyecta argumentos de la query en parámetros del método.
   El nombre del parámetro debe coincidir con el nombre del argumento GraphQL.
   
   SCHEMA:                                   JAVA:
   type Query {                              @DgsQuery
     usersByTier(tier: RewardTier!):         public List<User> usersByTier(
       [User!]!                                  @InputArgument RewardTier tier) {
   }                                             return userService.findByTier(tier);
                                             }"

run_graphql_test "Query usersByTier con argumentos" \
    '{ usersByTier(tier: GOLD) { fullName tier } }' \
    '"tier".*"GOLD"'

# Test 15: @DgsQuery con Input type complejo
print_subsection "Test 15: Query con Input type complejo (RewardsFilterInput)"

print_concept "   Los Input types permiten agrupar múltiples parámetros en un objeto.
   DGS los deserializa automáticamente a la clase Java correspondiente.
   
   SCHEMA:                              JAVA:
   input RewardsFilterInput {           @DgsQuery
     status: RewardStatus               public List<Reward> rewards(
     category: TransactionCategory          @InputArgument RewardsFilterInput filter
   }                                    ) {
                                            return rewardService.findByFilter(filter);
   rewards(filter: RewardsFilterInput)  }"

run_graphql_test "Filtro complejo con RewardsFilterInput" \
    '{ rewards(filter: { status: ACTIVE, category: TRAVEL }) { amount category status } }' \
    '"status".*"ACTIVE".*"category".*"TRAVEL"'

# Test 16: @DgsData para campo anidado (Reward -> User)
print_subsection "Test 16: Resolver con @DgsData para navegación anidada"

print_concept "   @DgsData resuelve campos anidados que no están en el objeto padre.
   Cuando el cliente pide reward.user, DGS llama al resolver de user.
   
   SCHEMA:                          JAVA:
   type Reward {                    @DgsData(parentType = \"Reward\", field = \"user\")
     user: User                     public User user(DgsDataFetchingEnvironment dfe) {
   }                                    Reward reward = dfe.getSource();
                                        return userService.findById(reward.getUserId());
   El campo 'user' NO está en la     }
   clase Reward de Java - se
   resuelve dinámicamente."

run_graphql_test "Navegación Reward -> User con @DgsData" \
    '{ reward(id: \"reward-100\") { amount user { fullName tier } } }' \
    '"user".*"fullName".*"tier"'

# Test 17: @DgsData para campo anidado (User -> Rewards)
print_subsection "Test 17: Navegación inversa User -> Rewards con @DgsData"

print_concept "   La navegación inversa funciona igual: User → Rewards.
   Esto permite queries bidireccionales desde cualquier punto.
   
   JAVA:
   @DgsData(parentType = \"User\", field = \"rewards\")
   public List<Reward> rewards(DgsDataFetchingEnvironment dfe) {
       User user = dfe.getSource();  // El User padre
       return rewardService.findByUserId(user.getId());
   }
   
   ⚠️  SIN DataLoader, esto causa el problema N+1 (veremos en sección 3.5)"

run_graphql_test "Navegación User -> Rewards con @DgsData" \
    '{ user(id: \"user-003\") { fullName rewards { amount category } } }' \
    '"rewards".*"amount".*"category"'

# Test 18: Query agregada con cálculos
print_subsection "Test 18: Query con cálculos agregados (rewardsSummary)"

print_concept "   Los resolvers pueden hacer cálculos complejos, no solo buscar datos.
   Esto es ideal para dashboards, reportes, o resúmenes.
   
   JAVA:
   @DgsQuery
   public RewardsSummary rewardsSummary(@InputArgument String userId) {
       List<Reward> rewards = rewardService.findByUserId(userId);
       
       return RewardsSummary.builder()
           .totalEarned(rewards.stream().mapToDouble(Reward::getAmount).sum())
           .availableBalance(calculateAvailable(rewards))
           .rewardsByCategory(groupByCategory(rewards))
           .build();
   }"

run_graphql_test "Summary con totales calculados" \
    '{ rewardsSummary(userId: \"user-001\") { totalEarned availableBalance rewardsByCategory { category totalAmount } } }' \
    '"totalEarned".*"availableBalance".*"rewardsByCategory"'

################################################################################
# SECCIÓN 3.4 - MUTATIONS Y LÓGICA DE NEGOCIO (30 min)
################################################################################

print_section "SECCIÓN 3.4 — MUTATIONS Y LÓGICA DE NEGOCIO INTEGRADA"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Implementar mutations con validaciones y lógica de negocio.               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  DIFERENCIA QUERY vs MUTATION:                                             │${NC}"
log "${WHITE}│  • Query  → Solo lectura (GET), se pueden ejecutar en paralelo             │${NC}"
log "${WHITE}│  • Mutation → Escritura (POST/PUT/DELETE), se ejecutan en secuencia        │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  PATRÓN DE RESPUESTA ESTRUCTURADA:                                         │${NC}"
log "${WHITE}│  En lugar de lanzar excepciones, retornamos objetos con:                   │${NC}"
log "${WHITE}│    { success: Boolean, message: String, data: T }                          │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ARQUITECTURA RECOMENDADA:                                                 │${NC}"
log "${WHITE}│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐               │${NC}"
log "${WHITE}│  │   Resolver   │ ──▶ │   Service    │ ──▶ │  Repository  │               │${NC}"
log "${WHITE}│  │  (entrada)   │     │  (lógica)    │     │   (datos)    │               │${NC}"
log "${WHITE}│  └──────────────┘     └──────────────┘     └──────────────┘               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  El Resolver solo orquesta. La lógica de negocio va en el Service.        │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 19: Mutation createReward
print_subsection "Test 19: Mutation createReward con lógica de negocio"

print_concept "   Esta mutation crea un Reward calculando el cashback según:
   - El tier del usuario (BRONZE=1%, SILVER=2%, GOLD=3%, PLATINUM=5%)
   - La categoría (TRAVEL=3x, RESTAURANTS=2x, SHOPPING=1.5x)
   
   JAVA SERVICE:
   public Reward createReward(CreateRewardInput input) {
       User user = userService.findById(input.getUserId());
       double basePercent = getTierPercentage(user.getTier());  // 1-5%
       double multiplier = getCategoryMultiplier(input.getCategory()); // 1-3x
       double cashback = input.getTransactionAmount() * basePercent * multiplier / 100;
       
       return Reward.builder()
           .amount(cashback)
           .multiplier(multiplier)
           .category(input.getCategory())
           .build();
   }"

run_graphql_test "Crear reward calculando cashback" \
    'mutation { createReward(input: { userId: \"user-001\", transactionId: \"txn-test-001\", transactionAmount: 1000.0, category: TRAVEL, description: \"Vuelo a Miami\" }) { id amount category multiplier } }' \
    '"amount".*"category".*"TRAVEL"'

# Test 20: Mutation redeemCashback
print_subsection "Test 20: Mutation redeemCashback con validaciones"

print_concept "   Las mutations deben validar antes de ejecutar:
   1. ¿Existe el usuario?
   2. ¿Tiene saldo suficiente?
   3. ¿La cuenta destino es válida?
   
   JAVA SERVICE:
   public RedeemResult redeemCashback(RedeemInput input) {
       User user = userService.findById(input.getUserId());
       
       if (user.getAvailableCashback() < input.getAmount()) {
           return RedeemResult.failure(\"Insufficient balance\");
       }
       
       // Procesar redención...
       return RedeemResult.success(input.getAmount());
   }"

run_graphql_test "Redimir cashback" \
    'mutation { redeemCashback(input: { userId: \"user-002\", amount: 50.0, destinationAccount: \"ACC-123456\" }) { success message redeemedAmount } }' \
    '"success".*true.*"message"'

# Test 21: Mutation con validación de balance insuficiente
print_subsection "Test 21: Validación de balance insuficiente"

print_concept "   Cuando una validación falla, NO lanzamos excepción.
   Retornamos { success: false, message: 'Error...' }
   
   Esto permite al cliente manejar errores de negocio de forma predecible,
   diferenciándolos de errores técnicos (que sí lanzan excepciones).
   
   JAVA:
   if (balance < amount) {
       return RedeemResult.builder()
           .success(false)
           .message(\"Insufficient balance. Available: \" + balance)
           .build();
   }"

run_graphql_test "Redención rechazada por balance insuficiente" \
    'mutation { redeemCashback(input: { userId: \"user-005\", amount: 99999.0, destinationAccount: \"ACC-999\" }) { success message } }' \
    '"success"[[:space:]]*:[[:space:]]*false.*[Ii]nsufficient'

# Test 22: Mutation updateRewardStatus
print_subsection "Test 22: Mutation updateRewardStatus (admin)"

print_concept "   Algunas mutations son administrativas (cancelar, suspender, etc.).
   Deben registrar quién hizo el cambio y por qué (auditoría).
   
   JAVA:
   @DgsMutation
   public Reward updateRewardStatus(@InputArgument UpdateStatusInput input) {
       Reward reward = rewardService.findById(input.getRewardId());
       
       reward.setStatus(input.getNewStatus());
       reward.setStatusReason(input.getReason());
       reward.setUpdatedAt(LocalDateTime.now());
       
       return rewardService.save(reward);
   }"

run_graphql_test "Actualizar estado de reward" \
    'mutation { updateRewardStatus(input: { rewardId: \"reward-101\", newStatus: CANCELLED, reason: \"Test de cancelación\" }) { id status } }' \
    '"status".*"CANCELLED"'

# Test 23: Mutation upgradeUserTier
print_subsection "Test 23: Mutation upgradeUserTier"

print_concept "   Esta mutation cambia el tier de un usuario.
   En producción, esto afectaría el porcentaje de cashback futuro.
   
   REGLAS DE NEGOCIO:
   • BRONZE → SILVER: Gastar \$1,000+
   • SILVER → GOLD: Gastar \$5,000+
   • GOLD → PLATINUM: Gastar \$20,000+
   
   JAVA:
   @DgsMutation
   public User upgradeUserTier(
       @InputArgument String userId,
       @InputArgument RewardTier newTier) {
       return userService.upgradeTier(userId, newTier);
   }"

run_graphql_test "Upgrade de tier de usuario" \
    'mutation { upgradeUserTier(userId: \"user-005\", newTier: SILVER) { fullName tier } }' \
    '"tier".*"SILVER"'

# Test 24: Mutation batch (expireOldRewards)
print_subsection "Test 24: Mutation batch expireOldRewards"

print_concept "   Las mutations pueden ser operaciones batch (afectan múltiples registros).
   Útil para tareas administrativas como expirar rewards viejas.
   
   JAVA:
   @DgsMutation
   public int expireOldRewards() {
       LocalDateTime cutoff = LocalDateTime.now().minusDays(365);
       
       List<Reward> expired = rewardRepository
           .findByStatusAndCreatedAtBefore(ACTIVE, cutoff);
       
       expired.forEach(r -> r.setStatus(EXPIRED));
       rewardRepository.saveAll(expired);
       
       return expired.size();  // Cuántas se expiraron
   }"

run_graphql_test "Expirar rewards vencidas (batch)" \
    'mutation { expireOldRewards }' \
    '"data"'

################################################################################
# SECCIÓN 3.5 - DATALOADER Y PROBLEMA N+1 (30 min)
################################################################################

print_section "SECCIÓN 3.5 — OPTIMIZACIÓN CON DATALOADER Y PROBLEMA N+1"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Entender y resolver el problema N+1 con DataLoader.                       │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ❌ PROBLEMA N+1 (SIN DataLoader):                                         │${NC}"
log "${WHITE}│  Query: { users { rewards { amount } } }                                   │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  1 query para obtener 10 usuarios                                          │${NC}"
log "${WHITE}│  + 10 queries para obtener rewards de cada usuario                         │${NC}"
log "${WHITE}│  = 11 queries totales 😱                                                   │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ✅ SOLUCIÓN CON DataLoader:                                               │${NC}"
log "${WHITE}│  1 query para obtener 10 usuarios                                          │${NC}"
log "${WHITE}│  + 1 query batch: SELECT * FROM rewards WHERE user_id IN (1,2,3...10)     │${NC}"
log "${WHITE}│  = 2 queries totales 🎉                                                    │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  CÓMO FUNCIONA:                                                            │${NC}"
log "${WHITE}│  1. DataLoader acumula todas las solicitudes del mismo 'tick'              │${NC}"
log "${WHITE}│  2. Al final del tick, ejecuta UNA query batch con todos los IDs           │${NC}"
log "${WHITE}│  3. Distribuye los resultados a cada solicitud original                    │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 25: Query que activaría N+1 sin DataLoader
print_subsection "Test 25: Query múltiple User -> Rewards (batch loading)"

print_concept "   Esta query obtiene múltiples usuarios con sus rewards.
   SIN DataLoader: 1 + N queries (N = número de usuarios)
   CON DataLoader: 2 queries (usuarios + rewards en batch)
   
   JAVA DATALOADER:
   @DgsDataLoader(name = \"rewards\")
   public class RewardsDataLoader implements BatchLoader<String, List<Reward>> {
       
       @Override
       public CompletionStage<List<List<Reward>>> load(List<String> userIds) {
           // UNA sola query: WHERE user_id IN (userIds)
           Map<String, List<Reward>> rewardsByUser = 
               rewardService.findByUserIds(userIds);
           
           return CompletableFuture.completedFuture(
               userIds.stream()
                   .map(id -> rewardsByUser.getOrDefault(id, List.of()))
                   .toList()
           );
       }
   }"

run_graphql_test "Multiples usuarios con sus rewards (DataLoader activo)" \
    '{ usersByTier(tier: BRONZE) { fullName rewards { amount category } } }' \
    '"fullName".*"rewards".*"amount"'

# Test 26: Query inversa que activaría N+1
print_subsection "Test 26: Query múltiple Reward -> User (batch loading)"

print_concept "   DataLoader también funciona en dirección inversa (Reward → User).
   Si tenemos 50 rewards y queremos el user de cada una:
   
   SIN DataLoader: 50 queries SELECT * FROM users WHERE id = ?
   CON DataLoader: 1 query SELECT * FROM users WHERE id IN (...)
   
   JAVA:
   @DgsDataLoader(name = \"users\")
   public class UsersDataLoader implements BatchLoader<String, User> {
       @Override
       public CompletionStage<List<User>> load(List<String> userIds) {
           return CompletableFuture.completedFuture(
               userService.findByIds(userIds)
           );
       }
   }"

run_graphql_test "Múltiples rewards con su user (DataLoader activo)" \
    '{ rewards(filter: { status: ACTIVE }) { amount user { fullName tier } } }' \
    '"amount".*"user".*"fullName"'

# Test 27: Query profunda anidada
print_subsection "Test 27: Query profundamente anidada User -> Rewards -> User"

print_concept "   GraphQL permite navegación infinita: User → Rewards → User → Rewards...
   Sin DataLoader esto sería exponencial: O(N^depth)
   Con DataLoader sigue siendo O(depth) queries.
   
   ⚠️  CUIDADO: Queries muy profundas pueden ser ataques DoS.
   Considera limitar la profundidad máxima:
   
   @Bean
   public Instrumentation maxDepthInstrumentation() {
       return new MaxQueryDepthInstrumentation(10);  // Máximo 10 niveles
   }"

run_graphql_test "Navegación User -> Rewards -> User (3 niveles)" \
    '{ user(id: \"user-001\") { fullName rewards { amount user { tier } } } }' \
    '"fullName".*"rewards".*"tier"'

# Test 28: Query con filtros + DataLoader
print_subsection "Test 28: Query filtrada con DataLoader"

print_concept "   DataLoader funciona incluso con filtros adicionales.
   El filtro se aplica DESPUÉS de cargar en batch.
   
   JAVA (en el resolver):
   @DgsData(parentType = \"User\", field = \"rewards\")
   public CompletableFuture<List<Reward>> rewards(
       DgsDataFetchingEnvironment dfe,
       @InputArgument RewardStatus status) {
       
       DataLoader<String, List<Reward>> loader = 
           dfe.getDataLoader(\"rewards\");
       
       User user = dfe.getSource();
       return loader.load(user.getId())
           .thenApply(rewards -> filterByStatus(rewards, status));
   }"

run_graphql_test "userRewards con filtro de status (DataLoader)" \
    '{ userRewards(userId: \"user-003\", status: ACTIVE) { amount user { fullName } } }' \
    '"amount".*"user"'

# Test 29: Múltiples usuarios PLATINUM con rewards
print_subsection "Test 29: Tier PLATINUM con todas sus rewards (batch eficiente)"

print_concept "   Esta query combina todo lo aprendido:
   1. Filtro por tier (usersByTier)
   2. Campo calculado (availableCashback)
   3. Relación anidada (rewards) con DataLoader
   
   El performance es óptimo porque DataLoader agrupa todas
   las solicitudes de rewards en una sola query."

run_graphql_test "PLATINUM users con rewards (DataLoader batch)" \
    '{ usersByTier(tier: PLATINUM) { fullName availableCashback rewards { amount category status } } }' \
    '"fullName".*"availableCashback".*"rewards"'

# Test 30: Query con CashbackRules y cálculos
print_subsection "Test 30: Query de reglas + cálculo de cashback"

print_concept "   Esta query demuestra cómo GraphQL puede combinar múltiples operaciones:
   1. cashbackRule → Obtener las reglas configuradas
   2. calculateCashback → Calcular el cashback para un monto específico
   
   Ambas queries se ejecutan en paralelo (optimización de GraphQL).
   
   JAVA:
   @DgsQuery
   public CashbackRule cashbackRule(@InputArgument TransactionCategory category) {
       return ruleService.getRule(category);
   }
   
   @DgsQuery
   public Double calculateCashback(
       @InputArgument String userId,
       @InputArgument Double transactionAmount,
       @InputArgument TransactionCategory category) {
       return cashbackService.calculate(userId, transactionAmount, category);
   }"

run_graphql_test "CashbackRules y calculateCashback" \
    '{ cashbackRule(category: TRAVEL) { basePercentage tierMultipliers { platinum } } calculateCashback(userId: \"user-004\", transactionAmount: 5000.0, category: TRAVEL) }' \
    '"basePercentage".*"calculateCashback"'

################################################################################
# RESUMEN FINAL
################################################################################

print_section "📊 RESUMEN DE TESTS - CHAPTER 03"

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
    log "${GREEN}║            El CHAPTER 03 está funcionando perfectamente.                     ║${NC}"
    log "${GREEN}║                                                                              ║${NC}"
    log "${GREEN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
else
    log "${YELLOW}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    log "${YELLOW}║                                                                              ║${NC}"
    log "${YELLOW}║                   ⚠️  ALGUNOS TESTS FALLARON ⚠️                            ║${NC}"
    log "${YELLOW}║                                                                              ║${NC}"
    log "${YELLOW}║           Revisa el log para más detalles                                    ║${NC}"
    log "${YELLOW}║                                                                              ║${NC}"
    log "${YELLOW}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
fi

log ""
log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  📚 RESUMEN DE CONCEPTOS APRENDIDOS                                        │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ✅ Sección 3.1: Configuración de DGS y estructura de proyecto             │${NC}"
log "${WHITE}│     • Dependencia Maven graphql-dgs-spring-boot-starter                    │${NC}"
log "${WHITE}│     • Auto-descubrimiento de schemas en /resources/schema/                 │${NC}"
log "${WHITE}│     • GraphiQL UI para testing interactivo                                 │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ✅ Sección 3.2: Schema GraphQL y mapeo a Java                             │${NC}"
log "${WHITE}│     • Types → Clases Java, Enums → Java Enums                              │${NC}"
log "${WHITE}│     • Input types para parámetros de mutations                             │${NC}"
log "${WHITE}│     • Custom Scalars (DateTime, Money)                                     │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ✅ Sección 3.3: Resolvers con anotaciones DGS                             │${NC}"
log "${WHITE}│     • @DgsQuery, @DgsMutation para entry points                            │${NC}"
log "${WHITE}│     • @DgsData para campos anidados                                        │${NC}"
log "${WHITE}│     • @InputArgument para inyectar parámetros                              │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ✅ Sección 3.4: Mutations y lógica de negocio                             │${NC}"
log "${WHITE}│     • Validaciones antes de ejecutar                                       │${NC}"
log "${WHITE}│     • Patrón { success, message, data }                                    │${NC}"
log "${WHITE}│     • Separación Resolver → Service → Repository                           │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ✅ Sección 3.5: DataLoader y problema N+1                                 │${NC}"
log "${WHITE}│     • BatchLoader para cargar en lotes                                     │${NC}"
log "${WHITE}│     • De N+1 queries a 2 queries                                           │${NC}"
log "${WHITE}│     • Optimización automática de GraphQL                                   │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
log "${CYAN}📄 Log completo guardado en: ${OUTPUT_FILE}${NC}"
log ""

exit $FAILED_TESTS