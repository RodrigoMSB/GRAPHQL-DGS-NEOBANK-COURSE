package com.neobank.cashback.dataloader;

import com.neobank.cashback.domain.User;
import com.neobank.cashback.repository.UserRepository;
import com.netflix.graphql.dgs.DgsDataLoader;
import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * SECCIÓN 3.5: Optimización con DataLoader y prevención del problema N+1
 * 
 * ========================================
 * ¿QUÉ ES EL PROBLEMA N+1?
 * ========================================
 * 
 * Imagina esta query GraphQL:
 * 
 * query {
 *   rewards {              # 1 query para obtener 100 rewards
 *     amount
 *     user {               # 100 queries individuales para obtener cada User
 *       fullName           # PROBLEMA: 1 + 100 = 101 queries!
 *     }
 *   }
 * }
 * 
 * Sin DataLoader:
 * - 1 query para obtener todas las rewards
 * - 100 queries individuales para obtener cada usuario (N+1 queries)
 * - Total: 101 queries a la base de datos
 * - Rendimiento: HORRIBLE
 * 
 * Con DataLoader:
 * - 1 query para obtener todas las rewards
 * - 1 query batch para obtener TODOS los usuarios necesarios de una vez
 * - Total: 2 queries (rendimiento óptimo)
 * 
 * ========================================
 * ¿CÓMO FUNCIONA DATALOADER?
 * ========================================
 * 
 * DataLoader actúa como un cache + batcher:
 * 
 * 1. Acumula múltiples peticiones individuales durante un tick del event loop
 * 2. Agrupa todas las keys en un solo batch
 * 3. Ejecuta UNA sola query para todas las keys
 * 4. Cachea los resultados durante la request
 * 5. Distribuye los resultados a cada petición original
 * 
 * Ejemplo visual:
 * 
 * Peticiones individuales:
 * - getUser("user-001")
 * - getUser("user-002")
 * - getUser("user-001")  // duplicado
 * - getUser("user-003")
 * 
 * DataLoader agrupa:
 * - batchLoadUsers(["user-001", "user-002", "user-003"])  // sin duplicados
 * 
 * Resultado: 1 query en lugar de 4
 * 
 * ========================================
 * ESTRATEGIAS DE CACHING
 * ========================================
 * 
 * DataLoader cachea por REQUEST (no global):
 * - Cada request GraphQL tiene su propia instancia de DataLoader
 * - El cache se limpia al finalizar la request
 * - Evita problemas de stale data
 * 
 * Diferencia con caché de cliente (Apollo Client):
 * - DataLoader: Server-side, per-request caching
 * - Apollo Client: Client-side, persistent caching
 */
@DgsDataLoader(name = "users")
public class UserDataLoader implements BatchLoader<String, User> {
    
    private final UserRepository userRepository;
    
    public UserDataLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Este método se ejecuta UNA SOLA VEZ por batch de keys.
     * 
     * DGS llama a este método automáticamente cuando:
     * 1. Múltiples resolvers piden usuarios
     * 2. Se acumula un batch durante el event loop tick
     * 3. DataLoader agrupa todas las keys únicas
     * 
     * @param userIds Lista de IDs de usuarios solicitados
     * @return CompletionStage con lista de usuarios en el MISMO ORDEN que las keys
     */
    @Override
    public CompletionStage<List<User>> load(List<String> userIds) {
        // Log para demostrar el batching en acción
        System.out.println("🔥 DataLoader batch loading " + userIds.size() + " users: " + userIds);
        
        // Obtener todos los usuarios en UNA SOLA operación
        List<User> allUsers = userRepository.findAll();
        
        // Crear mapa para lookup eficiente
        Map<String, User> userMap = allUsers.stream()
            .filter(user -> userIds.contains(user.getId()))
            .collect(Collectors.toMap(User::getId, user -> user));
        
        // CRÍTICO: Devolver usuarios en el MISMO ORDEN que las keys
        // Si key no existe, devolver null
        List<User> result = userIds.stream()
            .map(userMap::get)
            .collect(Collectors.toList());
        
        return CompletableFuture.completedFuture(result);
    }
}

/**
 * NOTAS IMPORTANTES:
 * 
 * 1. EL ORDEN IMPORTA:
 *    DataLoader espera que devuelvas los resultados en el mismo orden que las keys.
 *    Si keys = ["user-001", "user-002"], result DEBE ser [User1, User2] en ESE orden.
 * 
 * 2. MANEJO DE NULLS:
 *    Si un ID no existe, devuelve null en esa posición.
 *    Ejemplo: keys = ["user-001", "user-999"], result = [User1, null]
 * 
 * 3. DUPLICADOS:
 *    DataLoader elimina duplicados automáticamente antes de llamar a load().
 *    Si pides ["user-001", "user-001"], load() recibe ["user-001"] (una sola vez).
 * 
 * 4. SCOPE:
 *    El cache de DataLoader es PER-REQUEST, no global.
 *    Cada GraphQL request crea nuevas instancias de DataLoader.
 * 
 * 5. ASYNC:
 *    DataLoader usa CompletableFuture para operaciones asíncronas.
 *    Esto permite paralelizar múltiples DataLoaders.
 */
