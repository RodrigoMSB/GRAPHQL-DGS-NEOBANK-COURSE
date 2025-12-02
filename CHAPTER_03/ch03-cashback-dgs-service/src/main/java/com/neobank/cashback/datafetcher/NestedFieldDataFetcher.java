package com.neobank.cashback.datafetcher;

import com.neobank.cashback.domain.Reward;
import com.neobank.cashback.domain.User;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import org.dataloader.DataLoader;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * SECCIÓN 3.3 + 3.5: Resolvers para campos anidados CON DataLoader
 * 
 * En GraphQL, cada campo puede tener su propio resolver.
 * Esto permite construir grafos de datos complejos sin overfetching.
 * 
 * Ejemplo de query que usa estos resolvers:
 * 
 * query {
 *   user(id: "user-001") {
 *     fullName
 *     rewards {           # <- Este campo se resuelve con userRewards()
 *       amount
 *       category
 *       user {            # <- Este campo se resuelve con rewardUser()
 *         fullName
 *       }
 *     }
 *   }
 * }
 * 
 * VERSIÓN OPTIMIZADA: Usa DataLoader para evitar el problema N+1
 * 
 * ========================================
 * COMPARACIÓN: SIN vs CON DataLoader
 * ========================================
 * 
 * SIN DataLoader (versión anterior):
 * 
 * query {
 *   usersByTier(tier: GOLD) {    # Query 1: obtener 10 usuarios
 *     fullName
 *     rewards {                   # Queries 2-11: 10 queries individuales (N+1!)
 *       amount
 *       user {                    # Queries 12-111: 100 queries individuales (N+1!)
 *         fullName
 *       }
 *     }
 *   }
 * }
 * Total: 111 queries 😱
 * 
 * CON DataLoader (esta versión):
 * 
 * query {
 *   usersByTier(tier: GOLD) {    # Query 1: obtener 10 usuarios
 *     fullName
 *     rewards {                   # Query 2: 1 batch para todas las rewards
 *       amount
 *       user {                    # Query 3: 1 batch para todos los usuarios
 *         fullName
 *       }
 *     }
 *   }
 * }
 * Total: 3 queries 🚀
 * 
 * Mejora: 37x menos queries!
 */
@DgsComponent
public class NestedFieldDataFetcher {
    
    /**
     * Resuelve el campo: User.rewards: [Reward!]!
     * 
     * VERSIÓN OPTIMIZADA con DataLoader.
     * 
     * En lugar de hacer:
     *   rewardRepository.findByUserId(user.getId())  // N queries
     * 
     * Usamos:
     *   dataLoader.load(user.getId())  // 1 query batch
     */
    @DgsData(parentType = "User", field = "rewards")
    public CompletableFuture<List<Reward>> userRewards(DgsDataFetchingEnvironment dfe) {
        User user = dfe.getSource();
        
        // Obtener el DataLoader de rewards del contexto
        DataLoader<String, List<Reward>> dataLoader = dfe.getDataLoader("rewards");
        
        // load() NO ejecuta inmediatamente - acumula la petición
        // DGS ejecutará el batch automáticamente cuando sea necesario
        return dataLoader.load(user.getId());
    }
    
    /**
     * Resuelve el campo: Reward.user: User!
     * 
     * VERSIÓN OPTIMIZADA con DataLoader.
     * 
     * En lugar de hacer:
     *   userRepository.findById(reward.getUserId())  // N queries
     * 
     * Usamos:
     *   dataLoader.load(reward.getUserId())  // 1 query batch
     */
    @DgsData(parentType = "Reward", field = "user")
    public CompletableFuture<User> rewardUser(DgsDataFetchingEnvironment dfe) {
        Reward reward = dfe.getSource();
        
        // Obtener el DataLoader de users del contexto
        DataLoader<String, User> dataLoader = dfe.getDataLoader("users");
        
        // load() acumula la petición para batch loading
        return dataLoader.load(reward.getUserId());
    }
}

/**
 * EXPLICACIÓN DETALLADA DEL FLUJO:
 * 
 * 1. Cliente envía query pidiendo 10 usuarios con sus rewards
 * 
 * 2. DGS ejecuta QueryDataFetcher.usersByTier() -> retorna 10 Users
 * 
 * 3. Para cada User, DGS ve que se pide el campo "rewards"
 *    - Llama a userRewards() 10 veces
 *    - Cada llamada hace dataLoader.load(userId)
 *    - DataLoader NO ejecuta aún - solo ACUMULA las 10 peticiones
 * 
 * 4. Al finalizar el event loop tick, DataLoader agrupa las 10 keys:
 *    - ["user-001", "user-002", ..., "user-010"]
 *    - Llama a RewardsDataLoader.load() UNA SOLA VEZ
 *    - Retorna Map<userId, List<Reward>>
 * 
 * 5. DataLoader distribuye los resultados a cada userRewards() original
 * 
 * 6. Si se pide reward.user, el proceso se repite con UserDataLoader
 * 
 * Resultado: De 111 queries a 3 queries. ¡Magia! 🎩✨
 */
