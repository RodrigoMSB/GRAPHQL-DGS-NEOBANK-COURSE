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
 * Servicio de lógica de negocio para Cashback.
 * 
 * Sección 3.4 del temario: Mutations y lógica de negocio integrada.
 * 
 * Este servicio encapsula:
 * - Cálculo de cashback según reglas y tier
 * - Creación de rewards
 * - Redención de cashback
 * - Actualización de estados
 * - Expiración de rewards
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
     * Fórmula: amount * (basePercentage / 100) * tierMultiplier
     */
    public BigDecimal calculateCashback(String userId, BigDecimal transactionAmount, 
                                       TransactionCategory category) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        CashbackRule rule = ruleRepository.findByCategory(category)
            .orElseThrow(() -> new RuntimeException("Rule not found for category: " + category));
        
        if (!rule.getIsActive()) {
            return BigDecimal.ZERO;
        }
        
        // Verificar monto mínimo
        if (transactionAmount.compareTo(rule.getMinTransactionAmount()) < 0) {
            return BigDecimal.ZERO;
        }
        
        // Obtener multiplicador según tier
        Double tierMultiplier = getTierMultiplier(user.getTier(), rule.getTierMultipliers());
        
        // Calcular cashback: amount * (percentage / 100) * multiplier
        BigDecimal cashback = transactionAmount
            .multiply(BigDecimal.valueOf(rule.getBasePercentage()))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(tierMultiplier))
            .setScale(2, RoundingMode.HALF_UP);
        
        // Aplicar tope máximo
        if (cashback.compareTo(rule.getMaxCashbackPerTransaction()) > 0) {
            cashback = rule.getMaxCashbackPerTransaction();
        }
        
        return cashback;
    }
    
    /**
     * Crea una nueva reward basada en una transacción.
     * 
     * Mutation: createReward
     */
    public Reward createReward(String userId, String transactionId, 
                              BigDecimal transactionAmount, TransactionCategory category,
                              String description) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        BigDecimal cashbackAmount = calculateCashback(userId, transactionAmount, category);
        
        if (cashbackAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("Transaction does not qualify for cashback");
        }
        
        CashbackRule rule = ruleRepository.findByCategory(category).orElseThrow();
        Double tierMultiplier = getTierMultiplier(user.getTier(), rule.getTierMultipliers());
        
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
     * Mutation: redeemCashback
     */
    public RedemptionResult redeemCashback(String userId, BigDecimal amount, 
                                          String destinationAccount) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Validar que tenga suficiente balance
        if (user.getAvailableCashback().compareTo(amount) < 0) {
            return new RedemptionResult(
                false,
                "Insufficient cashback balance. Available: " + user.getAvailableCashback(),
                null,
                user.getAvailableCashback(),
                null
            );
        }
        
        // Validar monto mínimo de redención
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
        
        // Marcar rewards como REDEEMED hasta cubrir el monto
        List<Reward> activeRewards = rewardRepository.findByUserIdAndStatus(userId, RewardStatus.ACTIVE);
        BigDecimal remaining = amount;
        
        for (Reward reward : activeRewards) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            
            if (reward.getAmount().compareTo(remaining) <= 0) {
                reward.setStatus(RewardStatus.REDEEMED);
                remaining = remaining.subtract(reward.getAmount());
            } else {
                // Dividir reward (simplificado - en producción sería más complejo)
                reward.setStatus(RewardStatus.REDEEMED);
                remaining = BigDecimal.ZERO;
            }
            rewardRepository.save(reward);
        }
        
        // Actualizar balance del usuario
        user.setAvailableCashback(user.getAvailableCashback().subtract(amount));
        userRepository.save(user);
        
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
     * Mutation: updateRewardStatus
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
        
        // Si se cancela o expira, ajustar balance del usuario
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
     * Mutation: expireOldRewards
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
     * Upgrade del tier de un usuario.
     * 
     * Mutation: upgradeUserTier
     */
    public User upgradeUserTier(String userId, RewardTier newTier) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        user.setTier(newTier);
        return userRepository.save(user);
    }
    
    // Helper methods
    
    private Double getTierMultiplier(RewardTier tier, TierMultipliers multipliers) {
        return switch (tier) {
            case BRONZE -> multipliers.getBronze();
            case SILVER -> multipliers.getSilver();
            case GOLD -> multipliers.getGold();
            case PLATINUM -> multipliers.getPlatinum();
        };
    }
}