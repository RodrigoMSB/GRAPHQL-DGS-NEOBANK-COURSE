package com.neobank.cashback.service;

import com.neobank.cashback.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para el programa de Cashback.
 * 
 * 🎯 PROPÓSITO:
 * Centraliza toda la lógica de negocio del programa de recompensas:
 * - Gestión de usuarios y sus tiers
 * - Creación y confirmación de transacciones
 * - Cálculo de cashback según tier y categoría
 * - Gestión de rewards (pendientes, disponibles, canjeadas)
 * 
 * 📦 DATOS EN MEMORIA:
 * Para el demo, usa HashMaps en memoria. En producción se reemplazaría
 * por repositorios JPA conectados a base de datos.
 * 
 * 🎓 SECCIÓN 2.1: DISEÑO ORIENTADO A DOMINIO
 * 
 * Este servicio implementa el modelo de dominio:
 * ```
 * User (1) ──────┬────── (N) Transaction
 *   │            │              │
 *   │ tier       │              │ (1:1)
 *   │            │              ▼
 *   └────────────┴────── (N) Reward
 * ```
 * 
 * 📊 DATOS INICIALES:
 * - User 1: Maria Silva (GOLD) - 3 transacciones
 * - User 2: Carlos Rodriguez (PLATINUM) - 2 transacciones
 * - Rewards generadas automáticamente para cada transacción
 * 
 * @see CashbackTier (porcentajes base por tier)
 * @see TransactionCategory (multiplicadores por categoría)
 */
@Service
public class CashbackService {
    
    /** Almacén de usuarios. Key: userId */
    private final Map<String, User> users = new HashMap<>();
    
    /** Almacén de transacciones. Key: transactionId */
    private final Map<String, Transaction> transactions = new HashMap<>();
    
    /** Almacén de rewards. Key: rewardId */
    private final Map<String, Reward> rewards = new HashMap<>();
    
    /**
     * Constructor que inicializa datos de demostración.
     */
    public CashbackService() {
        initMockData();
    }
    
    /**
     * Inicializa datos de ejemplo para demostración.
     * 
     * 📊 DATOS CREADOS:
     * 
     * USER 1: Maria Silva
     * - Tier: GOLD (3% base)
     * - Transacciones:
     *   - $150 en GROCERIES (1.5x) → 4.5% = $6.75 cashback
     *   - $85.50 en RESTAURANTS (2x) → 6% = $5.13 cashback
     *   - $1200 en TRAVEL (3x) → 9% = $108.00 cashback
     * 
     * USER 2: Carlos Rodriguez
     * - Tier: PLATINUM (5% base)
     * - Transacciones:
     *   - $60 en GAS_STATIONS (1.5x) → 7.5% = $4.50 cashback
     *   - $450 en SHOPPING (1x) → 5% = $22.50 cashback
     */
    private void initMockData() {
        // ─────────────────────────────────────────────────────────────────
        // USER 1: Maria Silva (GOLD tier - 3% base)
        // ─────────────────────────────────────────────────────────────────
        User user1 = User.builder()
                .id("user-001")
                .email("maria.silva@email.com")
                .fullName("Maria Silva")
                .tier(CashbackTier.GOLD)
                .enrolledAt(LocalDateTime.now().minusMonths(6))
                .build();
        users.put(user1.getId(), user1);
        
        // ─────────────────────────────────────────────────────────────────
        // USER 2: Carlos Rodriguez (PLATINUM tier - 5% base)
        // ─────────────────────────────────────────────────────────────────
        User user2 = User.builder()
                .id("user-002")
                .email("carlos.rodriguez@email.com")
                .fullName("Carlos Rodriguez")
                .tier(CashbackTier.PLATINUM)
                .enrolledAt(LocalDateTime.now().minusYears(2))
                .build();
        users.put(user2.getId(), user2);
        
        // ─────────────────────────────────────────────────────────────────
        // TRANSACCIONES DE USER 1 (Maria)
        // ─────────────────────────────────────────────────────────────────
        createMockTransaction("trans-001", "user-001", 150.0, 
                TransactionCategory.GROCERIES, "Supermarket XYZ");
        createMockTransaction("trans-002", "user-001", 85.50, 
                TransactionCategory.RESTAURANTS, "Pizza House");
        createMockTransaction("trans-003", "user-001", 1200.0, 
                TransactionCategory.TRAVEL, "Airline Tickets");
        
        // ─────────────────────────────────────────────────────────────────
        // TRANSACCIONES DE USER 2 (Carlos)
        // ─────────────────────────────────────────────────────────────────
        createMockTransaction("trans-004", "user-002", 60.0, 
                TransactionCategory.GAS_STATIONS, "Shell Station");
        createMockTransaction("trans-005", "user-002", 450.0, 
                TransactionCategory.SHOPPING, "Electronics Store");
    }
    
    /**
     * Crea una transacción mock con su reward asociada.
     * 
     * @param id ID de la transacción
     * @param userId ID del usuario
     * @param amount Monto de la compra
     * @param category Categoría de la transacción
     * @param merchant Nombre del comercio
     */
    private void createMockTransaction(String id, String userId, Double amount,
                                       TransactionCategory category, String merchant) {
        // Crear transacción confirmada
        Transaction tx = Transaction.builder()
                .id(id)
                .userId(userId)
                .amount(amount)
                .category(category)
                .merchantName(merchant)
                .transactionDate(LocalDateTime.now().minusDays(new Random().nextInt(30)))
                .status(TransactionStatus.CONFIRMED)
                .build();
        transactions.put(id, tx);
        
        // Calcular y crear reward
        User user = users.get(userId);
        double cashbackPercentage = user.getTier().getCashbackPercentage(category);
        double cashbackAmount = amount * (cashbackPercentage / 100.0);
        
        Reward reward = Reward.builder()
                .id("reward-" + id)
                .userId(userId)
                .transactionId(id)
                .amount(cashbackAmount)
                .earnedAt(tx.getTransactionDate())
                .expiresAt(tx.getTransactionDate().plusMonths(12))
                .status(RewardStatus.AVAILABLE)
                .build();
        rewards.put(reward.getId(), reward);
    }
    
    // =========================================================================
    // MÉTODOS DE CONSULTA - USUARIOS
    // =========================================================================
    
    /**
     * Obtiene un usuario por ID.
     */
    public User getUserById(String id) {
        return users.get(id);
    }
    
    /**
     * Busca un usuario por email.
     */
    public User getUserByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Obtiene todos los usuarios.
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    // =========================================================================
    // MÉTODOS DE CONSULTA - TRANSACCIONES
    // =========================================================================
    
    /**
     * Obtiene una transacción por ID.
     */
    public Transaction getTransactionById(String id) {
        return transactions.get(id);
    }
    
    /**
     * Obtiene transacciones de un usuario.
     */
    public List<Transaction> getTransactionsByUserId(String userId) {
        return transactions.values().stream()
                .filter(t -> t.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las transacciones.
     */
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values());
    }
    
    // =========================================================================
    // MÉTODOS DE CONSULTA - REWARDS
    // =========================================================================
    
    /**
     * Obtiene una reward por ID.
     */
    public Reward getRewardById(String id) {
        return rewards.get(id);
    }
    
    /**
     * Obtiene rewards de un usuario.
     */
    public List<Reward> getRewardsByUserId(String userId) {
        return rewards.values().stream()
                .filter(r -> r.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
    
    // =========================================================================
    // MÉTODOS DE CÁLCULO - CASHBACK
    // =========================================================================
    
    /**
     * Calcula el cashback disponible para canjear.
     * 
     * Solo suma rewards con status = AVAILABLE.
     * 
     * @param userId ID del usuario
     * @return Monto total disponible
     */
    public Double calculateAvailableCashback(String userId) {
        return rewards.values().stream()
                .filter(r -> r.getUserId().equals(userId))
                .filter(r -> r.getStatus() == RewardStatus.AVAILABLE)
                .mapToDouble(Reward::getAmount)
                .sum();
    }
    
    /**
     * Calcula el total de cashback ganado históricamente.
     * 
     * Suma todas las rewards del usuario.
     * 
     * @param userId ID del usuario
     * @return Total ganado en toda la historia
     */
    public Double calculateTotalCashbackEarned(String userId) {
        return rewards.values().stream()
                .filter(r -> r.getUserId().equals(userId))
                .mapToDouble(Reward::getAmount)
                .sum();
    }
    
    /**
     * Calcula el total gastado en transacciones.
     * 
     * Solo suma transacciones CONFIRMED.
     * 
     * @param userId ID del usuario
     * @return Total gastado
     */
    public Double calculateTotalSpent(String userId) {
        return transactions.values().stream()
                .filter(t -> t.getUserId().equals(userId))
                .filter(t -> t.getStatus() == TransactionStatus.CONFIRMED)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
    
    /**
     * Calcula el monto de cashback para una transacción.
     * 
     * 🎓 FÓRMULA:
     * cashbackAmount = amount × (tierPercentage × categoryMultiplier) / 100
     * 
     * @param transaction Transacción a calcular
     * @return Monto del cashback
     */
    public Double calculateCashbackAmount(Transaction transaction) {
        User user = users.get(transaction.getUserId());
        if (user == null) return 0.0;
        
        double percentage = user.getTier().getCashbackPercentage(transaction.getCategory());
        return transaction.getAmount() * (percentage / 100.0);
    }
    
    /**
     * Calcula el porcentaje de cashback para una transacción.
     * 
     * 🎓 FÓRMULA:
     * percentage = tierBasePercentage × categoryMultiplier
     * 
     * @param transaction Transacción a calcular
     * @return Porcentaje (ej: 6.0 = 6%)
     */
    public Double calculateCashbackPercentage(Transaction transaction) {
        User user = users.get(transaction.getUserId());
        if (user == null) return 0.0;
        
        return user.getTier().getCashbackPercentage(transaction.getCategory());
    }
    
    // =========================================================================
    // MÉTODOS DE MUTACIÓN - TRANSACCIONES
    // =========================================================================
    
    /**
     * Crea una nueva transacción.
     * 
     * La transacción se crea con status PENDING.
     * El cashback se genera cuando se confirma.
     * 
     * @param userId ID del usuario
     * @param amount Monto de la compra
     * @param category Categoría
     * @param merchantName Nombre del comercio
     * @param description Descripción opcional
     * @param date Fecha opcional (default: now)
     * @return Transacción creada
     */
    public Transaction createTransaction(String userId, Double amount,
                                         TransactionCategory category, String merchantName,
                                         String description, LocalDateTime date) {
        String id = "trans-" + UUID.randomUUID().toString().substring(0, 8);
        
        Transaction transaction = Transaction.builder()
                .id(id)
                .userId(userId)
                .amount(amount)
                .category(category)
                .merchantName(merchantName)
                .description(description)
                .transactionDate(date != null ? date : LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();
        
        transactions.put(id, transaction);
        return transaction;
    }
    
    /**
     * Confirma una transacción y genera su reward.
     * 
     * 🎓 FLUJO:
     * 1. Cambia status a CONFIRMED
     * 2. Calcula cashback según tier y categoría
     * 3. Crea Reward con status PENDING (30 días de espera)
     * 
     * @param transactionId ID de la transacción a confirmar
     * @return Transacción confirmada (o null si no existe)
     */
    public Transaction confirmTransaction(String transactionId) {
        Transaction tx = transactions.get(transactionId);
        if (tx == null) return null;
        
        // Confirmar transacción
        tx.setStatus(TransactionStatus.CONFIRMED);
        
        // Calcular y crear reward
        double cashbackAmount = calculateCashbackAmount(tx);
        
        Reward reward = Reward.builder()
                .id("reward-" + UUID.randomUUID().toString().substring(0, 8))
                .userId(tx.getUserId())
                .transactionId(tx.getId())
                .amount(cashbackAmount)
                .earnedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMonths(12))
                .status(RewardStatus.PENDING) // 30 días de espera
                .build();
        
        rewards.put(reward.getId(), reward);
        return tx;
    }
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO
 * =============================================================================
 * 
 * 📊 DATOS MOCK DISPONIBLES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  Users         │  2 (Maria GOLD, Carlos PLATINUM)                      │
 * │  Transactions  │  5 (3 de Maria, 2 de Carlos)                          │
 * │  Rewards       │  5 (una por cada transacción)                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 📊 CÁLCULO DE CASHBACK:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  Tier      │  Base  │  + TRAVEL (3x)  │  + RESTAURANTS (2x)            │
 * ├────────────┼────────┼─────────────────┼─────────────────────────────────┤
 * │  BRONZE    │   1%   │      3%         │        2%                       │
 * │  SILVER    │   2%   │      6%         │        4%                       │
 * │  GOLD      │   3%   │      9%         │        6%                       │
 * │  PLATINUM  │   5%   │     15%         │       10%                       │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * =============================================================================
 */