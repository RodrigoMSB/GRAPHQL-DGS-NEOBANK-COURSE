package com.neobank.cashback.datafetcher;

import com.neobank.cashback.domain.CashbackRule;
import com.neobank.cashback.domain.Reward;
import com.neobank.cashback.domain.RewardStatus;
import com.neobank.cashback.domain.RewardTier;
import com.neobank.cashback.domain.TransactionCategory;
import com.neobank.cashback.domain.User;
import com.neobank.cashback.repository.CashbackRuleRepository;
import com.neobank.cashback.repository.RewardRepository;
import com.neobank.cashback.repository.UserRepository;
import com.neobank.cashback.service.CashbackService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SECCIÓN 3.3: Implementación de resolvers con @DgsData
 * 
 * Este DataFetcher maneja todas las QUERIES del schema.
 * 
 * @DgsComponent: Marca esta clase como un componente DGS (similar a @Component
 *                de Spring)
 * @DgsQuery: Indica que este método resuelve una query GraphQL
 * @InputArgument: Inyecta argumentos de la query
 * 
 *                 DGS hace el binding automático entre: - Nombre del método
 *                 GraphQL -> nombre del método Java (o se especifica
 *                 en @DgsQuery) - Argumentos GraphQL -> parámetros Java - Tipos
 *                 de retorno GraphQL -> tipos Java
 */
@DgsComponent
public class QueryDataFetcher {

	private final UserRepository userRepository;
	private final RewardRepository rewardRepository;
	private final CashbackRuleRepository ruleRepository;
	private final CashbackService cashbackService;

	public QueryDataFetcher(UserRepository userRepository, RewardRepository rewardRepository,
			CashbackRuleRepository ruleRepository, CashbackService cashbackService) {
		this.userRepository = userRepository;
		this.rewardRepository = rewardRepository;
		this.ruleRepository = ruleRepository;
		this.cashbackService = cashbackService;
	}

	/**
	 * Query: user(id: ID!): User
	 * 
	 * Obtiene un usuario por su ID.
	 */
	@DgsQuery
	public User user(@InputArgument String id) {
		return userRepository.findById(id).orElse(null);
	}

	/**
	 * Query: usersByTier(tier: RewardTier!): [User!]!
	 * 
	 * Filtra usuarios por tier.
	 */
	@DgsQuery
	public List<User> usersByTier(@InputArgument RewardTier tier) {
		return userRepository.findByTier(tier);
	}

	/**
	 * Query: reward(id: ID!): Reward
	 * 
	 * Obtiene una reward por su ID.
	 */
	@DgsQuery
	public Reward reward(@InputArgument String id) {
		return rewardRepository.findById(id).orElse(null);
	}

	/**
	 * Query: rewards(filter: RewardsFilterInput): [Reward!]!
	 * 
	 * Lista rewards con filtros opcionales. Demuestra el uso de input types
	 * complejos.
	 */
	@DgsQuery
	public List<Reward> rewards(@InputArgument RewardsFilterInput filter) {
		List<Reward> allRewards = rewardRepository.findAll();

		if (filter == null) {
			return allRewards;
		}

		return allRewards.stream().filter(r -> filter.getUserId() == null || r.getUserId().equals(filter.getUserId()))
				.filter(r -> filter.getStatus() == null || r.getStatus() == filter.getStatus())
				.filter(r -> filter.getCategory() == null || r.getCategory() == filter.getCategory())
				.filter(r -> filter.getMinAmount() == null || r.getAmount().compareTo(filter.getMinAmount()) >= 0)
				.filter(r -> filter.getMaxAmount() == null || r.getAmount().compareTo(filter.getMaxAmount()) <= 0)
				.filter(r -> filter.getEarnedAfter() == null || r.getEarnedAt().isAfter(filter.getEarnedAfter()))
				.filter(r -> filter.getEarnedBefore() == null || r.getEarnedAt().isBefore(filter.getEarnedBefore()))
				.collect(Collectors.toList());
	}

	/**
	 * Query: userRewards(userId: ID!, status: RewardStatus): [Reward!]!
	 * 
	 * Obtiene rewards de un usuario, opcionalmente filtradas por estado.
	 */
	@DgsQuery
	public List<Reward> userRewards(@InputArgument String userId,
			@InputArgument(name = "status") RewardStatus rewardStatus) {
		if (rewardStatus != null) {
			return rewardRepository.findByUserIdAndStatus(userId, rewardStatus);
		}
		return rewardRepository.findByUserId(userId);
	}

	/**
	 * Query: rewardsSummary(userId: ID!): RewardsSummary
	 * 
	 * Genera un resumen agregado de las rewards del usuario. Demuestra queries
	 * complejas con cálculos.
	 */
	@DgsQuery
	public RewardsSummary rewardsSummary(@InputArgument String userId) {
		List<Reward> userRewards = rewardRepository.findByUserId(userId);

		BigDecimal totalEarned = userRewards.stream().map(Reward::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalRedeemed = userRewards.stream().filter(r -> r.getStatus() == RewardStatus.REDEEMED)
				.map(Reward::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalExpired = userRewards.stream().filter(r -> r.getStatus() == RewardStatus.EXPIRED)
				.map(Reward::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal availableBalance = userRewards.stream().filter(r -> r.getStatus() == RewardStatus.ACTIVE)
				.map(Reward::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

		// Agrupar por categoría
		List<CategorySummary> byCategory = userRewards.stream().collect(Collectors.groupingBy(Reward::getCategory))
				.entrySet().stream().map(entry -> {
					BigDecimal total = entry.getValue().stream().map(Reward::getAmount).reduce(BigDecimal.ZERO,
							BigDecimal::add);
					return new CategorySummary(entry.getKey(), total, entry.getValue().size());
				}).collect(Collectors.toList());

		// Agrupar por estado
		List<StatusSummary> byStatus = userRewards.stream().collect(Collectors.groupingBy(Reward::getStatus)).entrySet()
				.stream().map(entry -> {
					BigDecimal total = entry.getValue().stream().map(Reward::getAmount).reduce(BigDecimal.ZERO,
							BigDecimal::add);
					return new StatusSummary(entry.getKey(), total, entry.getValue().size());
				}).collect(Collectors.toList());

		return new RewardsSummary(userId, totalEarned, totalRedeemed, totalExpired, availableBalance, byCategory, byStatus);
	}

	/**
	 * Query: cashbackRules: [CashbackRule!]!
	 * 
	 * Lista todas las reglas de cashback.
	 */
	@DgsQuery
	public List<CashbackRule> cashbackRules() {
		return ruleRepository.findAll();
	}

	/**
	 * Query: cashbackRule(category: TransactionCategory!): CashbackRule
	 * 
	 * Obtiene la regla de una categoría específica.
	 */
	@DgsQuery
	public CashbackRule cashbackRule(@InputArgument TransactionCategory category) {
		return ruleRepository.findByCategory(category).orElse(null);
	}

	/**
	 * Query: calculateCashback(userId: ID!, transactionAmount: Money!, category:
	 * TransactionCategory!): Money!
	 * 
	 * Calcula cuánto cashback se otorgaría por una transacción hipotética. Útil
	 * para mostrar al usuario cuánto ganará antes de hacer la compra.
	 */
	@DgsQuery
	public BigDecimal calculateCashback(@InputArgument String userId, @InputArgument BigDecimal transactionAmount,
			@InputArgument TransactionCategory category) {
		return cashbackService.calculateCashback(userId, transactionAmount, category);
	}
}

/**
 * Input type para filtrar rewards.
 * 
 * DGS genera automáticamente estas clases desde el schema, pero aquí las
 * definimos manualmente para mayor control.
 */
class RewardsFilterInput {
	private String userId;
	private RewardStatus status;
	private TransactionCategory category;
	private BigDecimal minAmount;
	private BigDecimal maxAmount;
	private LocalDateTime earnedAfter;
	private LocalDateTime earnedBefore;

	// Getters
	public String getUserId() {
		return userId;
	}

	public RewardStatus getStatus() {
		return status;
	}

	public TransactionCategory getCategory() {
		return category;
	}

	public BigDecimal getMinAmount() {
		return minAmount;
	}

	public BigDecimal getMaxAmount() {
		return maxAmount;
	}

	public LocalDateTime getEarnedAfter() {
		return earnedAfter;
	}

	public LocalDateTime getEarnedBefore() {
		return earnedBefore;
	}
}

/**
 * Clases auxiliares para el summary
 */
class CategorySummary {
	private TransactionCategory category;
	private BigDecimal totalAmount;
	private Integer count;

	public CategorySummary(TransactionCategory category, BigDecimal totalAmount, Integer count) {
		this.category = category;
		this.totalAmount = totalAmount;
		this.count = count;
	}

	public TransactionCategory getCategory() {
		return category;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public Integer getCount() {
		return count;
	}
}

class StatusSummary {
	private RewardStatus status;
	private BigDecimal totalAmount;
	private Integer count;

	public StatusSummary(RewardStatus status, BigDecimal totalAmount, Integer count) {
		this.status = status;
		this.totalAmount = totalAmount;
		this.count = count;
	}

	public RewardStatus getStatus() {
		return status;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public Integer getCount() {
		return count;
	}
}

class RewardsSummary {
	private String userId;
	private BigDecimal totalEarned;
	private BigDecimal totalRedeemed;
	private BigDecimal totalExpired;
	private BigDecimal availableBalance;
	private List<CategorySummary> rewardsByCategory;
	private List<StatusSummary> rewardsByStatus;

	public RewardsSummary(String userId, BigDecimal totalEarned, BigDecimal totalRedeemed, BigDecimal totalExpired,
			BigDecimal availableBalance, List<CategorySummary> rewardsByCategory, List<StatusSummary> rewardsByStatus) {
		this.userId = userId;
		this.totalEarned = totalEarned;
		this.totalRedeemed = totalRedeemed;
		this.totalExpired = totalExpired;
		this.availableBalance = availableBalance;
		this.rewardsByCategory = rewardsByCategory;
		this.rewardsByStatus = rewardsByStatus;
	}

	// Getters
	public String getUserId() {
		return userId;
	}

	public BigDecimal getTotalEarned() {
		return totalEarned;
	}

	public BigDecimal getTotalRedeemed() {
		return totalRedeemed;
	}

	public BigDecimal getTotalExpired() {
		return totalExpired;
	}

	public BigDecimal getAvailableBalance() {
		return availableBalance;
	}

	public List<CategorySummary> getRewardsByCategory() {
		return rewardsByCategory;
	}

	public List<StatusSummary> getRewardsByStatus() {
		return rewardsByStatus;
	}
}