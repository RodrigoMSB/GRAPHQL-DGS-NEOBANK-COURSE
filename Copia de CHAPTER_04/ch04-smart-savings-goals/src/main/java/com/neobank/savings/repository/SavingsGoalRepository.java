package com.neobank.savings.repository;

import com.neobank.savings.model.SavingsGoalEntity;
import com.neobank.savings.model.SavingsGoalEntity.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para acceso a datos de Metas de Ahorro.
 * 
 * 🎓 SECCIÓN 4.2: REPOSITORIOS SPRING DATA JPA
 * 
 * Spring Data JPA genera automáticamente la implementación de este
 * repositorio basándose en los nombres de los métodos.
 * 
 * 💡 ¿CÓMO FUNCIONA LA MAGIA?
 * Spring analiza el nombre del método y genera el SQL:
 * 
 * ```
 * findByUserId(Long userId)
 *    ↓
 * SELECT * FROM savings_goals WHERE user_id = ?
 * 
 * findByUserIdAndStatus(Long userId, GoalStatus status)
 *    ↓
 * SELECT * FROM savings_goals WHERE user_id = ? AND status = ?
 * ```
 * 
 * 📦 MÉTODOS HEREDADOS DE JpaRepository:
 * - save(entity): Inserta o actualiza
 * - findById(id): Busca por ID
 * - findAll(): Obtiene todos
 * - delete(entity): Elimina
 * - count(): Cuenta registros
 * - existsById(id): Verifica existencia
 * 
 * 🎓 CONVENCIÓN DE NOMBRES:
 * ```
 * findBy[Campo]                    → WHERE campo = ?
 * findBy[Campo]And[OtroCampo]      → WHERE campo = ? AND otroCampo = ?
 * findBy[Campo]Or[OtroCampo]       → WHERE campo = ? OR otroCampo = ?
 * findBy[Campo]OrderBy[Otro]Asc    → WHERE campo = ? ORDER BY otro ASC
 * findBy[Campo]GreaterThan         → WHERE campo > ?
 * findBy[Campo]LessThan            → WHERE campo < ?
 * findBy[Campo]Between             → WHERE campo BETWEEN ? AND ?
 * findBy[Campo]Like                → WHERE campo LIKE ?
 * findBy[Campo]IsNull              → WHERE campo IS NULL
 * countBy[Campo]                   → SELECT COUNT(*) WHERE campo = ?
 * ```
 * 
 * @see JpaRepository (interfaz base)
 * @see SavingsGoalEntity (entidad)
 */
@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoalEntity, Long> {
    
    /**
     * Busca todas las metas de un usuario.
     * 
     * SQL generado:
     * SELECT * FROM savings_goals WHERE user_id = ?
     * 
     * @param userId ID del usuario
     * @return Lista de metas del usuario
     */
    List<SavingsGoalEntity> findByUserId(Long userId);
    
    /**
     * Busca metas de un usuario filtradas por estado.
     * 
     * SQL generado:
     * SELECT * FROM savings_goals WHERE user_id = ? AND status = ?
     * 
     * 💡 EJEMPLO DE USO:
     * ```java
     * // Obtener solo metas activas del usuario 1
     * List<SavingsGoalEntity> active = repository.findByUserIdAndStatus(1L, GoalStatus.ACTIVE);
     * ```
     * 
     * @param userId ID del usuario
     * @param status Estado a filtrar
     * @return Lista de metas que coinciden
     */
    List<SavingsGoalEntity> findByUserIdAndStatus(Long userId, GoalStatus status);
    
    // =========================================================================
    // EJEMPLOS DE QUERIES QUE PODRÍAS AGREGAR:
    // =========================================================================
    //
    // // Metas ordenadas por progreso (más cercanas a completarse)
    // @Query("SELECT g FROM SavingsGoalEntity g WHERE g.userId = :userId " +
    //        "ORDER BY (g.currentAmount / g.targetAmount) DESC")
    // List<SavingsGoalEntity> findByUserIdOrderByProgressDesc(@Param("userId") Long userId);
    //
    // // Contar metas activas de un usuario
    // long countByUserIdAndStatus(Long userId, GoalStatus status);
    //
    // // Suma de todos los ahorros de un usuario
    // @Query("SELECT SUM(g.currentAmount) FROM SavingsGoalEntity g WHERE g.userId = :userId")
    // BigDecimal sumCurrentAmountByUserId(@Param("userId") Long userId);
    //
    // // Metas por categoría
    // List<SavingsGoalEntity> findByUserIdAndCategory(Long userId, GoalCategory category);
    //
    // =========================================================================
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO - SECCIÓN 4.2
 * =============================================================================
 * 
 * 📊 MÉTODOS DERIVADOS DE SPRING DATA:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  Método                          │  SQL Generado                       │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │  findByUserId(1L)                │  WHERE user_id = 1                  │
 * │  findByUserIdAndStatus(1L, ACT)  │  WHERE user_id = 1 AND status='ACT' │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 💡 VENTAJAS DE SPRING DATA JPA:
 * - No escribes SQL manualmente
 * - Type-safe (errores en compilación)
 * - Paginación incluida (Pageable)
 * - Soporte para @Query (JPQL/SQL nativo)
 * 
 * =============================================================================
 */