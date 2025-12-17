package com.neobank.savings.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad JPA que representa una Meta de Ahorro.
 * 
 * 🎓 SECCIÓN 4.2: ENTIDADES, REPOSITORIOS Y MAPEO
 * 
 * Esta clase demuestra:
 * - Mapeo JPA a tabla PostgreSQL
 * - Uso de enums como columnas
 * - Valores por defecto en campos
 * - Builder pattern para construcción fluida
 * 
 * 📦 MAPEO A BASE DE DATOS:
 * ```
 * Clase Java                    Tabla PostgreSQL
 * ──────────────────────────────────────────────────
 * SavingsGoalEntity      →      savings_goals
 * goalId (Long)          →      goal_id (BIGSERIAL PK)
 * userId (Long)          →      user_id (BIGINT NOT NULL)
 * name (String)          →      name (VARCHAR NOT NULL)
 * description (String)   →      description (VARCHAR)
 * targetAmount (BigDec)  →      target_amount (NUMERIC(15,2))
 * currentAmount (BigDec) →      current_amount (NUMERIC(15,2))
 * category (Enum)        →      category (VARCHAR)
 * status (Enum)          →      status (VARCHAR)
 * ```
 * 
 * 💡 ¿POR QUÉ BIGDECIMAL PARA MONTOS?
 * Double tiene problemas de precisión con decimales:
 * ```java
 * double d = 0.1 + 0.2;  // = 0.30000000000000004 ❌
 * BigDecimal b = new BigDecimal("0.1").add(new BigDecimal("0.2"));  // = 0.3 ✅
 * ```
 * Para dinero, SIEMPRE usa BigDecimal.
 * 
 * 🎓 ANOTACIONES JPA:
 * - @Entity: Marca la clase como entidad JPA
 * - @Table: Especifica nombre de tabla
 * - @Id: Campo de clave primaria
 * - @GeneratedValue: Auto-generación de ID
 * - @Column: Configuración de columna
 * - @Enumerated: Cómo persistir enums
 * 
 * @see SavingsGoalRepository (acceso a datos)
 * @see SavingsGoalService (lógica de negocio)
 */
@Entity
@Table(name = "savings_goals")
public class SavingsGoalEntity {
    
    /**
     * Identificador único de la meta.
     * Auto-generado por PostgreSQL (SERIAL/IDENTITY).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;
    
    /**
     * ID del usuario propietario de la meta.
     * En producción, sería FK a tabla de usuarios.
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * Nombre descriptivo de la meta.
     * Ej: "Vacaciones a Japón", "Fondo de emergencia"
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * Descripción detallada opcional.
     */
    private String description;
    
    /**
     * Monto objetivo a alcanzar.
     * Ej: $5,000.00 para vacaciones
     * 
     * 💡 precision=15, scale=2 significa:
     * - Hasta 15 dígitos totales
     * - 2 dígitos después del punto decimal
     * - Máximo: 9,999,999,999,999.99
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;
    
    /**
     * Monto actualmente ahorrado.
     * Se actualiza con cada depósito.
     * Default: 0.00
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;
    
    /**
     * Categoría de la meta.
     * 
     * @see GoalCategory
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalCategory category;
    
    /**
     * Estado actual de la meta.
     * Default: ACTIVE
     * 
     * @see GoalStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status = GoalStatus.ACTIVE;
    
    // =========================================================================
    // ENUMS
    // =========================================================================
    
    /**
     * Estados posibles de una meta de ahorro.
     * 
     * Ciclo de vida típico:
     * ACTIVE → COMPLETED (al alcanzar targetAmount)
     * ACTIVE → PAUSED (usuario pausa temporalmente)
     * ACTIVE → CANCELLED (usuario cancela)
     */
    public enum GoalStatus {
        /** Meta activa, aceptando depósitos */
        ACTIVE,
        /** Meta pausada temporalmente */
        PAUSED,
        /** Meta completada (currentAmount >= targetAmount) */
        COMPLETED,
        /** Meta cancelada por el usuario */
        CANCELLED
    }
    
    /**
     * Categorías de metas de ahorro.
     * 
     * Permiten organizar y filtrar las metas,
     * y potencialmente aplicar reglas diferentes por categoría.
     */
    public enum GoalCategory {
        /** Fondo de emergencia (3-6 meses de gastos) */
        EMERGENCY_FUND,
        /** Vacaciones y viajes */
        VACATION,
        /** Enganche para casa */
        HOME_PURCHASE,
        /** Educación (universidad, cursos) */
        EDUCATION,
        /** Ahorro para retiro */
        RETIREMENT,
        /** Inversiones */
        INVESTMENT,
        /** Otras metas */
        OTHER
    }
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    /**
     * Constructor vacío requerido por JPA.
     */
    public SavingsGoalEntity() {
    }
    
    /**
     * Constructor completo.
     */
    public SavingsGoalEntity(Long goalId, Long userId, String name, String description,
                             BigDecimal targetAmount, BigDecimal currentAmount,
                             GoalCategory category, GoalStatus status) {
        this.goalId = goalId;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount != null ? currentAmount : BigDecimal.ZERO;
        this.category = category;
        this.status = status != null ? status : GoalStatus.ACTIVE;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public Long getGoalId() {
        return goalId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public BigDecimal getTargetAmount() {
        return targetAmount;
    }
    
    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }
    
    public GoalCategory getCategory() {
        return category;
    }
    
    public GoalStatus getStatus() {
        return status;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }
    
    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }
    
    public void setCategory(GoalCategory category) {
        this.category = category;
    }
    
    public void setStatus(GoalStatus status) {
        this.status = status;
    }
    
    // =========================================================================
    // BUSINESS METHODS
    // =========================================================================
    
    /**
     * Calcula el porcentaje de progreso hacia la meta.
     * 
     * @return Porcentaje (0.0 a 100.0+)
     */
    public double getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return currentAmount
                .divide(targetAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
    
    /**
     * Calcula el monto restante para alcanzar la meta.
     * 
     * @return Monto faltante (puede ser negativo si se excedió)
     */
    public BigDecimal getRemainingAmount() {
        return targetAmount.subtract(currentAmount);
    }
    
    /**
     * Verifica si la meta está completa.
     * 
     * @return true si currentAmount >= targetAmount
     */
    public boolean isCompleted() {
        return currentAmount.compareTo(targetAmount) >= 0;
    }
    
    // =========================================================================
    // EQUALS, HASHCODE, TOSTRING
    // =========================================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavingsGoalEntity that = (SavingsGoalEntity) o;
        return Objects.equals(goalId, that.goalId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(goalId);
    }
    
    @Override
    public String toString() {
        return "SavingsGoalEntity{" +
                "goalId=" + goalId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", targetAmount=" + targetAmount +
                ", currentAmount=" + currentAmount +
                ", progress=" + getProgressPercentage() + "%" +
                ", category=" + category +
                ", status=" + status +
                '}';
    }
    
    // =========================================================================
    // BUILDER
    // =========================================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long goalId;
        private Long userId;
        private String name;
        private String description;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount = BigDecimal.ZERO;
        private GoalCategory category;
        private GoalStatus status = GoalStatus.ACTIVE;
        
        public Builder goalId(Long goalId) {
            this.goalId = goalId;
            return this;
        }
        
        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder targetAmount(BigDecimal targetAmount) {
            this.targetAmount = targetAmount;
            return this;
        }
        
        public Builder currentAmount(BigDecimal currentAmount) {
            this.currentAmount = currentAmount != null ? currentAmount : BigDecimal.ZERO;
            return this;
        }
        
        public Builder category(GoalCategory category) {
            this.category = category;
            return this;
        }
        
        public Builder status(GoalStatus status) {
            this.status = status != null ? status : GoalStatus.ACTIVE;
            return this;
        }
        
        public SavingsGoalEntity build() {
            return new SavingsGoalEntity(goalId, userId, name, description,
                    targetAmount, currentAmount, category, status);
        }
    }
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO - SECCIÓN 4.2
 * =============================================================================
 * 
 * 📊 MAPEO JPA:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  @Entity                 │  Marca como entidad JPA                     │
 * │  @Table(name="...")      │  Nombre de la tabla                         │
 * │  @Id                     │  Clave primaria                             │
 * │  @GeneratedValue         │  Auto-generación (IDENTITY = SERIAL)        │
 * │  @Column                 │  Configuración de columna                   │
 * │  @Enumerated             │  Cómo guardar enums (STRING o ORDINAL)      │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 💡 TIPS:
 * - Siempre usa BigDecimal para dinero
 * - Usa @Enumerated(EnumType.STRING) para legibilidad en BD
 * - Incluye constructor vacío para JPA
 * - El Builder facilita crear entidades en tests y código
 * 
 * =============================================================================
 */