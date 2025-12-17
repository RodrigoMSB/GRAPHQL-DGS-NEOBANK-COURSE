package com.neobank.savings.resolver;

import com.netflix.graphql.dgs.*;
import com.neobank.savings.model.SavingsGoalEntity;
import com.neobank.savings.service.SavingsGoalService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resolver GraphQL para Metas de Ahorro.
 * 
 * 🎓 SECCIÓN 4.1 + 4.4: INTEGRACIÓN JPA CON GRAPHQL
 * 
 * Este resolver demuestra:
 * - Conexión entre DGS y JPA
 * - Transformación de Entity a respuesta GraphQL
 * - Campos calculados (progressPercentage)
 * - Manejo de errores en mutations
 * 
 * 💡 ¿POR QUÉ TRANSFORMAR ENTITY A MAP?
 * 
 * Hay dos enfoques para retornar datos en GraphQL:
 * 
 * OPCIÓN 1: Retornar Entity directamente
 * ```java
 * @DgsQuery
 * public SavingsGoalEntity savingsGoal(String id) {
 *     return service.getGoalById(Long.parseLong(id));
 * }
 * ```
 * Problema: Los nombres de campos deben coincidir EXACTAMENTE con el schema.
 * En Entity tenemos goalId, pero en GraphQL es id.
 * 
 * OPCIÓN 2: Transformar a Map (lo que hacemos aquí)
 * ```java
 * @DgsQuery
 * public Map<String, Object> savingsGoal(String id) {
 *     SavingsGoalEntity entity = service.getGoalById(Long.parseLong(id));
 *     return toGraphQL(entity);  // Transforma campos
 * }
 * ```
 * Ventaja: Control total sobre el mapeo de campos.
 * 
 * OPCIÓN 3: Usar DTOs (recomendado para proyectos grandes)
 * Crear una clase SavingsGoalDTO con los campos del schema GraphQL.
 * 
 * @see SavingsGoalService (lógica de negocio)
 * @see savings-schema.graphqls (schema GraphQL)
 */
@DgsComponent
public class SavingsGoalResolver {
    
    private final SavingsGoalService service;
    
    public SavingsGoalResolver(SavingsGoalService service) {
        this.service = service;
    }
    
    // =========================================================================
    // QUERIES
    // =========================================================================
    
    /**
     * Query: savingsGoal(id: ID!): SavingsGoal
     * 
     * Obtiene una meta de ahorro por su ID.
     * 
     * 💡 EJEMPLO:
     * ```graphql
     * query {
     *   savingsGoal(id: "1") {
     *     name
     *     targetAmount
     *     currentAmount
     *     progressPercentage
     *   }
     * }
     * ```
     * 
     * @param id ID de la meta (String en GraphQL, Long en BD)
     * @return Map con los campos de SavingsGoal
     */
    @DgsQuery
    public Map<String, Object> savingsGoal(@InputArgument String id) {
        SavingsGoalEntity entity = service.getGoalById(Long.parseLong(id));
        return toGraphQL(entity);
    }
    
    /**
     * Query: savingsGoals(userId: ID!): [SavingsGoal!]!
     * 
     * Obtiene todas las metas de un usuario.
     * 
     * 💡 EJEMPLO:
     * ```graphql
     * query {
     *   savingsGoals(userId: "1") {
     *     name
     *     status
     *     progressPercentage
     *   }
     * }
     * ```
     * 
     * @param userId ID del usuario
     * @return Lista de metas del usuario
     */
    @DgsQuery
    public List<Map<String, Object>> savingsGoals(@InputArgument String userId) {
        return service.getGoalsByUserId(Long.parseLong(userId)).stream()
                .map(this::toGraphQL)
                .collect(Collectors.toList());
    }
    
    /**
     * Query: activeSavingsGoals(userId: ID!): [SavingsGoal!]!
     * 
     * Obtiene solo las metas activas de un usuario.
     * 
     * @param userId ID del usuario
     * @return Lista de metas con status = ACTIVE
     */
    @DgsQuery
    public List<Map<String, Object>> activeSavingsGoals(@InputArgument String userId) {
        return service.getActiveGoalsByUserId(Long.parseLong(userId)).stream()
                .map(this::toGraphQL)
                .collect(Collectors.toList());
    }
    
    // =========================================================================
    // MUTATIONS
    // =========================================================================
    
    /**
     * Mutation: createSavingsGoal(input: CreateSavingsGoalInput!): SavingsGoalResponse!
     * 
     * Crea una nueva meta de ahorro.
     * 
     * 🎓 SECCIÓN 4.5: MANEJO DE ERRORES
     * 
     * En vez de lanzar excepciones GraphQL, usamos un patrón de respuesta
     * estructurada con success/message/data.
     * 
     * 💡 EJEMPLO:
     * ```graphql
     * mutation {
     *   createSavingsGoal(input: {
     *     userId: "1"
     *     name: "Vacaciones 2025"
     *     description: "Viaje a Europa"
     *     targetAmount: 5000.00
     *     category: VACATION
     *   }) {
     *     success
     *     message
     *     goal {
     *       id
     *       name
     *       progressPercentage
     *     }
     *   }
     * }
     * ```
     * 
     * @param input Datos de la nueva meta
     * @return SavingsGoalResponse con success, message y goal
     */
    @DgsMutation
    public Map<String, Object> createSavingsGoal(@InputArgument Map<String, Object> input) {
        try {
            // Construir entidad desde input
            SavingsGoalEntity entity = SavingsGoalEntity.builder()
                    .userId(Long.parseLong(input.get("userId").toString()))
                    .name(input.get("name").toString())
                    .description(input.get("description") != null ? input.get("description").toString() : null)
                    .targetAmount(new BigDecimal(input.get("targetAmount").toString()))
                    .category(SavingsGoalEntity.GoalCategory.valueOf(input.get("category").toString()))
                    .build();
            
            // Guardar
            SavingsGoalEntity saved = service.createGoal(entity);
            
            // Respuesta exitosa
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Goal created successfully");
            response.put("goal", toGraphQL(saved));
            return response;
            
        } catch (Exception e) {
            // Respuesta de error
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("goal", null);
            return response;
        }
    }
    
    // =========================================================================
    // TRANSFORMACIÓN ENTITY → GRAPHQL
    // =========================================================================
    
    /**
     * Transforma una entidad JPA a un Map para GraphQL.
     * 
     * 🎓 SECCIÓN 4.4: CAMPOS CALCULADOS
     * 
     * progressPercentage NO está en la base de datos,
     * se calcula aquí cada vez que se consulta.
     * 
     * MAPEO DE CAMPOS:
     * ```
     * Entity (JPA)          GraphQL Schema
     * ─────────────────────────────────────
     * goalId           →    id
     * userId           →    userId
     * name             →    name
     * description      →    description
     * targetAmount     →    targetAmount
     * currentAmount    →    currentAmount
     * category         →    category
     * status           →    status
     * (calculado)      →    progressPercentage
     * ```
     * 
     * @param entity Entidad JPA
     * @return Map con campos para GraphQL
     */
    private Map<String, Object> toGraphQL(SavingsGoalEntity entity) {
        Map<String, Object> map = new HashMap<>();
        
        // Mapeo de campos (nota: goalId → id)
        map.put("id", entity.getGoalId().toString());
        map.put("userId", entity.getUserId().toString());
        map.put("name", entity.getName());
        map.put("description", entity.getDescription());
        map.put("targetAmount", entity.getTargetAmount());
        map.put("currentAmount", entity.getCurrentAmount());
        map.put("category", entity.getCategory().name());
        map.put("status", entity.getStatus().name());
        
        // Campo calculado
        map.put("progressPercentage", calculateProgress(entity));
        
        return map;
    }
    
    /**
     * Calcula el porcentaje de progreso.
     * 
     * Fórmula: (currentAmount / targetAmount) × 100
     * 
     * @param entity Meta de ahorro
     * @return Porcentaje (0.0 a 100.0+)
     */
    private double calculateProgress(SavingsGoalEntity entity) {
        if (entity.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return entity.getCurrentAmount()
                .divide(entity.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO - SECCIÓN 4.1 + 4.4
 * =============================================================================
 * 
 * 📊 QUERIES IMPLEMENTADAS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  savingsGoal(id)        │  Obtener meta por ID                         │
 * │  savingsGoals(userId)   │  Todas las metas de un usuario               │
 * │  activeSavingsGoals()   │  Solo metas activas                          │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 📊 MUTATIONS IMPLEMENTADAS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  createSavingsGoal      │  Crear nueva meta                            │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 📊 CAMPOS CALCULADOS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  progressPercentage     │  (currentAmount / targetAmount) × 100        │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * =============================================================================
 */