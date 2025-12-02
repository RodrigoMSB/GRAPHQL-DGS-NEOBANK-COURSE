package com.neobank.savings.resolver;

import com.netflix.graphql.dgs.*;
import com.neobank.savings.model.SavingsGoalEntity;
import com.neobank.savings.service.SavingsGoalService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@DgsComponent
@RequiredArgsConstructor
public class SavingsGoalResolver {
    
    private final SavingsGoalService service;
    
    @DgsQuery
    public Map<String, Object> savingsGoal(@InputArgument String id) {
        SavingsGoalEntity entity = service.getGoalById(Long.parseLong(id));
        return toGraphQL(entity);
    }
    
    @DgsQuery
    public List<Map<String, Object>> savingsGoals(@InputArgument String userId) {
        return service.getGoalsByUserId(Long.parseLong(userId)).stream()
            .map(this::toGraphQL)
            .collect(Collectors.toList());
    }
    
    @DgsQuery
    public List<Map<String, Object>> activeSavingsGoals(@InputArgument String userId) {
        return service.getActiveGoalsByUserId(Long.parseLong(userId)).stream()
            .map(this::toGraphQL)
            .collect(Collectors.toList());
    }
    
    @DgsMutation
    public Map<String, Object> createSavingsGoal(@InputArgument Map<String, Object> input) {
        try {
            SavingsGoalEntity entity = SavingsGoalEntity.builder()
                .userId(Long.parseLong(input.get("userId").toString()))
                .name(input.get("name").toString())
                .description(input.get("description") != null ? input.get("description").toString() : null)
                .targetAmount(new BigDecimal(input.get("targetAmount").toString()))
                .category(SavingsGoalEntity.GoalCategory.valueOf(input.get("category").toString()))
                .build();
            
            SavingsGoalEntity saved = service.createGoal(entity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Goal created successfully");
            response.put("goal", toGraphQL(saved));
            return response;
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("goal", null);
            return response;
        }
    }
    
    private Map<String, Object> toGraphQL(SavingsGoalEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getGoalId().toString());
        map.put("userId", entity.getUserId().toString());
        map.put("name", entity.getName());
        map.put("description", entity.getDescription());
        map.put("targetAmount", entity.getTargetAmount());
        map.put("currentAmount", entity.getCurrentAmount());
        map.put("category", entity.getCategory().name());
        map.put("status", entity.getStatus().name());
        map.put("progressPercentage", calculateProgress(entity));
        return map;
    }
    
    private double calculateProgress(SavingsGoalEntity entity) {
        if (entity.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return entity.getCurrentAmount()
            .divide(entity.getTargetAmount(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }
}
