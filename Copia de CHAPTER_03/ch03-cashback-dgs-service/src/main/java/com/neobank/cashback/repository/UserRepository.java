package com.neobank.cashback.repository;

import com.neobank.cashback.domain.RewardTier;
import com.neobank.cashback.domain.User;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repositorio in-memory para usuarios.
 * 
 * En CHAPTER_04 esto se reemplazará por JPA + PostgreSQL.
 * Por ahora, simulamos una base de datos con un ConcurrentHashMap.
 */
@Repository
public class UserRepository {
    
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    public UserRepository() {
        initializeData();
    }
    
    private void initializeData() {
        // Usuario BRONZE
        users.put("user-001", new User(
            "user-001",
            "maria.garcia@email.com",
            "María García",
            RewardTier.BRONZE,
            new BigDecimal("1250.50"),
            new BigDecimal("450.30"),
            LocalDateTime.now().minusMonths(6)
        ));
        
        // Usuario SILVER
        users.put("user-002", new User(
            "user-002",
            "carlos.rodriguez@email.com",
            "Carlos Rodríguez",
            RewardTier.SILVER,
            new BigDecimal("3890.75"),
            new BigDecimal("1200.50"),
            LocalDateTime.now().minusMonths(12)
        ));
        
        // Usuario GOLD
        users.put("user-003", new User(
            "user-003",
            "ana.martinez@email.com",
            "Ana Martínez",
            RewardTier.GOLD,
            new BigDecimal("8760.20"),
            new BigDecimal("2850.00"),
            LocalDateTime.now().minusMonths(18)
        ));
        
        // Usuario PLATINUM
        users.put("user-004", new User(
            "user-004",
            "roberto.lopez@email.com",
            "Roberto López",
            RewardTier.PLATINUM,
            new BigDecimal("25340.90"),
            new BigDecimal("8920.15"),
            LocalDateTime.now().minusMonths(24)
        ));
        
        // Más usuarios BRONZE para testear filtros
        users.put("user-005", new User(
            "user-005",
            "laura.fernandez@email.com",
            "Laura Fernández",
            RewardTier.BRONZE,
            new BigDecimal("580.00"),
            new BigDecimal("180.25"),
            LocalDateTime.now().minusMonths(3)
        ));
    }
    
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }
    
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
    
    public List<User> findByTier(RewardTier tier) {
        return users.values().stream()
            .filter(user -> user.getTier() == tier)
            .collect(Collectors.toList());
    }
    
    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }
    
    public void delete(String id) {
        users.remove(id);
    }
}
