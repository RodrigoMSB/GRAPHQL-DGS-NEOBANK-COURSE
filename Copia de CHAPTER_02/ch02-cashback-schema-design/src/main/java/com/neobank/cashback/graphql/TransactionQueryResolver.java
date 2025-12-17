package com.neobank.cashback.graphql;

import com.neobank.cashback.model.*;
import com.neobank.cashback.service.CashbackService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Query Resolver para Transacciones.
 * 
 * 🎓 SECCIÓN 2.3: QUERIES COMPLEJAS
 * 
 * Este resolver demuestra:
 * - Queries con múltiples filtros opcionales
 * - Campos calculados con @SchemaMapping
 * - Resolución de relaciones (Transaction → User)
 * 
 * 📦 QUERIES IMPLEMENTADAS:
 * - transaction(id): Obtiene una transacción por ID
 * - transactions(userId, status, category): Lista con filtros opcionales
 * 
 * 📦 CAMPOS CALCULADOS (via @SchemaMapping):
 * - user: Resuelve el User asociado a la Transaction
 * - cashbackAmount: Calcula el monto de cashback
 * - cashbackPercentage: Calcula el porcentaje aplicado
 * 
 * 💡 ¿POR QUÉ @SchemaMapping?
 * Los campos calculados NO están en la clase Transaction.
 * Se resuelven dinámicamente cuando el cliente los solicita.
 * Esto permite:
 * - Lazy loading (no calcular si no se pide)
 * - Lógica compleja que depende de otros datos
 * - Evitar N+1 queries (con DataLoader, visto en Cap. 3)
 * 
 * @see schema.graphqls (type Transaction)
 */
@Controller
public class TransactionQueryResolver {
    
    private final CashbackService cashbackService;
    
    /**
     * Constructor con inyección de dependencias.
     */
    public TransactionQueryResolver(CashbackService cashbackService) {
        this.cashbackService = cashbackService;
    }
    
    /**
     * Query: transaction(id: ID!)
     * 
     * Obtiene una transacción específica por su ID.
     * 
     * 💡 EJEMPLO:
     * ```graphql
     * query {
     *   transaction(id: "trans-001") {
     *     id
     *     amount
     *     merchantName
     *     cashbackAmount    # Campo calculado
     *   }
     * }
     * ```
     * 
     * @param id ID de la transacción
     * @return Transaction o null si no existe
     */
    @QueryMapping
    public Transaction transaction(@Argument String id) {
        return cashbackService.getTransactionById(id);
    }
    
    /**
     * Query: transactions(userId, status, category)
     * 
     * Lista transacciones con filtros opcionales.
     * 
     * 🎓 SECCIÓN 2.3: Filtros opcionales en queries
     * 
     * Todos los argumentos son opcionales (nullable en schema).
     * Se aplican en cascada si están presentes.
     * 
     * 💡 EJEMPLOS:
     * ```graphql
     * # Todas las transacciones
     * query { transactions { id amount } }
     * 
     * # Transacciones de un usuario
     * query { transactions(userId: "user-001") { id } }
     * 
     * # Transacciones confirmadas en restaurantes
     * query {
     *   transactions(status: CONFIRMED, category: RESTAURANTS) {
     *     id
     *     merchantName
     *   }
     * }
     * ```
     * 
     * @param userId Filtrar por usuario (opcional)
     * @param status Filtrar por estado (opcional)
     * @param category Filtrar por categoría (opcional)
     * @return Lista de transacciones que cumplen los filtros
     */
    @QueryMapping
    public List<Transaction> transactions(
            @Argument String userId,
            @Argument TransactionStatus status,
            @Argument TransactionCategory category) {
        
        // Comenzar con todas o filtradas por usuario
        List<Transaction> txs;
        if (userId != null) {
            txs = cashbackService.getTransactionsByUserId(userId);
        } else {
            txs = cashbackService.getAllTransactions();
        }
        
        // Aplicar filtro de status si existe
        if (status != null) {
            txs = txs.stream()
                    .filter(t -> t.getStatus() == status)
                    .toList();
        }
        
        // Aplicar filtro de categoría si existe
        if (category != null) {
            txs = txs.stream()
                    .filter(t -> t.getCategory() == category)
                    .toList();
        }
        
        return txs;
    }
    
    // =========================================================================
    // CAMPOS CALCULADOS DE TRANSACTION (@SchemaMapping)
    // =========================================================================
    //
    // Estos métodos se invocan SOLO cuando el cliente solicita el campo.
    // typeName = "Transaction" indica que son campos del type Transaction.
    //
    // =========================================================================
    
    /**
     * Campo calculado: user
     * 
     * Resuelve la relación Transaction → User.
     * 
     * 🎓 SECCIÓN 2.1: Relaciones en GraphQL
     * 
     * En el schema:
     * ```graphql
     * type Transaction {
     *   user: User!  # Relación resuelta por este método
     * }
     * ```
     * 
     * En la query:
     * ```graphql
     * query {
     *   transaction(id: "trans-001") {
     *     amount
     *     user {        # Invoca este método
     *       fullName
     *       tier
     *     }
     *   }
     * }
     * ```
     * 
     * @param transaction La transacción padre (inyectada automáticamente)
     * @return El usuario asociado a esta transacción
     */
    @SchemaMapping(typeName = "Transaction")
    public User user(Transaction transaction) {
        return cashbackService.getUserById(transaction.getUserId());
    }
    
    /**
     * Campo calculado: cashbackAmount
     * 
     * Calcula el monto de cashback para esta transacción.
     * 
     * 🎓 FÓRMULA:
     * cashbackAmount = amount × (tierPercentage × categoryMultiplier) / 100
     * 
     * 💡 EJEMPLO:
     * - Usuario GOLD (3% base)
     * - Categoría RESTAURANTS (2x multiplier)
     * - Amount: $100
     * - Cashback: $100 × (3% × 2) = $100 × 6% = $6.00
     * 
     * @param transaction La transacción a calcular
     * @return Monto de cashback en la moneda de la transacción
     */
    @SchemaMapping(typeName = "Transaction")
    public Double cashbackAmount(Transaction transaction) {
        return cashbackService.calculateCashbackAmount(transaction);
    }
    
    /**
     * Campo calculado: cashbackPercentage
     * 
     * Calcula el porcentaje de cashback aplicado.
     * 
     * 🎓 FÓRMULA:
     * cashbackPercentage = tierPercentage × categoryMultiplier
     * 
     * 💡 EJEMPLO:
     * - Usuario GOLD (3% base)
     * - Categoría TRAVEL (3x multiplier)
     * - Percentage: 3% × 3 = 9%
     * 
     * @param transaction La transacción a calcular
     * @return Porcentaje de cashback (9.0 = 9%)
     */
    @SchemaMapping(typeName = "Transaction")
    public Double cashbackPercentage(Transaction transaction) {
        return cashbackService.calculateCashbackPercentage(transaction);
    }
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO - SECCIÓN 2.3
 * =============================================================================
 * 
 * 📊 QUERIES IMPLEMENTADAS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  transaction(id)                │  Obtener una por ID                  │
 * │  transactions(userId,status,cat)│  Listar con filtros opcionales       │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 📊 CAMPOS CALCULADOS (@SchemaMapping):
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  user               │  Resuelve relación → User                        │
 * │  cashbackAmount     │  Monto de cashback ganado                        │
 * │  cashbackPercentage │  Porcentaje aplicado (tier × category)           │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 🎯 VENTAJA DE @SchemaMapping:
 * - Solo se ejecuta si el cliente pide el campo
 * - Permite lógica que depende del contexto
 * - Base para implementar DataLoader (evitar N+1)
 * 
 * =============================================================================
 */
