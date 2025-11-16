package com.neobank.cashback.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Usuario del programa de cashback.
 * 
 * SECCIÓN 2.1: Diseño orientado a dominio
 * - Entidad central del sistema
 * - Tiene relación one-to-many con Transaction y Reward
 * - Campos calculados (availableCashback, totalSpent) se resuelven en el resolver
 * 
 * SECCIÓN 2.2: Object type vs Input type
 * - Este es un OUTPUT type (lo que el servidor retorna)
 * - NO se usa directamente para crear usuarios (usaríamos un CreateUserInput)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * ID único del usuario.
     * En GraphQL: ID! (non-null scalar)
     */
    private String id;
    
    /**
     * Email del usuario (validado).
     * En GraphQL: Email! (custom scalar con validación)
     */
    private String email;
    
    /**
     * Nombre completo.
     * En GraphQL: String!
     */
    private String fullName;
    
    /**
     * Nivel en el programa de cashback.
     * Determina el porcentaje base de recompensas.
     * En GraphQL: CashbackTier! (enum non-null)
     */
    private CashbackTier tier;
    
    /**
     * Fecha de inscripción en el programa.
     * En GraphQL: DateTime! (custom scalar)
     */
    private LocalDateTime enrolledAt;
    
    // ------------------------------------------------------------------------
    // NOTA: Las relaciones (transactions, rewards) NO están aquí como campos
    // En GraphQL, se resuelven dinámicamente en el resolver:
    //
    // @SchemaMapping
    // public List<Transaction> transactions(User user) {
    //     return transactionService.findByUserId(user.getId());
    // }
    //
    // Esto permite:
    // - Lazy loading (solo si el cliente los pide)
    // - Filtrado y paginación
    // - No cargar todo en memoria
    // ------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------
    // CAMPOS CALCULADOS
    // Estos NO están en la base de datos, se calculan en el resolver
    // ------------------------------------------------------------------------
    
    /**
     * Total de cashback disponible para redimir.
     * Se calcula sumando todas las rewards con status AVAILABLE.
     * 
     * En GraphQL schema:
     * availableCashback: Money!
     * 
     * En Java resolver:
     * public Double availableCashback(User user) {
     *     return rewardService.calculateAvailable(user.getId());
     * }
     */
    // NO hay campo aquí - se calcula en resolver
    
    /**
     * Total de cashback ganado históricamente.
     * Suma de todas las rewards creadas (independiente del status).
     */
    // NO hay campo aquí - se calcula en resolver
    
    /**
     * Total gastado en transacciones.
     * Suma de amounts de todas las transactions CONFIRMED.
     */
    // NO hay campo aquí - se calcula en resolver
}
