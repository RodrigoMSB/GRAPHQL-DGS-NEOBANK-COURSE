package com.neobank.savings.repository;

import com.neobank.savings.model.SavingsGoalEntity;
import com.neobank.savings.model.SavingsGoalEntity.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoalEntity, Long> {
    
    List<SavingsGoalEntity> findByUserId(Long userId);
    
    List<SavingsGoalEntity> findByUserIdAndStatus(Long userId, GoalStatus status);
}
