package com.neobank.cashback.repository;

import com.neobank.cashback.domain.Reward;
import com.neobank.cashback.domain.RewardStatus;
import com.neobank.cashback.domain.TransactionCategory;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Repositorio in-memory para rewards.
 * 
 * Simula una base de datos con rewards ya generadas.
 */
@Repository
public class RewardRepository {
    
    private final Map<String, Reward> rewards = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(100);
    
    public RewardRepository() {
        initializeData();
    }
    
    private void initializeData() {
        LocalDateTime now = LocalDateTime.now();
        
        // Rewards para user-001 (María - BRONZE)
        createReward("user-001", new BigDecimal("15.50"), now.minusDays(5), 
                    RewardStatus.ACTIVE, TransactionCategory.GROCERIES, "txn-001", 1.0);
        createReward("user-001", new BigDecimal("8.20"), now.minusDays(10), 
                    RewardStatus.ACTIVE, TransactionCategory.RESTAURANTS, "txn-002", 1.0);
        createReward("user-001", new BigDecimal("22.30"), now.minusDays(15), 
                    RewardStatus.REDEEMED, TransactionCategory.SHOPPING, "txn-003", 1.0);
        
        // Rewards para user-002 (Carlos - SILVER)
        createReward("user-002", new BigDecimal("45.75"), now.minusDays(3), 
                    RewardStatus.ACTIVE, TransactionCategory.TRAVEL, "txn-004", 1.5);
        createReward("user-002", new BigDecimal("12.30"), now.minusDays(7), 
                    RewardStatus.ACTIVE, TransactionCategory.GROCERIES, "txn-005", 1.5);
        createReward("user-002", new BigDecimal("18.90"), now.minusDays(20), 
                    RewardStatus.EXPIRED, TransactionCategory.ENTERTAINMENT, "txn-006", 1.5);
        
        // Rewards para user-003 (Ana - GOLD)
        createReward("user-003", new BigDecimal("89.40"), now.minusDays(2), 
                    RewardStatus.ACTIVE, TransactionCategory.TRAVEL, "txn-007", 2.0);
        createReward("user-003", new BigDecimal("25.60"), now.minusDays(4), 
                    RewardStatus.ACTIVE, TransactionCategory.RESTAURANTS, "txn-008", 2.0);
        createReward("user-003", new BigDecimal("34.50"), now.minusDays(8), 
                    RewardStatus.ACTIVE, TransactionCategory.HEALTH, "txn-009", 2.0);
        createReward("user-003", new BigDecimal("15.20"), now.minusDays(30), 
                    RewardStatus.REDEEMED, TransactionCategory.GROCERIES, "txn-010", 2.0);
        
        // Rewards para user-004 (Roberto - PLATINUM)
        createReward("user-004", new BigDecimal("156.80"), now.minusDays(1), 
                    RewardStatus.ACTIVE, TransactionCategory.TRAVEL, "txn-011", 3.0);
        createReward("user-004", new BigDecimal("78.30"), now.minusDays(3), 
                    RewardStatus.ACTIVE, TransactionCategory.SHOPPING, "txn-012", 3.0);
        createReward("user-004", new BigDecimal("42.90"), now.minusDays(5), 
                    RewardStatus.ACTIVE, TransactionCategory.RESTAURANTS, "txn-013", 3.0);
        
        // Rewards para user-005 (Laura - BRONZE)
        createReward("user-005", new BigDecimal("6.50"), now.minusDays(2), 
                    RewardStatus.ACTIVE, TransactionCategory.GROCERIES, "txn-014", 1.0);
        createReward("user-005", new BigDecimal("4.20"), now.minusDays(6), 
                    RewardStatus.ACTIVE, TransactionCategory.UTILITIES, "txn-015", 1.0);
    }
    
    private void createReward(String userId, BigDecimal amount, LocalDateTime earnedAt,
                             RewardStatus status, TransactionCategory category, 
                             String transactionId, Double multiplier) {
        String id = "reward-" + String.format("%03d", idGenerator.getAndIncrement());
        LocalDateTime expiresAt = status == RewardStatus.ACTIVE ? earnedAt.plusDays(90) : null;
        
        Reward reward = new Reward(
            id, userId, amount, earnedAt, expiresAt, status, category, 
            transactionId, category.name() + " cashback", multiplier
        );
        
        rewards.put(id, reward);
    }
    
    public Optional<Reward> findById(String id) {
        return Optional.ofNullable(rewards.get(id));
    }
    
    public List<Reward> findAll() {
        return new ArrayList<>(rewards.values());
    }
    
    public List<Reward> findByUserId(String userId) {
        return rewards.values().stream()
            .filter(r -> r.getUserId().equals(userId))
            .collect(Collectors.toList());
    }
    
    public List<Reward> findByUserIdAndStatus(String userId, RewardStatus status) {
        return rewards.values().stream()
            .filter(r -> r.getUserId().equals(userId))
            .filter(r -> r.getStatus() == status)
            .collect(Collectors.toList());
    }
    
    public List<Reward> findByStatus(RewardStatus status) {
        return rewards.values().stream()
            .filter(r -> r.getStatus() == status)
            .collect(Collectors.toList());
    }
    
    public Reward save(Reward reward) {
        if (reward.getId() == null) {
            reward.setId("reward-" + String.format("%03d", idGenerator.getAndIncrement()));
        }
        rewards.put(reward.getId(), reward);
        return reward;
    }
    
    public void delete(String id) {
        rewards.remove(id);
    }
    
    public List<String> findUserIdsWithRewards(List<String> rewardIds) {
        return rewardIds.stream()
            .map(id -> rewards.get(id))
            .filter(Objects::nonNull)
            .map(Reward::getUserId)
            .distinct()
            .collect(Collectors.toList());
    }
}
