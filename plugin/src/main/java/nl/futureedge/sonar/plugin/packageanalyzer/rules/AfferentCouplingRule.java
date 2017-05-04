package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

/**
 * Afferent coupling rule.
 */
public final class AfferentCouplingRule extends AbstractPackageAnalyzerRule implements PackageAnalyzerRule {

	private static final Logger LOGGER = Loggers.get(AfferentCouplingRule.class);

	private static final String RULE_KEY = "afferent-coupling";
	private static final String PARAM_MAXIMUM = "maximum";

	private final Settings settings;

	/**
	 * Afferent coupling rule.
	 */
	public AfferentCouplingRule(final Settings settings) {
		super(RULE_KEY);
		this.settings = settings;
	}

	@Override
	public void define(final NewRepository repository) {
		LOGGER.debug("Defining rule in repostiory {}", repository.key());
		final NewRule afferentCouplingsRule = repository.createRule(RULE_KEY).setType(RuleType.CODE_SMELL)
				.setSeverity(Severity.MAJOR).setName("Afferent Coupling").setHtmlDescription(
						"The number of other packages that depend upon classes within the package is an indicator of the package's responsibility.");
		afferentCouplingsRule.createParam(PARAM_MAXIMUM).setName(PARAM_MAXIMUM)
				.setDescription("Maximum number of other packages allowed to depend upon classes within the package")
				.setType(RuleParamType.INTEGER).setDefaultValue("25");
	}

	@Override
	public void scanModel(final SensorContext context, final ActiveRule rule, final Model<Location> model) {
		final Integer maximum = Integer.valueOf(rule.param(PARAM_MAXIMUM));

		for (final Package<Location> packageToCheck : model.getPackages()) {
			final int afferentCoupling = packageToCheck.getUsedByPackages().size();

			LOGGER.debug("Package {}: afferent={}", packageToCheck.getName(), afferentCoupling);

			if (afferentCoupling > maximum) {
				final Set<Class<Location>> classes = selectClassesWithAfferentUsage(packageToCheck.getClasses());

				registerIssue(context, settings, rule, packageToCheck, classes,
						"Reduce number of packages that use this package (allowed: " + maximum + ", actual: "
								+ afferentCoupling + ")");
			}
		}
	}

	/**
	 * Only select classes that are used by classes outside this package.
	 * 
	 * @param packageClasses
	 *            package classes
	 * @return classes that have afferent usages
	 */
	private static Set<Class<Location>> selectClassesWithAfferentUsage(
			final SortedSet<Class<Location>> packageClasses) {
		final Set<Class<Location>> result = new HashSet<>();

		for (final Class<Location> packageClass : packageClasses) {
			if (hasAfferentUsage(packageClass)) {
				result.add(packageClass);
			}
		}

		return result;
	}

	private static boolean hasAfferentUsage(final Class<Location> packageClass) {
		final Package<Location> classPackage = packageClass.getParentPackage();

		for (final Class<Location> usedByClass : packageClass.getUsedByClasses()) {
			if (!classPackage.equals(usedByClass.getParentPackage())) {
				return true;
			}
		}

		return false;
	}
}
