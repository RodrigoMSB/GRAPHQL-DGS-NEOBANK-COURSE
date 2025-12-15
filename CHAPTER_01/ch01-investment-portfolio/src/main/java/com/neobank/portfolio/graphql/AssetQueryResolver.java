package com.neobank.portfolio.graphql;

import com.neobank.portfolio.data.MockDataService;
import com.neobank.portfolio.model.Asset;
import com.neobank.portfolio.model.AssetType;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

/**
 * GraphQL Resolver para Assets con Filtros, Ordenamiento y Paginación
 * 
 * SECCIÓN 1.4 - FILTROS, ORDEN Y PAGINACIÓN
 */
@Controller
public class AssetQueryResolver {
    
    private final MockDataService dataService;
    
    public AssetQueryResolver(MockDataService dataService) {
        this.dataService = dataService;
    }
    
    @QueryMapping
    public AssetConnection assets(
            @Argument String portfolioId,
            @Argument Map<String, Object> filter,
            @Argument Map<String, Object> sort,
            @Argument Map<String, Object> pagination) {
        
        // 1. Obtener todos los assets del portfolio
        List<Asset> allAssets = dataService.getAssetsByPortfolioId(portfolioId);
        
        // 2. FILTRAR
        List<Asset> filteredAssets = applyFilters(allAssets, filter);
        
        // 3. ORDENAR
        List<Asset> sortedAssets = applySorting(filteredAssets, sort);
        
        // 4. PAGINAR
        return applyPagination(sortedAssets, pagination);
    }
    
    private List<Asset> applyFilters(List<Asset> assets, Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return assets;
        }
        
        return assets.stream()
                .filter(asset -> {
                    // Filtro por tipo de activo
                    if (filter.containsKey("assetType")) {
                        String assetTypeStr = (String) filter.get("assetType");
                        AssetType filterType = AssetType.valueOf(assetTypeStr);
                        if (!asset.getAssetType().equals(filterType)) {
                            return false;
                        }
                    }
                    
                    // Filtro por valor mínimo
                    if (filter.containsKey("minValue")) {
                        Double minValue = ((Number) filter.get("minValue")).doubleValue();
                        if (asset.getTotalValue() < minValue) {
                            return false;
                        }
                    }
                    
                    // Filtro por valor máximo
                    if (filter.containsKey("maxValue")) {
                        Double maxValue = ((Number) filter.get("maxValue")).doubleValue();
                        if (asset.getTotalValue() > maxValue) {
                            return false;
                        }
                    }
                    
                    // Filtro por símbolo
                    if (filter.containsKey("symbolContains")) {
                        String symbolFilter = (String) filter.get("symbolContains");
                        if (!asset.getSymbol().toUpperCase().contains(symbolFilter.toUpperCase())) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    private List<Asset> applySorting(List<Asset> assets, Map<String, Object> sort) {
        if (sort == null || sort.isEmpty()) {
            return assets;
        }
        
        String field = (String) sort.get("field");
        String direction = (String) sort.get("direction");
        
        Comparator<Asset> comparator;
        switch (field) {
            case "SYMBOL":
                comparator = Comparator.comparing(Asset::getSymbol);
                break;
            case "TOTAL_VALUE":
                comparator = Comparator.comparing(Asset::getTotalValue);
                break;
            case "PROFIT_LOSS_PERCENT":
                comparator = Comparator.comparing(Asset::getProfitLossPercent);
                break;
            case "QUANTITY":
                comparator = Comparator.comparing(Asset::getQuantity);
                break;
            default:
                comparator = Comparator.comparing(Asset::getSymbol);
                break;
        }
        
        if ("DESC".equals(direction)) {
            comparator = comparator.reversed();
        }
        
        return assets.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    
    private AssetConnection applyPagination(List<Asset> assets, Map<String, Object> pagination) {
        int limit = 10;
        String afterCursor = null;
        
        if (pagination != null) {
            if (pagination.containsKey("limit")) {
                limit = ((Number) pagination.get("limit")).intValue();
            }
            if (pagination.containsKey("after")) {
                afterCursor = (String) pagination.get("after");
            }
        }
        
        int startIndex = 0;
        if (afterCursor != null) {
            String decodedCursor = decodeCursor(afterCursor);
            for (int i = 0; i < assets.size(); i++) {
                if (assets.get(i).getId().equals(decodedCursor)) {
                    startIndex = i + 1;
                    break;
                }
            }
        }
        
        int endIndex = Math.min(startIndex + limit, assets.size());
        List<Asset> pageAssets = assets.subList(startIndex, endIndex);
        
        List<AssetEdge> edges = pageAssets.stream()
                .map(asset -> new AssetEdge(
                        asset,
                        encodeCursor(asset.getId())
                ))
                .collect(Collectors.toList());
        
        boolean hasNextPage = endIndex < assets.size();
        boolean hasPreviousPage = startIndex > 0;
        
        String startCursor = edges.isEmpty() ? null : edges.get(0).getCursor();
        String endCursor = edges.isEmpty() ? null : edges.get(edges.size() - 1).getCursor();
        
        PageInfo pageInfo = new PageInfo(
            hasNextPage,
            hasPreviousPage,
            startCursor,
            endCursor
        );
        
        return new AssetConnection(edges, pageInfo, assets.size());
    }
    
    private String encodeCursor(String id) {
        return Base64.getEncoder().encodeToString(id.getBytes());
    }
    
    private String decodeCursor(String cursor) {
        return new String(Base64.getDecoder().decode(cursor));
    }
    
    // =========================================================================
    // DTOs para la respuesta paginada
    // =========================================================================
    
    public static class AssetConnection {
        private List<AssetEdge> edges;
        private PageInfo pageInfo;
        private Integer totalCount;
        
        public AssetConnection(List<AssetEdge> edges, PageInfo pageInfo, Integer totalCount) {
            this.edges = edges;
            this.pageInfo = pageInfo;
            this.totalCount = totalCount;
        }
        
        public List<AssetEdge> getEdges() { return edges; }
        public PageInfo getPageInfo() { return pageInfo; }
        public Integer getTotalCount() { return totalCount; }
    }
    
    public static class AssetEdge {
        private Asset node;
        private String cursor;
        
        public AssetEdge(Asset node, String cursor) {
            this.node = node;
            this.cursor = cursor;
        }
        
        public Asset getNode() { return node; }
        public String getCursor() { return cursor; }
    }
    
    public static class PageInfo {
        private Boolean hasNextPage;
        private Boolean hasPreviousPage;
        private String startCursor;
        private String endCursor;
        
        public PageInfo(Boolean hasNextPage, Boolean hasPreviousPage, String startCursor, String endCursor) {
            this.hasNextPage = hasNextPage;
            this.hasPreviousPage = hasPreviousPage;
            this.startCursor = startCursor;
            this.endCursor = endCursor;
        }
        
        public Boolean getHasNextPage() { return hasNextPage; }
        public Boolean getHasPreviousPage() { return hasPreviousPage; }
        public String getStartCursor() { return startCursor; }
        public String getEndCursor() { return endCursor; }
    }
}
