package com.neobank.cashback.graphql;

import com.neobank.cashback.model.*;
import com.neobank.cashback.service.CashbackService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Query Resolver para Usuarios.
 * 
 * 🎓 SECCIÓN 2.1: DISEÑO ORIENTADO A DOMINIO
 * 
 * Este resolver demuestra:
 * - Queries para entidad principal (User)
 * - Resolución de relaciones one-to-many (User → Transactions, Rewards)
 * - Campos calculados que agregan datos de otras entidades
 * 
 * 📦 QUERIES IMPLEMENTADAS:
 * - user(id): Obtiene un usuario por ID
 * - userByEmail(email): Busca usuario por email
 * - users(tier): Lista usuarios, opcionalmente filtrados por tier
 * 
 * 📦 CAMPOS CALCULADOS (via @SchemaMapping):
 * - transactions: Lista de transacciones del usuario
 * - rewards: Lista de recompensas del usuario
 * - availableCashback: Suma de cashback disponible para canjear
 * - totalCashbackEarned: Suma total de cashback ganado históricamente
 * - totalSpent: Suma total gastado en transacciones confirmadas
 * 
 * 🎓 RELACIONES DEL MODELO:
 * ```
 * User (1) ────── (N) Transaction
 *   │
 *   └──────────── (N) Reward
 * ```
 * 
 * @see schema.graphqls (type User)
 */
@Controller
public class UserQueryResolver {
    
    private final CashbackService cashbackService;
    
    /**
     * Constructor con inyección de dependencias.
     */
    public UserQueryResolver(CashbackService cashbackService) {
        this.cashbackService = cashbackService;
    }
    
    // =========================================================================
    // QUERIES PRINCIPALES
    // =========================================================================
    
    /**
     * Query: user(id: ID!)
     * 
     * Obtiene un usuario por su ID.
     * 
     * 💡 EJEMPLO:
     * ```graphql
     * query {
     *   user(id: "user-001") {
     *     fullName
     *     tier
     *     availableCashback  # Campo calculado
     *   }
     * }
     * ```
     * 
     * @param id ID del usuario
     * @return User o null si no existe
     */
    @QueryMapping
    public User user(@Argument String id) {
        return cashbackService.getUserById(id);
    }
    
    /**
     * Query: userByEmail(email: Email!)
     * 
     * Busca un usuario por su email.
     * 
     * 🎓 SECCIÓN 2.4: Uso del scalar Email
     * 
     * 💡 EJEMPLO:
     * ```graphql
     * query {
     *   userByEmail(email: "maria.silva@email.com") {
     *     id
     *     fullName
     *     tier
     *   }
     * }
     * ```
     * 
     * @param email Email del usuario (scalar Email)
     * @return User o null si no existe
     */
    @QueryMapping
    public User userByEmail(@Argument String email) {
        return cashbackService.getUserByEmail(email);
    }
    
    /**
     * Query: users(tier: CashbackTier)
     * 
     * Lista todos los usuarios, opcionalmente filtrados por tier.
     * 
     * 💡 EJEMPLOS:
     * ```graphql
     * # Todos los usuarios
     * query { users { id fullName tier } }
     * 
     * # Solo usuarios PLATINUM
     * query { users(tier: PLATINUM) { id fullName } }
     * ```
     * 
     * @param tier Filtro opcional por nivel de cashback
     * @return Lista de usuarios
     */
    @QueryMapping
    public List<User> users(@Argument CashbackTier tier) {
        List<User> allUsers = cashbackService.getAllUsers();
        
        if (tier == null) {
            return allUsers;
        }
        
        return allUsers.stream()
                .filter(u -> u.getTier() == tier)
                .toList();
    }
    
    // =========================================================================
    // CAMPOS CALCULADOS DEL USER (@SchemaMapping)
    // =========================================================================
    //
    // 🎓 SECCIÓN 2.1: RELACIONES Y CAMPOS CALCULADOS
    //
    // Estos campos NO están en la clase User, se resuelven dinámicamente.
    // Solo se ejecutan si el cliente los solicita en la query.
    //
    // =========================================================================
    
    /**
     * Campo calculado: transactions
     * 
     * Resuelve la relación User → Transactions (one-to-many).
     * 
     * 💡 EJEMPLO:
     * ```graphql
     * query {
     *   user(id: "user-001") {
     *     fullName
     *     transactions {    # Invoca este método
     *       id
     *       amount
     *       merchantName
     *     }
     *   }
     * }
     * ```
     * 
     * @param user El usuario padre (inyectado automáticamente)
     * @return Lista de transacciones del usuario
     */
    @SchemaMapping(typeName = "User")
    public List<Transaction> transactions(User user) {
        return cashbackService.getTransactionsByUserId(user.getId());
    }
    
    /**
     * Campo calculado: rewards
     * 
     * Resuelve la relación User → Rewards (one-to-many).
     * 
     * 💡 EJEMPLO:
     * ```graphql
     * query {
     *   user(id: "user-001") {
     *     rewards {
     *       id
     *       amount
     *       status
     *       expiresAt
     *     }
     *   }
     * }
     * ```
     * 
     * @param user El usuario padre
     * @return Lista de recompensas del usuario
     */
    @SchemaMapping(typeName = "User")
    public List<Reward> rewards(User user) {
        return cashbackService.getRewardsByUserId(user.getId());
    }
    
    /**
     * Campo calculado: availableCashback
     * 
     * Suma de todo el cashback disponible para canjear.
     * Solo cuenta rewards con status = AVAILABLE.
     * 
     * 🎓 LÓGICA:
     * SELECT SUM(amount) FROM rewards
     * WHERE userId = ? AND status = 'AVAILABLE'
     * 
     * 💡 EJEMPLO:
     * ```graphql
     * query {
     *   user(id: "user-001") {
     *     availableCashback  # Ej: 45.50
     *   }
     * }
     * ```
     * 
     * @param user El usuario
     * @return Monto total disponible para canjear
     */
    @SchemaMapping(typeName = "User")
    public Double availableCashback(User user) {
        return cashbackService.calculateAvailableCashback(user.getId());
    }
    
    /**
     * Campo calculado: totalCashbackEarned
     * 
     * Suma de todo el cashback ganado históricamente.
     * Cuenta todas las rewards independiente del status.
     * 
     * 🎓 LÓGICA:
     * SELECT SUM(amount) FROM rewards
     * WHERE userId = ?
     * 
     * @param user El usuario
     * @return Total de cashback ganado en toda la historia
     */
    @SchemaMapping(typeName = "User")
    public Double totalCashbackEarned(User user) {
        return cashbackService.calculateTotalCashbackEarned(user.getId());
    }
    
    /**
     * Campo calculado: totalSpent
     * 
     * Suma de todo el dinero gastado en transacciones confirmadas.
     * Solo cuenta transacciones con status = CONFIRMED.
     * 
     * 🎓 LÓGICA:
     * SELECT SUM(amount) FROM transactions
     * WHERE userId = ? AND status = 'CONFIRMED'
     * 
     * @param user El usuario
     * @return Total gastado en transacciones confirmadas
     */
    @SchemaMapping(typeName = "User")
    public Double totalSpent(User user) {
        return cashbackService.calculateTotalSpent(user.getId());
    }
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO - SECCIÓN 2.1
 * =============================================================================
 * 
 * 📊 QUERIES IMPLEMENTADAS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  user(id)           │  Obtener usuario por ID                          │
 * │  userByEmail(email) │  Buscar usuario por email                        │
 * │  users(tier)        │  Listar usuarios, filtro opcional por tier       │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 📊 CAMPOS CALCULADOS (@SchemaMapping):
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  transactions       │  Relación User → Transaction[]                   │
 * │  rewards            │  Relación User → Reward[]                        │
 * │  availableCashback  │  SUM(rewards.amount) WHERE status=AVAILABLE      │
 * │  totalCashbackEarned│  SUM(rewards.amount)                             │
 * │  totalSpent         │  SUM(transactions.amount) WHERE status=CONFIRMED │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 🎯 QUERY DE EJEMPLO COMPLETA:
 * ```graphql
 * query GetUserDashboard($id: ID!) {
 *   user(id: $id) {
 *     fullName
 *     tier
 *     enrolledAt
 *     
 *     # Campos calculados
 *     availableCashback
 *     totalCashbackEarned
 *     totalSpent
 *     
 *     # Relaciones
 *     transactions {
 *       amount
 *       merchantName
 *       cashbackAmount
 *     }
 *     rewards {
 *       amount
 *       status
 *       expiresAt
 *     }
 *   }
 * }
 * ```
 * 
 * =============================================================================
 */