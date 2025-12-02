package com.neobank.portfolio.graphql;

import com.neobank.portfolio.data.MockDataService;
import com.neobank.portfolio.model.Asset;
import com.neobank.portfolio.model.AssetType;
import com.neobank.portfolio.model.Portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * GraphQL Mutation Resolver
 * 
 * SECCIÓN 1.2 - MUTATIONS
 * 
 * Las mutations en GraphQL son operaciones que MODIFICAN datos.
 * Son el equivalente a POST, PUT, DELETE en REST.
 * 
 * DIFERENCIAS CON REST:
 * 
 * 1. En REST: POST /api/portfolios
 *    En GraphQL: mutation { createPortfolio(input: {...}) { ... } }
 * 
 * 2. En GraphQL, la mutation retorna el objeto creado/modificado,
 *    permitiendo al cliente especificar qué campos necesita en la respuesta.
 * 
 * EJEMPLO DE MUTATION CON RESPUESTA PERSONALIZADA:
 * 
 * mutation {
 *   createPortfolio(input: { name: "My Portfolio" }) {
 *     success
 *     message
 *     portfolio {
 *       id        ← El cliente decide qué campos retornar
 *       name
 *       totalValue
 *     }
 *   }
 * }
 * 
 * @see schema.graphqls (types CreatePortfolioInput, AddAssetInput, etc.)
 */
@Controller
@RequiredArgsConstructor
public class PortfolioMutationResolver {
    
    private final MockDataService dataService;
    
    /**
     * Mutation: createPortfolio
     * 
     * Crea un nuevo portfolio para el usuario autenticado.
     * 
     * SECCIÓN 1.2: Demuestra mutations básicas
     * SECCIÓN 1.5: Requiere autenticación (en producción)
     * 
     * Ejemplo de uso:
     * 
     * mutation CreatePortfolio($input: CreatePortfolioInput!) {
     *   createPortfolio(input: $input) {
     *     success
     *     message
     *     portfolio {
     *       id
     *       name
     *       createdAt
     *     }
     *   }
     * }
     * 
     * Variables:
     * {
     *   "input": {
     *     "name": "Growth Portfolio 2024"
     *   }
     * }
     * 
     * PATRÓN DE RESPUESTA:
     * - success: boolean indicando si la operación fue exitosa
     * - message: mensaje descriptivo para el usuario
     * - portfolio: el objeto creado (opcional, solo si success=true)
     */
    @MutationMapping
    public CreatePortfolioResponse createPortfolio(@Argument Map<String, Object> input) {
        try {
            // TODO (Sección 1.5): Obtener userId del SecurityContext
            String userId = "user-001"; // Hardcoded para este ejemplo
            String userName = "Carlos Mendoza"; // En producción, vendría del token JWT
            
            String name = (String) input.get("name");
            
            if (name == null || name.trim().isEmpty()) {
                return CreatePortfolioResponse.builder()
                        .success(false)
                        .message("Portfolio name is required")
                        .build();
            }
            
            Portfolio portfolio = dataService.createPortfolio(name, userId, userName);
            
            return CreatePortfolioResponse.builder()
                    .success(true)
                    .message("Portfolio created successfully")
                    .portfolio(portfolio)
                    .build();
                    
        } catch (Exception e) {
            return CreatePortfolioResponse.builder()
                    .success(false)
                    .message("Error creating portfolio: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Mutation: addAsset
     * 
     * Añade un activo a un portfolio existente.
     * 
     * SECCIÓN 1.2: Mutation más compleja con múltiples campos en el input
     * 
     * Ejemplo de uso:
     * 
     * mutation AddAsset($input: AddAssetInput!) {
     *   addAsset(input: $input) {
     *     success
     *     message
     *     asset {
     *       id
     *       symbol
     *       totalValue
     *     }
     *   }
     * }
     * 
     * Variables:
     * {
     *   "input": {
     *     "portfolioId": "portfolio-001",
     *     "symbol": "TSLA",
     *     "assetType": "STOCK",
     *     "quantity": 5.0,
     *     "buyPrice": 200.0
     *   }
     * }
     */
    @MutationMapping
    public AddAssetResponse addAsset(@Argument Map<String, Object> input) {
        try {
            String portfolioId = (String) input.get("portfolioId");
            String symbol = (String) input.get("symbol");
            String assetTypeStr = (String) input.get("assetType");
            Double quantity = ((Number) input.get("quantity")).doubleValue();
            Double buyPrice = ((Number) input.get("buyPrice")).doubleValue();
            
            // Validaciones
            if (portfolioId == null || symbol == null) {
                return AddAssetResponse.builder()
                        .success(false)
                        .message("portfolioId and symbol are required")
                        .build();
            }
            
            AssetType assetType = AssetType.valueOf(assetTypeStr);
            
            // TODO: En producción, obtener el nombre del activo de una API externa
            String assetName = symbol + " - " + assetType.name();
            
            Asset asset = dataService.addAssetToPortfolio(
                portfolioId,
                symbol,
                assetName,
                assetType,
                quantity,
                buyPrice
            );
            
            return AddAssetResponse.builder()
                    .success(true)
                    .message("Asset added successfully")
                    .asset(asset)
                    .build();
                    
        } catch (IllegalArgumentException e) {
            return AddAssetResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            return AddAssetResponse.builder()
                    .success(false)
                    .message("Error adding asset: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Mutation: removeAsset
     * 
     * Elimina un activo de un portfolio.
     * 
     * SECCIÓN 1.2: Mutation de eliminación
     * 
     * Ejemplo de uso:
     * 
     * mutation RemoveAsset($portfolioId: ID!, $assetId: ID!) {
     *   removeAsset(portfolioId: $portfolioId, assetId: $assetId) {
     *     success
     *     message
     *     code
     *   }
     * }
     * 
     * Variables:
     * {
     *   "portfolioId": "portfolio-001",
     *   "assetId": "asset-003"
     * }
     */
    @MutationMapping
    public MutationResponse removeAsset(
            @Argument String portfolioId,
            @Argument String assetId) {
        
        boolean removed = dataService.removeAsset(portfolioId, assetId);
        
        if (removed) {
            return MutationResponse.builder()
                    .success(true)
                    .message("Asset removed successfully")
                    .code("ASSET_REMOVED")
                    .build();
        } else {
            return MutationResponse.builder()
                    .success(false)
                    .message("Asset not found or could not be removed")
                    .code("ASSET_NOT_FOUND")
                    .build();
        }
    }
    
    // =========================================================================
    // RESPONSE TYPES (DTOs)
    // =========================================================================
    
    /**
     * DTO para respuesta de createPortfolio mutation
     */
    @lombok.Data
    @lombok.Builder
    public static class CreatePortfolioResponse {
        private Boolean success;
        private String message;
        private Portfolio portfolio;
    }
    
    /**
     * DTO para respuesta de addAsset mutation
     */
    @lombok.Data
    @lombok.Builder
    public static class AddAssetResponse {
        private Boolean success;
        private String message;
        private Asset asset;
    }
    
    /**
     * DTO para respuesta genérica de mutations
     */
    @lombok.Data
    @lombok.Builder
    public static class MutationResponse {
        private Boolean success;
        private String message;
        private String code;
    }
}

/*
 * =============================================================================
 * VENTAJAS DE MUTATIONS EN GRAPHQL (Sección 1.2)
 * =============================================================================
 * 
 * 1. EL CLIENTE DECIDE QUÉ RETORNAR:
 * 
 * En REST, después de crear un portfolio con POST, recibes el objeto completo.
 * En GraphQL, el cliente decide:
 * 
 * mutation {
 *   createPortfolio(input: {name: "Test"}) {
 *     success
 *     portfolio { id }    ← Solo retornar el ID
 *   }
 * }
 * 
 * O si necesita más:
 * 
 * mutation {
 *   createPortfolio(input: {name: "Test"}) {
 *     success
 *     portfolio {
 *       id
 *       name
 *       createdAt
 *       totalValue
 *     }
 *   }
 * }
 * 
 * 
 * 2. ATOMICIDAD Y RESPUESTA ESTRUCTURADA:
 * 
 * Las mutations retornan un objeto de respuesta estructurado:
 * - success: para saber si la operación fue exitosa
 * - message: mensaje para el usuario
 * - code: código de error/éxito (útil para i18n)
 * - [objeto]: el recurso creado/modificado
 * 
 * Esto permite manejar errores de manera consistente en el cliente.
 * 
 * 
 * 3. MÚLTIPLES MUTATIONS EN UNA SOLA REQUEST:
 * 
 * GraphQL permite ejecutar múltiples mutations en secuencia:
 * 
 * mutation {
 *   portfolio: createPortfolio(input: {name: "New"}) {
 *     success
 *     portfolio { id }
 *   }
 *   asset: addAsset(input: {
 *     portfolioId: "portfolio-001"
 *     symbol: "AAPL"
 *     assetType: STOCK
 *     quantity: 10
 *     buyPrice: 150
 *   }) {
 *     success
 *     asset { id }
 *   }
 * }
 * 
 * En REST, necesitarías 2 llamadas HTTP separadas.
 * 
 * =============================================================================
 */
