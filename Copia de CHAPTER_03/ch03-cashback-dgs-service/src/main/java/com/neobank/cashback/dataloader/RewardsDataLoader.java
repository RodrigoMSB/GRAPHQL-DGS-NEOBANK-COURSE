package com.neobank.cashback.dataloader;

import com.neobank.cashback.domain.Reward;
import com.neobank.cashback.repository.RewardRepository;
import com.netflix.graphql.dgs.DgsDataLoader;
import org.dataloader.BatchLoader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * DataLoader para cargar rewards de múltiples usuarios en batch.
 * 
 * Este DataLoader resuelve el N+1 problem cuando se piden rewards de múltiples usuarios:
 * 
 * query {
 *   usersByTier(tier: GOLD) {    # 10 usuarios
 *     fullName
 *     rewards {                   # Sin DataLoader: 10 queries
 *       amount                    # Con DataLoader: 1 query
 *     }
 *   }
 * }
 * 
 * IMPORTANTE: Este DataLoader retorna una LISTA de rewards por cada userId,
 * a diferencia de UserDataLoader que retorna UN solo User por userId.
 */
@DgsDataLoader(name = "rewards")
public class RewardsDataLoader implements BatchLoader<String, List<Reward>> {
    
    private final RewardRepository rewardRepository;
    
    public RewardsDataLoader(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }
    
    /**
     * Carga rewards para múltiples usuarios en un solo batch.
     * 
     * @param userIds Lista de IDs de usuarios
     * @return CompletionStage con lista de listas de rewards (List<List<Reward>>)
     */
    @Override
    public CompletionStage<List<List<Reward>>> load(List<String> userIds) {
        System.out.println("🔥 DataLoader batch loading rewards for " + userIds.size() + " users: " + userIds);
        
        // Obtener TODAS las rewards de una vez
        List<Reward> allRewards = rewardRepository.findAll();
        
        // Agrupar por userId
        Map<String, List<Reward>> rewardsByUser = allRewards.stream()
            .filter(reward -> userIds.contains(reward.getUserId()))
            .collect(Collectors.groupingBy(Reward::getUserId));
        
        // CRÍTICO: Devolver en el mismo orden que userIds
        // Si un usuario no tiene rewards, devolver lista vacía (NO null)
        List<List<Reward>> result = userIds.stream()
            .map(userId -> rewardsByUser.getOrDefault(userId, List.of()))
            .collect(Collectors.toList());
        
        return CompletableFuture.completedFuture(result);
    }
}

/**
 * DIFERENCIA CLAVE: BatchLoader<K, V> vs BatchLoader<K, List<V>>
 * 
 * UserDataLoader:   BatchLoader<String, User>           -> retorna UN User por key
 * RewardsDataLoader: BatchLoader<String, List<Reward>>  -> retorna LISTA de Rewards por key
 * 
 * Para relaciones 1:N (un usuario tiene MUCHAS rewards), usa List<V>.
 * Para relaciones N:1 (una reward tiene UN usuario), usa V.
 */
