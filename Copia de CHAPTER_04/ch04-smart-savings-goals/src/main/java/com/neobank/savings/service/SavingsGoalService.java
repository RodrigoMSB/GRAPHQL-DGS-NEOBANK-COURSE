package com.neobank.savings.service;

import com.neobank.savings.model.SavingsGoalEntity;
import com.neobank.savings.model.SavingsGoalEntity.GoalStatus;
import com.neobank.savings.repository.SavingsGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de lógica de negocio para Metas de Ahorro.
 * 
 * 🎓 SECCIÓN 4.3: TRANSACCIONES EN MUTATIONS
 * 
 * Este servicio demuestra:
 * - Uso de @Transactional para operaciones atómicas
 * - Validaciones de negocio
 * - Separación de responsabilidades (Resolver → Service → Repository)
 * 
 * 💡 ¿POR QUÉ @TRANSACTIONAL?
 * 
 * Sin transacción:
 * ```
 * 1. Guardar meta → OK
 * 2. Actualizar balance usuario → FALLA
 * Resultado: Meta guardada pero balance inconsistente 😱
 * ```
 * 
 * Con transacción:
 * ```
 * BEGIN TRANSACTION
 * 1. Guardar meta → OK
 * 2. Actualizar balance usuario → FALLA
 * ROLLBACK (todo se revierte)
 * Resultado: Datos consistentes ✅
 * ```
 * 
 * 🎓 PROPAGACIÓN DE TRANSACCIONES:
 * - REQUIRED (default): Usa transacción existente o crea una nueva
 * - REQUIRES_NEW: Siempre crea una nueva (suspende la actual)
 * - SUPPORTS: Usa si existe, si no, ejecuta sin transacción
 * - NOT_SUPPORTED: Ejecuta sin transacción (suspende si existe)
 * 
 * @see SavingsGoalRepository (acceso a datos)
 * @see SavingsGoalResolver (capa GraphQL)
 */
@Service
public class SavingsGoalService {
    
    private final SavingsGoalRepository repository;
    
    public SavingsGoalService(SavingsGoalRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Obtiene una meta por su ID.
     * 
     * @param goalId ID de la meta
     * @return La meta encontrada
     * @throws RuntimeException si no existe
     */
    public SavingsGoalEntity getGoalById(Long goalId) {
        return repository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found: " + goalId));
    }
    
    /**
     * Obtiene todas las metas de un usuario.
     * 
     * @param userId ID del usuario
     * @return Lista de todas las metas (cualquier estado)
     */
    public List<SavingsGoalEntity> getGoalsByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
    
    /**
     * Obtiene solo las metas activas de un usuario.
     * 
     * @param userId ID del usuario
     * @return Lista de metas con status = ACTIVE
     */
    public List<SavingsGoalEntity> getActiveGoalsByUserId(Long userId) {
        return repository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE);
    }
    
    /**
     * Crea una nueva meta de ahorro.
     * 
     * 🎓 SECCIÓN 4.3: TRANSACCIONES
     * 
     * @Transactional asegura que:
     * - Si algo falla, se hace ROLLBACK
     * - Los cambios se persisten al final (COMMIT)
     * 
     * @param goal Meta a crear
     * @return Meta creada con ID generado
     */
    @Transactional
    public SavingsGoalEntity createGoal(SavingsGoalEntity goal) {
        // Validaciones de negocio
        validateGoal(goal);
        
        // Establecer valores por defecto
        goal.setStatus(GoalStatus.ACTIVE);
        if (goal.getCurrentAmount() == null) {
            goal.setCurrentAmount(BigDecimal.ZERO);
        }
        
        return repository.save(goal);
    }
    
    /**
     * Deposita dinero en una meta de ahorro.
     * 
     * 🎓 TRANSACCIÓN IMPORTANTE:
     * Si el depósito completa la meta, también actualiza el status.
     * Ambas operaciones deben ser atómicas.
     * 
     * @param goalId ID de la meta
     * @param amount Monto a depositar
     * @return Meta actualizada
     * @throws RuntimeException si la meta no existe o está pausada/cancelada
     */
    @Transactional
    public SavingsGoalEntity deposit(Long goalId, BigDecimal amount) {
        // Validar monto
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        // Obtener meta
        SavingsGoalEntity goal = getGoalById(goalId);
        
        // Validar que esté activa
        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new IllegalStateException("Cannot deposit to a " + goal.getStatus() + " goal");
        }
        
        // Actualizar monto
        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
        
        // Verificar si se completó
        if (goal.isCompleted()) {
            goal.setStatus(GoalStatus.COMPLETED);
        }
        
        return repository.save(goal);
    }
    
    /**
     * Retira dinero de una meta de ahorro.
     * 
     * @param goalId ID de la meta
     * @param amount Monto a retirar
     * @return Meta actualizada
     * @throws RuntimeException si no hay suficiente saldo
     */
    @Transactional
    public SavingsGoalEntity withdraw(Long goalId, BigDecimal amount) {
        // Validar monto
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        // Obtener meta
        SavingsGoalEntity goal = getGoalById(goalId);
        
        // Validar que esté activa
        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new IllegalStateException("Cannot withdraw from a " + goal.getStatus() + " goal");
        }
        
        // Validar saldo suficiente
        if (goal.getCurrentAmount().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance. Available: " + goal.getCurrentAmount());
        }
        
        // Actualizar monto
        goal.setCurrentAmount(goal.getCurrentAmount().subtract(amount));
        
        return repository.save(goal);
    }
    
    /**
     * Cambia el estado de una meta.
     * 
     * @param goalId ID de la meta
     * @param newStatus Nuevo estado
     * @return Meta actualizada
     */
    @Transactional
    public SavingsGoalEntity updateStatus(Long goalId, GoalStatus newStatus) {
        SavingsGoalEntity goal = getGoalById(goalId);
        goal.setStatus(newStatus);
        return repository.save(goal);
    }
    
    /**
     * Validaciones de negocio para una meta.
     */
    private void validateGoal(SavingsGoalEntity goal) {
        if (goal.getName() == null || goal.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Goal name is required");
        }
        if (goal.getTargetAmount() == null || goal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Target amount must be positive");
        }
        if (goal.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (goal.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }
    }
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO - SECCIÓN 4.3
 * =============================================================================
 * 
 * 📊 OPERACIONES TRANSACCIONALES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  Método           │  Operaciones                                       │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │  createGoal       │  Validar + Guardar                                 │
 * │  deposit          │  Validar + Actualizar monto + Actualizar status    │
 * │  withdraw         │  Validar saldo + Actualizar monto                  │
 * │  updateStatus     │  Cambiar estado                                    │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 🎯 REGLA DE ORO:
 * Si una operación modifica múltiples cosas que deben ser consistentes,
 * envuélvela en @Transactional.
 * 
 * =============================================================================
 */