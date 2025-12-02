package com.neobank.portfolio.graphql;

import com.neobank.portfolio.data.MockDataService;
import com.neobank.portfolio.model.Asset;
import com.neobank.portfolio.model.Performance;
import com.neobank.portfolio.model.Portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Query Resolver
 * 
 * SECCIÓN 1.2 - COMPONENTES Y LENGUAJE BASE
 * 
 * Este resolver implementa las QUERIES definidas en el schema GraphQL.
 * 
 * Los resolvers son el equivalente a los Controllers en REST, pero con
 * una diferencia fundamental: GraphQL resuelve campos de manera GRANULAR.
 * 
 * CONCEPTOS CLAVE:
 * 
 * 1. @QueryMapping: Mapea métodos Java a queries del schema
 * 2. @Argument: Mapea parámetros del método a argumentos de la query
 * 3. Return type: Debe coincidir con el tipo declarado en el schema
 * 
 * SECCIÓN 1.3 - CONSULTAS ANIDADAS Y VARIABLES
 * 
 * GraphQL automáticamente resuelve relaciones anidadas:
 * - Si un Portfolio tiene assets, GraphQL los incluirá si se solicitan
 * - El cliente decide qué profundidad de anidación necesita
 * 
 * EJEMPLO DE CONSULTA ANIDADA:
 * 
 * query {
 *   portfolio(id: "portfolio-001") {
 *     name
 *     totalValue
 *     assets {              ← Anidación nivel 1
 *       symbol
 *       totalValue
 *     }
 *     performance {         ← Anidación nivel 1
 *       totalReturn
 *       bestPerformer {     ← Anidación nivel 2
 *         symbol
 *       }
 *     }
 *   }
 * }
 * 
 * EJEMPLO CON VARIABLES (Sección 1.3):
 * 
 * query GetPortfolio($portfolioId: ID!) {
 *   portfolio(id: $portfolioId) {
 *     name
 *     totalValue
 *   }
 * }
 * 
 * Variables JSON:
 * {
 *   "portfolioId": "portfolio-001"
 * }
 * 
 * VENTAJAS:
 * - Reusabilidad de la query
 * - Seguridad (evita inyección)
 * - Validación automática de tipos
 * 
 * @see schema.graphqls
 */
@Controller
@RequiredArgsConstructor
public class PortfolioQueryResolver {
    
    private final MockDataService dataService;
    
    /**
     * Query: myPortfolios
     * 
     * Retorna todos los portfolios del usuario autenticado.
     * 
     * SECCIÓN 1.5: En un caso real, obtendríamos el userId del contexto
     * de autenticación (JWT token). Para este ejemplo, usamos un user fijo.
     * 
     * GraphQL Query:
     * query {
     *   myPortfolios {
     *     id
     *     name
     *     totalValue
     *   }
     * }
     * 
     * NOTA: El cliente decide qué campos retornar. Si solo quiere nombres:
     * 
     * query {
     *   myPortfolios {
     *     name
     *   }
     * }
     * 
     * Esto evita OVERFETCHING (Sección 1.1)
     */
    @QueryMapping
    public List<Portfolio> myPortfolios() {
        // TODO (Sección 1.5): Obtener userId del SecurityContext
        String userId = "user-001"; // Hardcoded para este ejemplo
        
        return dataService.getPortfoliosByOwnerId(userId);
    }
    
    /**
     * Query: portfolio(id: ID!)
     * 
     * Retorna un portfolio específico por ID.
     * 
     * SECCIÓN 1.3: Demuestra el uso de VARIABLES
     * 
     * Ejemplo con variable:
     * 
     * query GetPortfolio($id: ID!) {
     *   portfolio(id: $id) {
     *     name
     *     assets {
     *       symbol
     *       totalValue
     *     }
     *   }
     * }
     * 
     * Variables:
     * { "id": "portfolio-001" }
     * 
     * SECCIÓN 1.5: El tipo ID! (con !) indica que es obligatorio (non-nullable)
     * GraphQL valida automáticamente que se proporcione este argumento.
     */
    @QueryMapping
    public Portfolio portfolio(@Argument String id) {
        return dataService.getPortfolioById(id);
    }
    
    /**
     * Query: searchAsset(symbol: String!)
     * 
     * Busca un activo por su símbolo.
     * 
     * SECCIÓN 1.3: Ejemplo simple de query con variable
     * 
     * query SearchAsset($symbol: String!) {
     *   searchAsset(symbol: $symbol) {
     *     name
     *     currentPrice
     *     assetType
     *   }
     * }
     * 
     * Variables:
     * { "symbol": "AAPL" }
     */
    @QueryMapping
    public Asset searchAsset(@Argument String symbol) {
        return dataService.getAssetBySymbol(symbol);
    }
    
    /**
     * Query: portfolioPerformance(portfolioId: ID!)
     * 
     * Obtiene el rendimiento de un portfolio.
     * 
     * SECCIÓN 1.3: También se puede acceder mediante consulta anidada:
     * 
     * Opción 1 (Query directa):
     * query {
     *   portfolioPerformance(portfolioId: "portfolio-001") {
     *     totalReturn
     *     bestPerformer {
     *       symbol
     *     }
     *   }
     * }
     * 
     * Opción 2 (Anidada - MÁS COMÚN):
     * query {
     *   portfolio(id: "portfolio-001") {
     *     name
     *     performance {
     *       totalReturn
     *       bestPerformer {
     *         symbol
     *       }
     *     }
     *   }
     * }
     * 
     * La opción 2 es más eficiente: UNA query en vez de DOS.
     */
    @QueryMapping
    public Performance portfolioPerformance(@Argument String portfolioId) {
        return dataService.getPerformance(portfolioId);
    }
    
    /**
     * TODO (Sección 1.4): Implementar query de assets con filtros, orden y paginación
     * 
     * Este método debe implementar:
     * - Filtrado por AssetFilterInput
     * - Ordenamiento por AssetSortInput
     * - Paginación cursor-based con PaginationInput
     * 
     * Ver: PortfolioAssetsResolver.java (se creará en el siguiente paso)
     */
}

/*
 * =============================================================================
 * COMPARACIÓN CON REST (Continuación de Sección 1.1)
 * =============================================================================
 * 
 * FLEXIBILIDAD DE CONSULTAS:
 * 
 * Con estos 4 métodos de resolver, el cliente puede hacer INFINITAS
 * combinaciones de consultas, por ejemplo:
 * 
 * 1. Solo nombres de portfolios:
 * query { myPortfolios { name } }
 * 
 * 2. Portfolios con assets:
 * query { myPortfolios { name assets { symbol } } }
 * 
 * 3. Portfolios con performance:
 * query { myPortfolios { name performance { totalReturn } } }
 * 
 * 4. Portfolio específico con todo anidado:
 * query {
 *   portfolio(id: "portfolio-001") {
 *     name
 *     assets { symbol currentPrice }
 *     performance {
 *       totalReturn
 *       bestPerformer { symbol }
 *       worstPerformer { symbol }
 *     }
 *   }
 * }
 * 
 * EN REST, necesitarías:
 * - Múltiples endpoints
 * - Múltiples llamadas HTTP
 * - O crear endpoints específicos para cada caso de uso
 * 
 * =============================================================================
 */
