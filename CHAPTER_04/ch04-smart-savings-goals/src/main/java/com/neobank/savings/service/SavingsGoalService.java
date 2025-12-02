package com.neobank.savings.service;

import com.neobank.savings.model.SavingsGoalEntity;
import com.neobank.savings.model.SavingsGoalEntity.GoalStatus;
import com.neobank.savings.repository.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingsGoalService {
    
    private final SavingsGoalRepository repository;
    
    public SavingsGoalEntity getGoalById(Long goalId) {
        return repository.findById(goalId)
            .orElseThrow(() -> new RuntimeException("Goal not found: " + goalId));
    }
    
    public List<SavingsGoalEntity> getGoalsByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
    
    public List<SavingsGoalEntity> getActiveGoalsByUserId(Long userId) {
        return repository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE);
    }
    
    @Transactional
    public SavingsGoalEntity createGoal(SavingsGoalEntity goal) {
        goal.setStatus(GoalStatus.ACTIVE);
        return repository.save(goal);
    }
}
