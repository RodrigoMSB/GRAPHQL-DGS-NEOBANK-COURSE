package com.neobank.cashback.datafetcher;

import com.neobank.cashback.domain.Reward;
import com.neobank.cashback.domain.RewardStatus;
import com.neobank.cashback.domain.RewardTier;
import com.neobank.cashback.domain.RedemptionResult;
import com.neobank.cashback.domain.TransactionCategory;
import com.neobank.cashback.domain.User;
import com.neobank.cashback.service.CashbackService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;

import java.math.BigDecimal;

/**
 * SECCIÓN 3.4: Mutations y lógica de negocio integrada
 * 
 * Este DataFetcher maneja todas las MUTATIONS del schema.
 * 
 * Las mutations permiten modificar datos en el servidor.
 * A diferencia de REST (POST/PUT/DELETE), en GraphQL todas las mutaciones
 * se envían al mismo endpoint /graphql pero con la palabra clave "mutation".
 * 
 * Buenas prácticas para mutations:
 * - Validar inputs antes de ejecutar lógica
 * - Devolver objetos ricos (no solo boolean)
 * - Manejar errores con mensajes claros
 * - Mantener operaciones atómicas
 */
@DgsComponent
public class MutationDataFetcher {
    
    private final CashbackService cashbackService;
    
    public MutationDataFetcher(CashbackService cashbackService) {
        this.cashbackService = cashbackService;
    }
    
    /**
     * Mutation: createReward(input: CreateRewardInput!): Reward!
     * 
     * Crea una nueva reward basada en una transacción.
     * 
     * Esta mutation se ejecutaría típicamente desde un evento:
     * 1. Usuario hace una compra con tarjeta NeoBank
     * 2. Sistema de transacciones emite evento "TRANSACTION_COMPLETED"
     * 3. Cashback Service consume el evento y ejecuta esta mutation
     * 4. Se crea la reward y se acredita al usuario
     */
    @DgsMutation
    public Reward createReward(@InputArgument CreateRewardInput input) {
        return cashbackService.createReward(
            input.getUserId(),
            input.getTransactionId(),
            input.getTransactionAmount(),
            input.getCategory(),
            input.getDescription()
        );
    }
    
    /**
     * Mutation: redeemCashback(input: RedeemCashbackInput!): RedemptionResult!
     * 
     * Permite al usuario canjear su cashback acumulado.
     * 
     * El cashback puede redimirse como:
     * - Transferencia a cuenta bancaria
     * - Pago de tarjeta de crédito
     * - Crédito en cuenta
     * 
     * Validaciones:
     * - Usuario debe tener balance suficiente
     * - Monto mínimo de redención
     * - Cuenta destino debe ser válida
     */
    @DgsMutation
    public RedemptionResult redeemCashback(@InputArgument RedeemCashbackInput input) {
        return cashbackService.redeemCashback(
            input.getUserId(),
            input.getAmount(),
            input.getDestinationAccount()
        );
    }
    
    /**
     * Mutation: updateRewardStatus(input: UpdateRewardStatusInput!): Reward!
     * 
     * Actualiza el estado de una reward.
     * 
     * Casos de uso:
     * - Cancelar reward por fraude detectado
     * - Marcar reward como expirada
     * - Reversiones por devoluciones
     * 
     * Esta mutation típicamente es para uso administrativo.
     */
    @DgsMutation
    public Reward updateRewardStatus(@InputArgument UpdateRewardStatusInput input) {
        return cashbackService.updateRewardStatus(
            input.getRewardId(),
            input.getNewStatus(),
            input.getReason()
        );
    }
    
    /**
     * Mutation: expireOldRewards: Int!
     * 
     * Job batch que expira rewards vencidas.
     * 
     * Típicamente se ejecutaría:
     * - Via cron job diario
     * - Via scheduler de Spring (@Scheduled)
     * - Manualmente por administrador
     * 
     * Retorna la cantidad de rewards expiradas.
     */
    @DgsMutation
    public Integer expireOldRewards() {
        return cashbackService.expireOldRewards();
    }
    
    /**
     * Mutation: upgradeUserTier(userId: ID!, newTier: RewardTier!): User!
     * 
     * Promociona a un usuario a un tier superior.
     * 
     * Los upgrades de tier pueden basarse en:
     * - Total de cashback ganado
     * - Número de transacciones
     * - Monto total gastado
     * - Criterios promocionales
     */
    @DgsMutation
    public User upgradeUserTier(@InputArgument String userId, 
                                @InputArgument RewardTier newTier) {
        return cashbackService.upgradeUserTier(userId, newTier);
    }
}

/**
 * Input types para las mutations.
 * 
 * DGS puede generar estas clases automáticamente desde el schema,
 * pero definirlas manualmente da mayor control.
 */
class CreateRewardInput {
    private String userId;
    private String transactionId;
    private BigDecimal transactionAmount;
    private TransactionCategory category;
    private String description;
    
    // Getters
    public String getUserId() { return userId; }
    public String getTransactionId() { return transactionId; }
    public BigDecimal getTransactionAmount() { return transactionAmount; }
    public TransactionCategory getCategory() { return category; }
    public String getDescription() { return description; }
}

class RedeemCashbackInput {
    private String userId;
    private BigDecimal amount;
    private String destinationAccount;
    
    // Getters
    public String getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public String getDestinationAccount() { return destinationAccount; }
}

class UpdateRewardStatusInput {
    private String rewardId;
    private RewardStatus newStatus;
    private String reason;
    
    // Getters
    public String getRewardId() { return rewardId; }
    public RewardStatus getNewStatus() { return newStatus; }
    public String getReason() { return reason; }
}