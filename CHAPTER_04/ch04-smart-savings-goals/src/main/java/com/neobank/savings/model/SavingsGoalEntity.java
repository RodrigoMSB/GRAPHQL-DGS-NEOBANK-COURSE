package com.neobank.savings.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "savings_goals")
public class SavingsGoalEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status = GoalStatus.ACTIVE;
    
    // =========================================================================
    // ENUMS
    // =========================================================================
    
    public enum GoalStatus {
        ACTIVE, PAUSED, COMPLETED, CANCELLED
    }
    
    public enum GoalCategory {
        EMERGENCY_FUND, VACATION, HOME_PURCHASE, EDUCATION, RETIREMENT, INVESTMENT, OTHER
    }
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public SavingsGoalEntity() {
    }
    
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
