package com.neobank.portfolio.rest;

import com.neobank.portfolio.data.MockDataService;
import com.neobank.portfolio.model.Asset;
import com.neobank.portfolio.model.Performance;
import com.neobank.portfolio.model.Portfolio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller para Investment Portfolio
 * 
 * SECCIÓN 1.1 - DE REST A GRAPHQL
 * 
 * Este controlador demuestra la implementación REST tradicional del mismo
 * dominio que exponemos en GraphQL.
 * 
 * PROBLEMAS DE REST QUE GRAPHQL RESUELVE:
 * 
 * 1. OVERFETCHING: El endpoint GET /portfolios/{id} devuelve TODO el portfolio
 *    incluyendo assets y performance, aunque el cliente solo necesite el nombre.
 * 
 * 2. UNDERFETCHING: Si el cliente necesita portfolio + performance + assets,
 *    tiene que hacer 3 llamadas HTTP separadas.
 * 
 * 3. MÚLTIPLES ENDPOINTS: Necesitamos un endpoint por cada tipo de consulta.
 * 
 * 4. VERSIONADO: Si cambia la estructura, necesitamos /v2/portfolios
 * 
 * COMPARAR CON:
 * - GraphQL permite en UNA sola query obtener exactamente lo que se necesita
 * - GraphQL usa UN solo endpoint (/graphql)
 * - GraphQL evita versionado gracias a la evolución del schema
 * 
 * @see com.neobank.portfolio.graphql.PortfolioQueryResolver
 */
@RestController
@RequestMapping("/api/rest")
public class PortfolioRestController {
    
    private final MockDataService dataService;
    
    public PortfolioRestController(MockDataService dataService) {
        this.dataService = dataService;
    }
    
    /**
     * GET /api/rest/portfolios
     * 
     * Obtiene TODOS los portfolios con TODA su información.
     * 
     * PROBLEMA: Si el cliente solo necesita los nombres, recibe también
     * todos los assets, performance, etc. (OVERFETCHING)
     */
    @GetMapping("/portfolios")
    public ResponseEntity<List<Portfolio>> getAllPortfolios() {
        return ResponseEntity.ok(dataService.getAllPortfolios());
    }
    
    /**
     * GET /api/rest/portfolios/{id}
     * 
     * Obtiene un portfolio específico con TODA su información.
     * 
     * PROBLEMA: Devuelve todo el objeto, aunque solo se necesite un campo.
     */
    @GetMapping("/portfolios/{id}")
    public ResponseEntity<Portfolio> getPortfolioById(@PathVariable String id) {
        Portfolio portfolio = dataService.getPortfolioById(id);
        if (portfolio == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(portfolio);
    }
    
    /**
     * GET /api/rest/portfolios/{id}/assets
     * 
     * Obtiene los activos de un portfolio.
     * 
     * PROBLEMA: Si ya llamaste a /portfolios/{id}, recibes los assets duplicados.
     * Si NO llamaste primero, necesitas hacer 2 llamadas (UNDERFETCHING).
     */
    @GetMapping("/portfolios/{id}/assets")
    public ResponseEntity<List<Asset>> getPortfolioAssets(@PathVariable String id) {
        List<Asset> assets = dataService.getAssetsByPortfolioId(id);
        return ResponseEntity.ok(assets);
    }
    
    /**
     * GET /api/rest/portfolios/{id}/performance
     * 
     * Obtiene el performance de un portfolio.
     * 
     * PROBLEMA: Otra llamada HTTP adicional si necesitas performance.
     */
    @GetMapping("/portfolios/{id}/performance")
    public ResponseEntity<Performance> getPortfolioPerformance(@PathVariable String id) {
        Performance performance = dataService.getPerformance(id);
        if (performance == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(performance);
    }
    
    /**
     * GET /api/rest/assets/{symbol}
     * 
     * Busca un activo por símbolo.
     * 
     * PROBLEMA: Endpoint separado para cada tipo de búsqueda.
     */
    @GetMapping("/assets/{symbol}")
    public ResponseEntity<Asset> getAssetBySymbol(@PathVariable String symbol) {
        Asset asset = dataService.getAssetBySymbol(symbol);
        if (asset == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(asset);
    }
    
    /**
     * POST /api/rest/portfolios
     * 
     * Crea un nuevo portfolio.
     * 
     * NOTA: En REST, típicamente usamos POST para crear recursos.
     * En GraphQL, usamos mutations.
     */
    @PostMapping("/portfolios")
    public ResponseEntity<Portfolio> createPortfolio(@RequestBody CreatePortfolioRequest request) {
        Portfolio portfolio = dataService.createPortfolio(
            request.getName(),
            request.getOwnerId(),
            request.getOwnerName()
        );
        return ResponseEntity.ok(portfolio);
    }
    
    // =========================================================================
    // DTOs para requests
    // =========================================================================
    
    public static class CreatePortfolioRequest {
        private String name;
        private String ownerId;
        private String ownerName;
        
        public CreatePortfolioRequest() {
        }
        
        public CreatePortfolioRequest(String name, String ownerId, String ownerName) {
            this.name = name;
            this.ownerId = ownerId;
            this.ownerName = ownerName;
        }
        
        // Getters
        public String getName() { return name; }
        public String getOwnerId() { return ownerId; }
        public String getOwnerName() { return ownerName; }
        
        // Setters
        public void setName(String name) { this.name = name; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    }
}

/*
 * =============================================================================
 * COMPARACIÓN REST vs GRAPHQL (Sección 1.1)
 * =============================================================================
 * 
 * ESCENARIO 1: El cliente solo necesita los NOMBRES de todos los portfolios
 * 
 * REST:
 * GET /api/rest/portfolios
 * - Devuelve TODO: portfolios completos con assets, performance, etc.
 * - Desperdicio de ancho de banda
 * 
 * GraphQL:
 * query {
 *   myPortfolios {
 *     name
 *   }
 * }
 * - Devuelve SOLO los nombres
 * - Mínimo de datos transferidos
 * 
 * 
 * ESCENARIO 2: El cliente necesita portfolio + assets + performance
 * 
 * REST:
 * 1. GET /api/rest/portfolios/portfolio-001
 * 2. GET /api/rest/portfolios/portfolio-001/assets
 * 3. GET /api/rest/portfolios/portfolio-001/performance
 * - 3 llamadas HTTP
 * - Posible duplicación de datos
 * 
 * GraphQL:
 * query {
 *   portfolio(id: "portfolio-001") {
 *     name
 *     totalValue
 *     assets {
 *       symbol
 *       totalValue
 *     }
 *     performance {
 *       totalReturn
 *     }
 *   }
 * }
 * - 1 sola llamada HTTP
 * - Solo los datos necesarios
 * 
 * 
 * ESCENARIO 3: El cliente necesita portfolios CON assets, PERO SIN performance
 * 
 * REST:
 * - No existe este endpoint específico
 * - Opción 1: Llamar a /portfolios y recibir TODO (incluyendo performance no deseado)
 * - Opción 2: Crear un nuevo endpoint /portfolios?include=assets
 * - Proliferación de endpoints
 * 
 * GraphQL:
 * query {
 *   myPortfolios {
 *     name
 *     assets {
 *       symbol
 *     }
 *   }
 * }
 * - El cliente decide qué campos incluir
 * - No se necesitan endpoints adicionales
 * 
 * =============================================================================
 */
