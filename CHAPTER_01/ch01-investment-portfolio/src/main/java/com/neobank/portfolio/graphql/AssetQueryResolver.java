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
 * GraphQL Resolver para consultas de Assets con Filtros, Ordenamiento y Paginación.
 * 
 * 🎓 SECCIÓN 1.4 - FILTROS, ORDEN Y PAGINACIÓN
 * 
 * Este resolver implementa la query "assets" del schema GraphQL, que permite
 * consultar activos de un portfolio con capacidades avanzadas de:
 * - Filtrado (por tipo, valor mínimo/máximo, símbolo)
 * - Ordenamiento (por símbolo, valor, ganancia, cantidad)
 * - Paginación cursor-based (patrón Relay)
 * 
 * 💡 ANALOGÍA:
 * Es como un buscador de productos en Amazon:
 * - Filtras por categoría, precio, marca
 * - Ordenas por precio, relevancia, valoraciones
 * - Navegas página por página
 * 
 * 📦 PATRÓN RELAY CONNECTION:
 * Este resolver implementa el estándar de paginación de Relay/Facebook:
 * - edges: Lista de resultados, cada uno con su cursor
 * - pageInfo: Metadatos de navegación (hasNextPage, cursors)
 * - totalCount: Total de elementos en la colección
 * 
 * 💡 EJEMPLO DE USO:
 * ```graphql
 * query {
 *   assets(
 *     portfolioId: "portfolio-001"
 *     filter: { assetType: STOCK, minValue: 1000 }
 *     sort: { field: TOTAL_VALUE, direction: DESC }
 *     pagination: { limit: 5 }
 *   ) {
 *     totalCount
 *     edges {
 *       cursor
 *       node { symbol totalValue }
 *     }
 *     pageInfo {
 *       hasNextPage
 *       endCursor
 *     }
 *   }
 * }
 * ```
 * 
 * @see schema.graphqls (type Query, assets field)
 */
@Controller
public class AssetQueryResolver {
    
    private final MockDataService dataService;
    
    /**
     * Constructor con inyección de dependencias.
     * 
     * @param dataService Servicio de datos para acceder a los activos
     */
    public AssetQueryResolver(MockDataService dataService) {
        this.dataService = dataService;
    }
    
    /**
     * Query: assets
     * 
     * Obtiene activos de un portfolio con filtros, orden y paginación.
     * 
     * 🎓 FLUJO DE PROCESAMIENTO:
     * 1. Obtener todos los assets del portfolio
     * 2. Aplicar filtros (si existen)
     * 3. Aplicar ordenamiento (si existe)
     * 4. Aplicar paginación
     * 5. Retornar AssetConnection con edges, pageInfo y totalCount
     * 
     * @param portfolioId ID del portfolio (OBLIGATORIO)
     * @param filter Filtros opcionales (assetType, minValue, maxValue, symbolContains)
     * @param sort Ordenamiento opcional (field, direction)
     * @param pagination Paginación opcional (limit, after)
     * @return AssetConnection con los resultados paginados
     */
    @QueryMapping
    public AssetConnection assets(
            @Argument String portfolioId,
            @Argument Map<String, Object> filter,
            @Argument Map<String, Object> sort,
            @Argument Map<String, Object> pagination) {
        
        // 1. Obtener todos los assets del portfolio
        List<Asset> allAssets = dataService.getAssetsByPortfolioId(portfolioId);
        
        // 2. FILTRAR - Reduce el conjunto de resultados según criterios
        List<Asset> filteredAssets = applyFilters(allAssets, filter);
        
        // 3. ORDENAR - Organiza los resultados según campo y dirección
        List<Asset> sortedAssets = applySorting(filteredAssets, sort);
        
        // 4. PAGINAR - Retorna solo una "página" de resultados
        return applyPagination(sortedAssets, pagination);
    }
    
    /**
     * Aplica filtros a la lista de activos.
     * 
     * 🎓 FILTROS DISPONIBLES:
     * - assetType: Tipo de activo (STOCK, CRYPTO, ETF, BOND, COMMODITY)
     * - minValue: Valor total mínimo del activo
     * - maxValue: Valor total máximo del activo
     * - symbolContains: Búsqueda parcial en el símbolo (case-insensitive)
     * 
     * 💡 TODOS LOS FILTROS SON OPCIONALES Y ACUMULATIVOS:
     * Si se especifican varios, se aplican con lógica AND.
     * 
     * @param assets Lista original de activos
     * @param filter Mapa con los filtros a aplicar
     * @return Lista filtrada de activos
     */
    private List<Asset> applyFilters(List<Asset> assets, Map<String, Object> filter) {
        // Si no hay filtros, retornar lista original
        if (filter == null || filter.isEmpty()) {
            return assets;
        }
        
        return assets.stream()
                .filter(asset -> {
                    // ─────────────────────────────────────────────────────
                    // FILTRO POR TIPO DE ACTIVO
                    // Ejemplo: filter: { assetType: STOCK }
                    // ─────────────────────────────────────────────────────
                    if (filter.containsKey("assetType")) {
                        String assetTypeStr = (String) filter.get("assetType");
                        AssetType filterType = AssetType.valueOf(assetTypeStr);
                        if (!asset.getAssetType().equals(filterType)) {
                            return false;
                        }
                    }
                    
                    // ─────────────────────────────────────────────────────
                    // FILTRO POR VALOR MÍNIMO
                    // Ejemplo: filter: { minValue: 1000 }
                    // Solo incluye activos con totalValue >= minValue
                    // ─────────────────────────────────────────────────────
                    if (filter.containsKey("minValue")) {
                        Double minValue = ((Number) filter.get("minValue")).doubleValue();
                        if (asset.getTotalValue() < minValue) {
                            return false;
                        }
                    }
                    
                    // ─────────────────────────────────────────────────────
                    // FILTRO POR VALOR MÁXIMO
                    // Ejemplo: filter: { maxValue: 50000 }
                    // Solo incluye activos con totalValue <= maxValue
                    // ─────────────────────────────────────────────────────
                    if (filter.containsKey("maxValue")) {
                        Double maxValue = ((Number) filter.get("maxValue")).doubleValue();
                        if (asset.getTotalValue() > maxValue) {
                            return false;
                        }
                    }
                    
                    // ─────────────────────────────────────────────────────
                    // FILTRO POR SÍMBOLO (búsqueda parcial)
                    // Ejemplo: filter: { symbolContains: "AA" }
                    // Encuentra "AAPL", "AAL", etc.
                    // ─────────────────────────────────────────────────────
                    if (filter.containsKey("symbolContains")) {
                        String symbolFilter = (String) filter.get("symbolContains");
                        if (!asset.getSymbol().toUpperCase().contains(symbolFilter.toUpperCase())) {
                            return false;
                        }
                    }
                    
                    // Si pasa todos los filtros, incluir el activo
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Aplica ordenamiento a la lista de activos.
     * 
     * 🎓 CAMPOS DE ORDENAMIENTO (AssetSortField):
     * - SYMBOL: Ordenar alfabéticamente por símbolo
     * - TOTAL_VALUE: Ordenar por valor total del activo
     * - PROFIT_LOSS_PERCENT: Ordenar por porcentaje de ganancia/pérdida
     * - QUANTITY: Ordenar por cantidad de unidades
     * 
     * 🎓 DIRECCIÓN (SortDirection):
     * - ASC: Ascendente (A-Z, menor a mayor)
     * - DESC: Descendente (Z-A, mayor a menor)
     * 
     * @param assets Lista de activos a ordenar
     * @param sort Mapa con field y direction
     * @return Lista ordenada de activos
     */
    private List<Asset> applySorting(List<Asset> assets, Map<String, Object> sort) {
        // Si no hay ordenamiento, retornar lista original
        if (sort == null || sort.isEmpty()) {
            return assets;
        }
        
        String field = (String) sort.get("field");
        String direction = (String) sort.get("direction");
        
        // Crear comparador según el campo especificado
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
                // Si el campo no es válido, ordenar por símbolo por defecto
                comparator = Comparator.comparing(Asset::getSymbol);
                break;
        }
        
        // Invertir si es descendente
        if ("DESC".equals(direction)) {
            comparator = comparator.reversed();
        }
        
        return assets.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    
    /**
     * Aplica paginación cursor-based a la lista de activos.
     * 
     * 🎓 ¿QUÉ ES CURSOR-BASED PAGINATION?
     * En lugar de usar offset (página 1, 2, 3...), usamos cursors que
     * apuntan a un elemento específico. Esto evita problemas cuando
     * se insertan o eliminan datos entre páginas.
     * 
     * 💡 EJEMPLO:
     * Primera página: pagination: { limit: 10 }
     * Segunda página: pagination: { limit: 10, after: "cursor-del-ultimo" }
     * 
     * 🔑 CURSOR:
     * Usamos Base64 del ID del asset como cursor.
     * Esto oculta el ID real y permite decodificarlo fácilmente.
     * 
     * @param assets Lista completa de activos (ya filtrada y ordenada)
     * @param pagination Mapa con limit y after (cursor)
     * @return AssetConnection con edges, pageInfo y totalCount
     */
    private AssetConnection applyPagination(List<Asset> assets, Map<String, Object> pagination) {
        // Valores por defecto
        int limit = 10;
        String afterCursor = null;
        
        // Extraer parámetros de paginación si existen
        if (pagination != null) {
            if (pagination.containsKey("limit")) {
                limit = ((Number) pagination.get("limit")).intValue();
            }
            if (pagination.containsKey("after")) {
                afterCursor = (String) pagination.get("after");
            }
        }
        
        // ─────────────────────────────────────────────────────────────────
        // ENCONTRAR EL ÍNDICE DE INICIO
        // Si hay cursor "after", empezamos DESPUÉS de ese elemento
        // ─────────────────────────────────────────────────────────────────
        int startIndex = 0;
        if (afterCursor != null) {
            String decodedCursor = decodeCursor(afterCursor);
            for (int i = 0; i < assets.size(); i++) {
                if (assets.get(i).getId().equals(decodedCursor)) {
                    startIndex = i + 1; // Empezar DESPUÉS del cursor
                    break;
                }
            }
        }
        
        // Calcular índice final (sin exceder el tamaño de la lista)
        int endIndex = Math.min(startIndex + limit, assets.size());
        
        // Obtener la sublista de esta "página"
        List<Asset> pageAssets = assets.subList(startIndex, endIndex);
        
        // ─────────────────────────────────────────────────────────────────
        // CREAR EDGES (cada asset envuelto con su cursor)
        // ─────────────────────────────────────────────────────────────────
        List<AssetEdge> edges = pageAssets.stream()
                .map(asset -> new AssetEdge(
                        asset,
                        encodeCursor(asset.getId())
                ))
                .collect(Collectors.toList());
        
        // ─────────────────────────────────────────────────────────────────
        // CREAR PAGE INFO (metadatos de navegación)
        // ─────────────────────────────────────────────────────────────────
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
        
        // Retornar la conexión completa
        return new AssetConnection(edges, pageInfo, assets.size());
    }
    
    /**
     * Codifica un ID en Base64 para usarlo como cursor.
     * 
     * 💡 ¿POR QUÉ BASE64?
     * - Oculta el ID real del elemento
     * - Es fácil de transmitir en URLs y JSON
     * - Es reversible (podemos decodificarlo)
     * 
     * @param id ID del asset
     * @return Cursor codificado en Base64
     */
    private String encodeCursor(String id) {
        return Base64.getEncoder().encodeToString(id.getBytes());
    }
    
    /**
     * Decodifica un cursor Base64 para obtener el ID original.
     * 
     * @param cursor Cursor codificado
     * @return ID original del asset
     */
    private String decodeCursor(String cursor) {
        return new String(Base64.getDecoder().decode(cursor));
    }
    
    // =========================================================================
    // DTOs PARA LA RESPUESTA PAGINADA (Patrón Relay Connection)
    // =========================================================================
    
    /**
     * Contenedor principal de la respuesta paginada.
     * 
     * 📦 ESTRUCTURA:
     * AssetConnection
     * ├── edges[]      → Lista de resultados con cursors
     * ├── pageInfo     → Metadatos de navegación
     * └── totalCount   → Total de elementos en toda la colección
     */
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
    
    /**
     * Envuelve un Asset con su cursor de paginación.
     * 
     * 💡 ¿POR QUÉ EDGE?
     * El patrón Edge permite agregar metadatos por elemento
     * (como el cursor) sin contaminar el tipo original (Asset).
     */
    public static class AssetEdge {
        private Asset node;    // El activo en sí
        private String cursor; // Cursor único para este elemento
        
        public AssetEdge(Asset node, String cursor) {
            this.node = node;
            this.cursor = cursor;
        }
        
        public Asset getNode() { return node; }
        public String getCursor() { return cursor; }
    }
    
    /**
     * Metadatos de paginación.
     * 
     * 🎓 CAMPOS:
     * - hasNextPage: ¿Hay más elementos después de esta página?
     * - hasPreviousPage: ¿Hay elementos antes de esta página?
     * - startCursor: Cursor del primer elemento de esta página
     * - endCursor: Cursor del último elemento de esta página
     * 
     * 💡 USO:
     * Si hasNextPage es true, el cliente puede pedir la siguiente página
     * usando: pagination: { after: endCursor }
     */
    public static class PageInfo {
        private Boolean hasNextPage;
        private Boolean hasPreviousPage;
        private String startCursor;
        private String endCursor;
        
        public PageInfo(Boolean hasNextPage, Boolean hasPreviousPage, 
                       String startCursor, String endCursor) {
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

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO - SECCIÓN 1.4
 * =============================================================================
 * 
 * 📊 CAPACIDADES IMPLEMENTADAS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  FILTRADO         │  Por tipo, valor mín/máx, símbolo                  │
 * │  ORDENAMIENTO     │  Por símbolo, valor, ganancia, cantidad            │
 * │  PAGINACIÓN       │  Cursor-based (patrón Relay)                       │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 🎯 VENTAJAS DE CURSOR-BASED vs OFFSET:
 * 
 * OFFSET (tradicional):
 * - Página 1: OFFSET 0, LIMIT 10
 * - Página 2: OFFSET 10, LIMIT 10
 * - ⚠️ Si se inserta un elemento, la página 2 muestra un duplicado
 * 
 * CURSOR (Relay):
 * - Primera: { limit: 10 }
 * - Segunda: { limit: 10, after: "cursor-ultimo-elemento" }
 * - ✅ Siempre consistente, aunque se modifiquen los datos
 * 
 * =============================================================================
 */