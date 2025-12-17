package com.neobank.cashback.service;

import com.neobank.cashback.domain.CashbackRule;
import com.neobank.cashback.domain.RedemptionResult;
import com.neobank.cashback.domain.Reward;
import com.neobank.cashback.domain.RewardStatus;
import com.neobank.cashback.domain.RewardTier;
import com.neobank.cashback.domain.TierMultipliers;
import com.neobank.cashback.domain.TransactionCategory;
import com.neobank.cashback.domain.User;
import com.neobank.cashback.repository.CashbackRuleRepository;
import com.neobank.cashback.repository.RewardRepository;
import com.neobank.cashback.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de lógica de negocio para el programa de Cashback.
 * 
 * 🎓 SECCIÓN 3.4: MUTATIONS Y LÓGICA DE NEGOCIO INTEGRADA
 * 
 * Este servicio encapsula TODA la lógica de negocio del programa:
 * - Cálculo de cashback según reglas y tier del usuario
 * - Creación de rewards por transacciones
 * - Redención de cashback acumulado
 * - Actualización de estados de rewards
 * - Expiración automática de rewards vencidas
 * - Upgrade de tier de usuarios
 * 
 * 💡 SEPARACIÓN DE RESPONSABILIDADES:
 * ```
 * ┌────────────────────────────────────────────────────────────────┐
 * │                    DataFetcher (GraphQL)                      │
 * │  - Recibe la request GraphQL                                  │
 * │  - Extrae argumentos                                          │
 * │  - Delega a CashbackService                                   │
 * │  - Retorna resultado                                          │
 * └────────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌────────────────────────────────────────────────────────────────┐
 * │                    CashbackService                            │
 * │  - Validaciones de negocio                                    │
 * │  - Cálculos complejos                                         │
 * │  - Orquestación de operaciones                                │
 * │  - Transaccionalidad (en Cap. 4 con BD real)                  │
 * └────────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌────────────────────────────────────────────────────────────────┐
 * │                    Repository Layer                           │
 * │  - Acceso a datos                                             │
 * │  - CRUD operations                                            │
 * └────────────────────────────────────────────────────────────────┘
 * ```
 * 
 * 📊 FÓRMULA DE CASHBACK:
 * ```
 * cashback = transactionAmount × (basePercentage / 100) × tierMultiplier
 * 
 * Ejemplo:
 * - Usuario GOLD compra $100 en GROCERIES
 * - GROCERIES base = 2%
 * - GOLD multiplier = 2.0x
 * - Cashback = $100 × 0.02 × 2.0 = $4.00
 * ```
 * 
 * @see MutationDataFetcher (consume este servicio)
 * @see CashbackRuleRepository (reglas por categoría)
 */
@Service
public class CashbackService {
    
    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;
    private final CashbackRuleRepository ruleRepository;
    
    public CashbackService(UserRepository userRepository,
                           RewardRepository rewardRepository,
                           CashbackRuleRepository ruleRepository) {
        this.userRepository = userRepository;
        this.rewardRepository = rewardRepository;
        this.ruleRepository = ruleRepository;
    }
    
    /**
     * Calcula cuánto cashback se debe otorgar por una transacción.
     * 
     * 🎓 LÓGICA DE CÁLCULO:
     * 1. Obtener usuario y su tier
     * 2. Obtener regla de la categoría
     * 3. Verificar que la regla esté activa
     * 4. Verificar monto mínimo de transacción
     * 5. Aplicar fórmula: amount × (base% / 100) × tierMultiplier
     * 6. Aplicar tope máximo si corresponde
     * 
     * 💡 EJEMPLO:
     * ```
     * Usuario: PLATINUM (multiplier 3.0x)
     * Categoría: TRAVEL (base 3%)
     * Monto: $500
     * Cashback = $500 × 0.03 × 3.0 = $45.00
     * Tope máximo TRAVEL: $200 → Cashback final: $45.00
     * ```
     * 
     * @param userId ID del usuario
     * @param transactionAmount Monto de la transacción
     * @param category Categoría de la transacción
     * @return Monto de cashback calculado
     */
    public BigDecimal calculateCashback(String userId, BigDecimal transactionAmount,
                                        TransactionCategory category) {
        // Obtener usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Obtener regla de la categoría
        CashbackRule rule = ruleRepository.findByCategory(category)
                .orElseThrow(() -> new RuntimeException("Rule not found for category: " + category));
        
        // Verificar que la regla esté activa
        if (!rule.getIsActive()) {
            return BigDecimal.ZERO;
        }
        
        // Verificar monto mínimo de transacción
        if (transactionAmount.compareTo(rule.getMinTransactionAmount()) < 0) {
            return BigDecimal.ZERO;
        }
        
        // Obtener multiplicador según tier del usuario
        Double tierMultiplier = getTierMultiplier(user.getTier(), rule.getTierMultipliers());
        
        // Calcular cashback: amount × (percentage / 100) × multiplier
        BigDecimal cashback = transactionAmount
                .multiply(BigDecimal.valueOf(rule.getBasePercentage()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(tierMultiplier))
                .setScale(2, RoundingMode.HALF_UP);
        
        // Aplicar tope máximo por transacción
        if (cashback.compareTo(rule.getMaxCashbackPerTransaction()) > 0) {
            cashback = rule.getMaxCashbackPerTransaction();
        }
        
        return cashback;
    }
    
    /**
     * Crea una nueva reward basada en una transacción.
     * 
     * 🎓 MUTATION: createReward
     * 
     * FLUJO:
     * 1. Calcular cashback según reglas
     * 2. Crear objeto Reward con estado ACTIVE
     * 3. Establecer fecha de expiración (90 días)
     * 4. Guardar reward en repositorio
     * 5. Actualizar balance del usuario
     * 
     * @param userId ID del usuario
     * @param transactionId ID de la transacción origen
     * @param transactionAmount Monto de la transacción
     * @param category Categoría de la transacción
     * @param description Descripción opcional
     * @return Reward creada
     * @throws RuntimeException si el usuario no existe o no califica
     */
    public Reward createReward(String userId, String transactionId,
                               BigDecimal transactionAmount, TransactionCategory category,
                               String description) {
        // Validar usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Calcular cashback
        BigDecimal cashbackAmount = calculateCashback(userId, transactionAmount, category);
        
        if (cashbackAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("Transaction does not qualify for cashback");
        }
        
        // Obtener multiplicador para guardar en la reward
        CashbackRule rule = ruleRepository.findByCategory(category).orElseThrow();
        Double tierMultiplier = getTierMultiplier(user.getTier(), rule.getTierMultipliers());
        
        // Crear reward
        Reward reward = new Reward();
        reward.setUserId(userId);
        reward.setAmount(cashbackAmount);
        reward.setEarnedAt(LocalDateTime.now());
        reward.setExpiresAt(LocalDateTime.now().plusDays(90)); // 90 días de vigencia
        reward.setStatus(RewardStatus.ACTIVE);
        reward.setCategory(category);
        reward.setTransactionId(transactionId);
        reward.setDescription(description != null ? description : category.name() + " cashback");
        reward.setMultiplier(tierMultiplier);
        
        // Guardar reward
        Reward saved = rewardRepository.save(reward);
        
        // Actualizar balance del usuario
        user.setTotalCashbackEarned(user.getTotalCashbackEarned().add(cashbackAmount));
        user.setAvailableCashback(user.getAvailableCashback().add(cashbackAmount));
        userRepository.save(user);
        
        return saved;
    }
    
    /**
     * Redime cashback disponible del usuario.
     * 
     * 🎓 MUTATION: redeemCashback
     * 
     * VALIDACIONES:
     * - Usuario debe existir
     * - Balance suficiente
     * - Monto mínimo de redención ($10)
     * 
     * FLUJO:
     * 1. Validar usuario y balance
     * 2. Marcar rewards como REDEEMED hasta cubrir el monto
     * 3. Actualizar balance del usuario
     * 4. Generar ID de transacción de redención
     * 
     * @param userId ID del usuario
     * @param amount Monto a redimir
     * @param destinationAccount Cuenta destino
     * @return RedemptionResult con el resultado de la operación
     */
    public RedemptionResult redeemCashback(String userId, BigDecimal amount,
                                           String destinationAccount) {
        // Validar usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Validar balance suficiente
        if (user.getAvailableCashback().compareTo(amount) < 0) {
            return new RedemptionResult(
                    false,
                    "Insufficient cashback balance. Available: " + user.getAvailableCashback(),
                    null,
                    user.getAvailableCashback(),
                    null
            );
        }
        
        // Validar monto mínimo
        BigDecimal minRedemption = new BigDecimal("10.00");
        if (amount.compareTo(minRedemption) < 0) {
            return new RedemptionResult(
                    false,
                    "Minimum redemption amount is " + minRedemption,
                    null,
                    user.getAvailableCashback(),
                    null
            );
        }
        
        // Marcar rewards como REDEEMED
        List<Reward> activeRewards = rewardRepository.findByUserIdAndStatus(userId, RewardStatus.ACTIVE);
        BigDecimal remaining = amount;
        
        for (Reward reward : activeRewards) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            
            reward.setStatus(RewardStatus.REDEEMED);
            remaining = remaining.subtract(reward.getAmount());
            rewardRepository.save(reward);
        }
        
        // Actualizar balance del usuario
        user.setAvailableCashback(user.getAvailableCashback().subtract(amount));
        userRepository.save(user);
        
        // Generar ID de transacción
        String transactionId = "redemption-" + UUID.randomUUID().toString().substring(0, 8);
        
        return new RedemptionResult(
                true,
                "Cashback redeemed successfully to account " + destinationAccount,
                amount,
                user.getAvailableCashback(),
                transactionId
        );
    }
    
    /**
     * Actualiza el estado de una reward.
     * 
     * 🎓 MUTATION: updateRewardStatus
     * 
     * Casos de uso:
     * - Cancelar por fraude
     * - Marcar como expirada
     * - Reversiones por devoluciones
     * 
     * @param rewardId ID de la reward
     * @param newStatus Nuevo estado
     * @param reason Razón del cambio
     * @return Reward actualizada
     */
    public Reward updateRewardStatus(String rewardId, RewardStatus newStatus, String reason) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new RuntimeException("Reward not found: " + rewardId));
        
        RewardStatus oldStatus = reward.getStatus();
        reward.setStatus(newStatus);
        
        if (reason != null) {
            reward.setDescription(reward.getDescription() + " [" + reason + "]");
        }
        
        Reward updated = rewardRepository.save(reward);
        
        // Si se cancela o expira una reward ACTIVE, ajustar balance
        if ((newStatus == RewardStatus.CANCELLED || newStatus == RewardStatus.EXPIRED)
                && oldStatus == RewardStatus.ACTIVE) {
            User user = userRepository.findById(reward.getUserId()).orElseThrow();
            user.setAvailableCashback(user.getAvailableCashback().subtract(reward.getAmount()));
            userRepository.save(user);
        }
        
        return updated;
    }
    
    /**
     * Expira todas las rewards vencidas.
     * 
     * 🎓 MUTATION: expireOldRewards
     * 
     * Este método típicamente se ejecuta via:
     * - Cron job diario
     * - Spring @Scheduled
     * - Manualmente por admin
     * 
     * @return Cantidad de rewards expiradas
     */
    public int expireOldRewards() {
        LocalDateTime now = LocalDateTime.now();
        List<Reward> activeRewards = rewardRepository.findByStatus(RewardStatus.ACTIVE);
        
        int count = 0;
        for (Reward reward : activeRewards) {
            if (reward.getExpiresAt() != null && reward.getExpiresAt().isBefore(now)) {
                updateRewardStatus(reward.getId(), RewardStatus.EXPIRED, "Auto-expired");
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Actualiza el tier de un usuario.
     * 
     * 🎓 MUTATION: upgradeUserTier
     * 
     * @param userId ID del usuario
     * @param newTier Nuevo tier
     * @return Usuario actualizado
     */
    public User upgradeUserTier(String userId, RewardTier newTier) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        user.setTier(newTier);
        return userRepository.save(user);
    }
    
    /**
     * Obtiene el multiplicador según el tier del usuario.
     * 
     * @param tier Tier del usuario
     * @param multipliers Multiplicadores configurados
     * @return Multiplicador a aplicar
     */
    private Double getTierMultiplier(RewardTier tier, TierMultipliers multipliers) {
        return switch (tier) {
            case BRONZE -> multipliers.getBronze();
            case SILVER -> multipliers.getSilver();
            case GOLD -> multipliers.getGold();
            case PLATINUM -> multipliers.getPlatinum();
        };
    }
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO - SECCIÓN 3.4
 * =============================================================================
 * 
 * 📊 OPERACIONES IMPLEMENTADAS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  calculateCashback     │  Calcula cashback por transacción             │
 * │  createReward          │  Crea reward y actualiza balance              │
 * │  redeemCashback        │  Canjea cashback disponible                   │
 * │  updateRewardStatus    │  Cambia estado de una reward                  │
 * │  expireOldRewards      │  Expira rewards vencidas (batch)              │
 * │  upgradeUserTier       │  Promociona usuario a nuevo tier              │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 📊 TABLA DE CASHBACK:
 * ┌──────────────┬────────┬─────────┬────────┬──────────┬───────────┐
 * │ Categoría    │ Base % │ BRONZE  │ SILVER │ GOLD     │ PLATINUM  │
 * ├──────────────┼────────┼─────────┼────────┼──────────┼───────────┤
 * │ TRAVEL       │ 3.0%   │ 3.0%    │ 4.5%   │ 6.0%     │ 9.0%      │
 * │ GROCERIES    │ 2.0%   │ 2.0%    │ 3.0%   │ 4.0%     │ 6.0%      │
 * │ RESTAURANTS  │ 1.5%   │ 1.5%    │ 2.25%  │ 3.0%     │ 4.5%      │
 * │ SHOPPING     │ 1.0%   │ 1.0%    │ 1.5%   │ 2.0%     │ 3.0%      │
 * │ UTILITIES    │ 0.5%   │ 0.5%    │ 0.75%  │ 1.0%     │ 1.5%      │
 * └──────────────┴────────┴─────────┴────────┴──────────┴───────────┘
 * 
 * =============================================================================
 */