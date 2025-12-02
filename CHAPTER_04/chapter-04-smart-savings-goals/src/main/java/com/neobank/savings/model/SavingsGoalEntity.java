package com.neobank.savings.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "savings_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private BigDecimal currentAmount = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;
    
    public enum GoalStatus {
        ACTIVE, PAUSED, COMPLETED, CANCELLED
    }
    
    public enum GoalCategory {
        EMERGENCY_FUND, VACATION, HOME_PURCHASE, EDUCATION, RETIREMENT, INVESTMENT, OTHER
    }
}
