package com.neobank.portfolio.data;

import com.neobank.portfolio.model.Asset;
import com.neobank.portfolio.model.AssetType;
import com.neobank.portfolio.model.Performance;
import com.neobank.portfolio.model.Portfolio;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de datos simulados para el Investment Portfolio Tracker.
 * 
 * 🎓 PROPÓSITO:
 * Proporciona datos en memoria para demostrar las funcionalidades de GraphQL
 * sin necesidad de configurar una base de datos real.
 * 
 * 💡 ANALOGÍA:
 * Es como un "banco de pruebas" con datos ficticios pero realistas,
 * similar a los datos de demo que ves en productos SaaS antes de registrarte.
 * 
 * ⚠️ IMPORTANTE:
 * - Este servicio es compartido por REST y GraphQL
 * - Demuestra que ambos pueden usar la MISMA lógica de negocio
 * - En producción, se reemplazaría por repositorios JPA/MongoDB
 * 
 * 📦 DATOS INICIALES:
 * - Usuario: Carlos Mendoza (user-001)
 * - Portfolio 1: "Growth Portfolio" con 4 activos (AAPL, GOOGL, BTC, VOO)
 * - Portfolio 2: "Retirement Fund" con 3 activos (BND, GLD, MSFT)
 * 
 * 🎓 SECCIONES QUE DEMUESTRA:
 * - Sección 1.1: Mismos datos para REST y GraphQL (comparación)
 * - Sección 1.2: Estructura de datos para Types del schema
 * - Sección 1.3: Relaciones anidadas (Portfolio → Assets → Performance)
 * 
 * @see PortfolioQueryResolver (consume este servicio via GraphQL)
 * @see PortfolioRestController (consume este servicio via REST)
 */
@Service
public class MockDataService {
    
    /**
     * Almacén en memoria de portfolios.
     * Key: portfolio ID, Value: Portfolio object
     */
    private final Map<String, Portfolio> portfolios = new HashMap<>();
    
    /**
     * Almacén en memoria de activos.
     * Key: asset ID, Value: Asset object
     * 
     * 💡 Mantenemos un mapa separado para búsquedas rápidas por ID
     */
    private final Map<String, Asset> assets = new HashMap<>();
    
    /**
     * Constructor que inicializa los datos de demostración.
     * 
     * Se ejecuta automáticamente cuando Spring crea el bean.
     */
    public MockDataService() {
        initializeMockData();
    }
    
    /**
     * Inicializa los datos de ejemplo para demostración.
     * 
     * 📊 DATOS CREADOS:
     * 
     * USUARIO: Carlos Mendoza (user-001)
     * 
     * PORTFOLIO 1: Growth Portfolio (portfolio-001)
     * ├── AAPL  - Apple Inc.           (STOCK)     - 10 unidades @ $150 → $185.50
     * ├── GOOGL - Alphabet Inc.        (STOCK)     - 5 unidades @ $2,800 → $2,950.75
     * ├── BTC   - Bitcoin              (CRYPTO)    - 0.5 unidades @ $45,000 → $67,000
     * └── VOO   - Vanguard S&P 500 ETF (ETF)       - 20 unidades @ $400 → $445.25
     * 
     * PORTFOLIO 2: Retirement Fund (portfolio-002)
     * ├── BND   - Vanguard Total Bond  (ETF)       - 50 unidades @ $80 → $82.15
     * ├── GLD   - SPDR Gold Shares     (COMMODITY) - 15 unidades @ $180 → $195.30
     * └── MSFT  - Microsoft Corp.      (STOCK)     - 8 unidades @ $300 → $378.85
     */
    private void initializeMockData() {
        // ─────────────────────────────────────────────────────────────────
        // USUARIO DEMO
        // ─────────────────────────────────────────────────────────────────
        String userId = "user-001";
        String userName = "Carlos Mendoza";
        
        // ─────────────────────────────────────────────────────────────────
        // PORTFOLIO 1: GROWTH PORTFOLIO
        // Enfoque: Crecimiento agresivo con acciones tech y crypto
        // ─────────────────────────────────────────────────────────────────
        Portfolio growthPortfolio = createPortfolio(
            "portfolio-001",
            "Growth Portfolio",
            userId,
            userName
        );
        
        // Añadir activos al Growth Portfolio
        addAssetToPortfolio(growthPortfolio, createAsset(
            "asset-001", "AAPL", "Apple Inc.", AssetType.STOCK,
            10.0, 150.0, 185.50  // +23.67% ganancia
        ));
        
        addAssetToPortfolio(growthPortfolio, createAsset(
            "asset-002", "GOOGL", "Alphabet Inc.", AssetType.STOCK,
            5.0, 2800.0, 2950.75  // +5.38% ganancia
        ));
        
        addAssetToPortfolio(growthPortfolio, createAsset(
            "asset-003", "BTC", "Bitcoin", AssetType.CRYPTO,
            0.5, 45000.0, 67000.0  // +48.89% ganancia
        ));
        
        addAssetToPortfolio(growthPortfolio, createAsset(
            "asset-004", "VOO", "Vanguard S&P 500 ETF", AssetType.ETF,
            20.0, 400.0, 445.25  // +11.31% ganancia
        ));
        
        // ─────────────────────────────────────────────────────────────────
        // PORTFOLIO 2: RETIREMENT FUND
        // Enfoque: Conservador con bonos, oro y blue chips
        // ─────────────────────────────────────────────────────────────────
        Portfolio retirementPortfolio = createPortfolio(
            "portfolio-002",
            "Retirement Fund",
            userId,
            userName
        );
        
        addAssetToPortfolio(retirementPortfolio, createAsset(
            "asset-005", "BND", "Vanguard Total Bond Market ETF", AssetType.ETF,
            50.0, 80.0, 82.15  // +2.69% ganancia
        ));
        
        addAssetToPortfolio(retirementPortfolio, createAsset(
            "asset-006", "GLD", "SPDR Gold Shares", AssetType.COMMODITY,
            15.0, 180.0, 195.30  // +8.5% ganancia
        ));
        
        addAssetToPortfolio(retirementPortfolio, createAsset(
            "asset-007", "MSFT", "Microsoft Corporation", AssetType.STOCK,
            8.0, 300.0, 378.85  // +26.28% ganancia
        ));
        
        // ─────────────────────────────────────────────────────────────────
        // CALCULAR TOTALES Y PERFORMANCE
        // ─────────────────────────────────────────────────────────────────
        growthPortfolio.calculateTotalValue();
        retirementPortfolio.calculateTotalValue();
        
        growthPortfolio.setPerformance(calculatePerformance(growthPortfolio));
        retirementPortfolio.setPerformance(calculatePerformance(retirementPortfolio));
        
        // Guardar en el repositorio en memoria
        portfolios.put(growthPortfolio.getId(), growthPortfolio);
        portfolios.put(retirementPortfolio.getId(), retirementPortfolio);
    }
    
    /**
     * Crea un nuevo portfolio (método interno).
     * 
     * @param id ID único del portfolio
     * @param name Nombre descriptivo
     * @param ownerId ID del propietario
     * @param ownerName Nombre del propietario
     * @return Portfolio creado
     */
    private Portfolio createPortfolio(String id, String name, String ownerId, String ownerName) {
        return Portfolio.builder()
                .id(id)
                .name(name)
                .ownerId(ownerId)
                .ownerName(ownerName)
                .createdAt(LocalDateTime.now().minusMonths(6)) // Creado hace 6 meses
                .assets(new ArrayList<>())
                .build();
    }
    
    /**
     * Crea un nuevo activo (método interno).
     * 
     * 💡 NOTA: También lo registra en el mapa de assets para búsquedas rápidas.
     * 
     * @param id ID único del activo
     * @param symbol Símbolo bursátil (AAPL, BTC, etc.)
     * @param name Nombre completo
     * @param type Tipo de activo
     * @param quantity Cantidad de unidades
     * @param buyPrice Precio de compra promedio
     * @param currentPrice Precio actual del mercado
     * @return Asset creado
     */
    private Asset createAsset(String id, String symbol, String name, AssetType type,
                             Double quantity, Double buyPrice, Double currentPrice) {
        Asset asset = Asset.builder()
                .id(id)
                .symbol(symbol)
                .name(name)
                .assetType(type)
                .quantity(quantity)
                .averageBuyPrice(buyPrice)
                .currentPrice(currentPrice)
                .lastUpdated(LocalDateTime.now())
                .build();
        
        // Registrar en el mapa de assets
        assets.put(id, asset);
        return asset;
    }
    
    /**
     * Añade un activo a un portfolio (método interno).
     */
    private void addAssetToPortfolio(Portfolio portfolio, Asset asset) {
        portfolio.getAssets().add(asset);
    }
    
    /**
     * Calcula el rendimiento de un portfolio.
     * 
     * 🎓 CÁLCULOS:
     * - totalReturn: Promedio de ganancia/pérdida de todos los activos
     * - yearReturn: totalReturn * 0.8 (simulado)
     * - monthReturn: totalReturn * 0.2 (simulado)
     * - weekReturn: totalReturn * 0.05 (simulado)
     * - bestPerformer: Activo con mayor % de ganancia
     * - worstPerformer: Activo con menor % de ganancia (o mayor pérdida)
     * 
     * @param portfolio Portfolio a analizar
     * @return Performance calculado
     */
    private Performance calculatePerformance(Portfolio portfolio) {
        List<Asset> assetList = portfolio.getAssets();
        
        // Si no hay activos, retornar performance vacío
        if (assetList.isEmpty()) {
            return Performance.builder()
                    .totalReturn(0.0)
                    .yearReturn(0.0)
                    .monthReturn(0.0)
                    .weekReturn(0.0)
                    .build();
        }
        
        // Encontrar mejor y peor performer
        Asset best = assetList.stream()
                .max(Comparator.comparing(Asset::getProfitLossPercent))
                .orElse(null);
        
        Asset worst = assetList.stream()
                .min(Comparator.comparing(Asset::getProfitLossPercent))
                .orElse(null);
        
        // Calcular rendimiento promedio
        double avgReturn = assetList.stream()
                .mapToDouble(Asset::getProfitLossPercent)
                .average()
                .orElse(0.0);
        
        return Performance.builder()
                .totalReturn(avgReturn)
                .yearReturn(avgReturn * 0.8)   // Simulado: 80% del total
                .monthReturn(avgReturn * 0.2)  // Simulado: 20% del total
                .weekReturn(avgReturn * 0.05)  // Simulado: 5% del total
                .bestPerformer(best)
                .worstPerformer(worst)
                .build();
    }
    
    // =========================================================================
    // MÉTODOS PÚBLICOS PARA CONSULTAS (usados por Resolvers y Controllers)
    // =========================================================================
    
    /**
     * Obtiene todos los portfolios.
     * 
     * @return Lista de todos los portfolios
     */
    public List<Portfolio> getAllPortfolios() {
        return new ArrayList<>(portfolios.values());
    }
    
    /**
     * Obtiene un portfolio por ID.
     * 
     * @param id ID del portfolio
     * @return Portfolio o null si no existe
     */
    public Portfolio getPortfolioById(String id) {
        return portfolios.get(id);
    }
    
    /**
     * Obtiene portfolios de un usuario específico.
     * 
     * 🎓 Este método demuestra cómo se filtraría por usuario autenticado.
     * 
     * @param ownerId ID del propietario
     * @return Lista de portfolios del usuario
     */
    public List<Portfolio> getPortfoliosByOwnerId(String ownerId) {
        return portfolios.values().stream()
                .filter(p -> p.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene un activo por ID.
     * 
     * @param id ID del activo
     * @return Asset o null si no existe
     */
    public Asset getAssetById(String id) {
        return assets.get(id);
    }
    
    /**
     * Busca un activo por símbolo (case-insensitive).
     * 
     * @param symbol Símbolo a buscar (ej: "AAPL", "aapl")
     * @return Asset o null si no existe
     */
    public Asset getAssetBySymbol(String symbol) {
        return assets.values().stream()
                .filter(a -> a.getSymbol().equalsIgnoreCase(symbol))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Obtiene los activos de un portfolio.
     * 
     * @param portfolioId ID del portfolio
     * @return Lista de activos (vacía si el portfolio no existe)
     */
    public List<Asset> getAssetsByPortfolioId(String portfolioId) {
        Portfolio portfolio = portfolios.get(portfolioId);
        return portfolio != null ? portfolio.getAssets() : new ArrayList<>();
    }
    
    /**
     * Obtiene el performance de un portfolio.
     * 
     * @param portfolioId ID del portfolio
     * @return Performance o null si el portfolio no existe
     */
    public Performance getPerformance(String portfolioId) {
        Portfolio portfolio = portfolios.get(portfolioId);
        return portfolio != null ? portfolio.getPerformance() : null;
    }
    
    // =========================================================================
    // MÉTODOS PARA MUTATIONS (crear, modificar, eliminar)
    // =========================================================================
    
    /**
     * Crea un nuevo portfolio (usado por mutations).
     * 
     * 🎓 SECCIÓN 1.2: Demuestra mutations de creación
     * 
     * @param name Nombre del portfolio
     * @param ownerId ID del propietario
     * @param ownerName Nombre del propietario
     * @return Portfolio creado
     */
    public Portfolio createPortfolio(String name, String ownerId, String ownerName) {
        // Generar ID único
        String id = "portfolio-" + UUID.randomUUID().toString().substring(0, 8);
        
        Portfolio portfolio = createPortfolio(id, name, ownerId, ownerName);
        portfolio.setTotalValue(0.0); // Inicializar en 0 para portfolios vacíos
        
        portfolios.put(id, portfolio);
        return portfolio;
    }
    
    /**
     * Añade un activo a un portfolio existente (usado por mutations).
     * 
     * 🎓 SECCIÓN 1.2: Demuestra mutations con múltiples parámetros
     * 
     * 💡 NOTA: Simula un 15% de ganancia para el precio actual
     * (en producción, se obtendría de una API de mercado)
     * 
     * @param portfolioId ID del portfolio
     * @param symbol Símbolo del activo
     * @param name Nombre del activo
     * @param type Tipo de activo
     * @param quantity Cantidad de unidades
     * @param buyPrice Precio de compra
     * @return Asset creado
     * @throws IllegalArgumentException si el portfolio no existe
     */
    public Asset addAssetToPortfolio(String portfolioId, String symbol, String name,
                                     AssetType type, Double quantity, Double buyPrice) {
        Portfolio portfolio = portfolios.get(portfolioId);
        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found: " + portfolioId);
        }
        
        // Generar ID único para el activo
        String assetId = "asset-" + UUID.randomUUID().toString().substring(0, 8);
        
        // Simular precio actual (en producción, vendría de API de mercado)
        Double currentPrice = buyPrice * 1.15; // +15% de ganancia simulada
        
        Asset asset = createAsset(assetId, symbol, name, type, quantity, buyPrice, currentPrice);
        addAssetToPortfolio(portfolio, asset);
        
        // Recalcular totales y performance
        portfolio.calculateTotalValue();
        portfolio.setPerformance(calculatePerformance(portfolio));
        
        return asset;
    }
    
    /**
     * Elimina un activo de un portfolio (usado por mutations).
     * 
     * 🎓 SECCIÓN 1.2: Demuestra mutations de eliminación
     * 
     * @param portfolioId ID del portfolio
     * @param assetId ID del activo a eliminar
     * @return true si se eliminó, false si no se encontró
     */
    public boolean removeAsset(String portfolioId, String assetId) {
        Portfolio portfolio = portfolios.get(portfolioId);
        if (portfolio == null) {
            return false;
        }
        
        // Intentar eliminar el activo del portfolio
        boolean removed = portfolio.getAssets().removeIf(a -> a.getId().equals(assetId));
        
        if (removed) {
            // Eliminar del mapa de assets
            assets.remove(assetId);
            
            // Recalcular totales y performance
            portfolio.calculateTotalValue();
            portfolio.setPerformance(calculatePerformance(portfolio));
        }
        
        return removed;
    }
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO
 * =============================================================================
 * 
 * 📊 DATOS DISPONIBLES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  Portfolios  │  2 (Growth Portfolio, Retirement Fund)                  │
 * │  Assets      │  7 (AAPL, GOOGL, BTC, VOO, BND, GLD, MSFT)              │
 * │  Usuario     │  1 (Carlos Mendoza - user-001)                          │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 🎯 MÉTODOS DE CONSULTA (para Queries):
 * - getAllPortfolios()
 * - getPortfolioById(id)
 * - getPortfoliosByOwnerId(ownerId)
 * - getAssetById(id)
 * - getAssetBySymbol(symbol)
 * - getAssetsByPortfolioId(portfolioId)
 * - getPerformance(portfolioId)
 * 
 * 🔧 MÉTODOS DE MODIFICACIÓN (para Mutations):
 * - createPortfolio(name, ownerId, ownerName)
 * - addAssetToPortfolio(portfolioId, symbol, name, type, quantity, buyPrice)
 * - removeAsset(portfolioId, assetId)
 * 
 * 💡 EN PRODUCCIÓN REEMPLAZAR POR:
 * - Spring Data JPA con PostgreSQL
 * - Spring Data MongoDB
 * - Cualquier otro repositorio de persistencia
 * 
 * =============================================================================
 */