package com.neobank.cashback.graphql;

import com.neobank.cashback.model.Transaction;
import com.neobank.cashback.model.input.CreateTransactionInput;
import com.neobank.cashback.model.response.TransactionResponse;
import com.neobank.cashback.service.CashbackService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * GraphQL Mutation Resolver para Transacciones.
 * 
 * 🎓 SECCIÓN 2.3: MUTATIONS COMPLEJAS
 * 
 * Este resolver demuestra:
 * - Uso de Input Types (CreateTransactionInput)
 * - Patrón de Response con success/message/data
 * - Manejo de errores con try-catch
 * 
 * 📦 MUTATIONS IMPLEMENTADAS:
 * - createTransaction: Crea una nueva transacción y genera su cashback
 * 
 * 🎓 SECCIÓN 2.2: INPUT TYPES vs OUTPUT TYPES
 * 
 * ¿Por qué usar CreateTransactionInput en lugar de Transaction?
 * 
 * 1. INPUT TYPE (CreateTransactionInput):
 *    - Solo campos que el cliente ENVÍA
 *    - No tiene 'id' (se genera en servidor)
 *    - No tiene 'status' (siempre empieza PENDING)
 *    - No tiene campos calculados
 * 
 * 2. OUTPUT TYPE (Transaction):
 *    - Campos que el servidor RETORNA
 *    - Incluye 'id' generado
 *    - Incluye 'status' actual
 *    - Incluye campos calculados (cashbackAmount, etc.)
 * 
 * @see CreateTransactionInput
 * @see TransactionResponse
 */
@Controller
public class TransactionMutationResolver {
    
    private final CashbackService cashbackService;
    
    /**
     * Constructor con inyección de dependencias.
     */
    public TransactionMutationResolver(CashbackService cashbackService) {
        this.cashbackService = cashbackService;
    }
    
    /**
     * Mutation: createTransaction
     * 
     * Crea una nueva transacción y genera automáticamente su cashback.
     * 
     * 🎓 FLUJO:
     * 1. Recibe CreateTransactionInput del cliente
     * 2. Crea Transaction con status PENDING
     * 3. Auto-confirma la transacción (para el demo)
     * 4. Crea Reward asociada con el cashback calculado
     * 5. Retorna TransactionResponse con success/message/transaction
     * 
     * 💡 EJEMPLO DE USO:
     * ```graphql
     * mutation {
     *   createTransaction(input: {
     *     userId: "user-001"
     *     amount: 150.00
     *     category: RESTAURANTS
     *     merchantName: "Pizza Palace"
     *     description: "Cena de cumpleaños"
     *   }) {
     *     success
     *     message
     *     transaction {
     *       id
     *       amount
     *       cashbackAmount      # Campo calculado
     *       cashbackPercentage  # Campo calculado
     *       status
     *     }
     *   }
     * }
     * ```
     * 
     * 💡 EJEMPLO CON VARIABLES:
     * ```graphql
     * mutation CreateTx($input: CreateTransactionInput!) {
     *   createTransaction(input: $input) {
     *     success
     *     transaction { id cashbackAmount }
     *   }
     * }
     * 
     * # Variables:
     * {
     *   "input": {
     *     "userId": "user-001",
     *     "amount": 150.00,
     *     "category": "RESTAURANTS",
     *     "merchantName": "Pizza Palace"
     *   }
     * }
     * ```
     * 
     * @param input Datos de la transacción a crear
     * @return TransactionResponse con resultado de la operación
     */
    @MutationMapping
    public TransactionResponse createTransaction(@Argument CreateTransactionInput input) {
        try {
            // ─────────────────────────────────────────────────────────
            // PASO 1: Crear la transacción con status PENDING
            // ─────────────────────────────────────────────────────────
            Transaction transaction = cashbackService.createTransaction(
                    input.getUserId(),
                    input.getAmount(),
                    input.getCategory(),
                    input.getMerchantName(),
                    input.getDescription(),
                    input.getTransactionDate()
            );
            
            // ─────────────────────────────────────────────────────────
            // PASO 2: Auto-confirmar para el demo
            // En producción, esto sería un proceso separado cuando
            // el comercio confirme el cargo.
            // ─────────────────────────────────────────────────────────
            transaction = cashbackService.confirmTransaction(transaction.getId());
            
            // ─────────────────────────────────────────────────────────
            // PASO 3: Retornar respuesta exitosa
            // ─────────────────────────────────────────────────────────
            return TransactionResponse.builder()
                    .success(true)
                    .message("Transaction created and confirmed successfully")
                    .transaction(transaction)
                    .build();
            
        } catch (Exception e) {
            // ─────────────────────────────────────────────────────────
            // MANEJO DE ERRORES
            // Retornamos success=false con mensaje de error
            // En vez de lanzar excepción que generaría error GraphQL
            // ─────────────────────────────────────────────────────────
            return TransactionResponse.builder()
                    .success(false)
                    .message("Error creating transaction: " + e.getMessage())
                    .build();
        }
    }
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO - SECCIÓN 2.3
 * =============================================================================
 * 
 * 📊 MUTATIONS IMPLEMENTADAS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  createTransaction(input)  │  Crea transacción + genera cashback       │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 🎯 PATRÓN DE RESPONSE:
 * ```graphql
 * type TransactionResponse {
 *   success: Boolean!    # ¿Operación exitosa?
 *   message: String!     # Mensaje para el usuario
 *   transaction: Transaction  # Datos (null si error)
 * }
 * ```
 * 
 * 💡 VENTAJAS DEL PATRÓN:
 * - El cliente siempre recibe una respuesta estructurada
 * - Los errores de negocio no son excepciones GraphQL
 * - Fácil de manejar en el frontend
 * - Consistente en todas las mutations
 * 
 * =============================================================================
 */