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

import nl.futureedge.sonar.plugin.packageanalyzer.settings.PackageAnalyzerProperties;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

/**
 * Efferent coupling rule.
 */
public final class EfferentCouplingRule extends AbstractPackageAnalyzerRule implements PackageAnalyzerRule {

	private static final Logger LOGGER = Loggers.get(EfferentCouplingRule.class);

	private static final String RULE_KEY = "efferent-coupling";
	private static final String PARAM_MAXIMUM = "maximum";

	private final Settings settings;

	/**
	 * Efferent coupling rule.
	 */
	public EfferentCouplingRule(final Settings settings) {
		super(RULE_KEY);
		this.settings = settings;
	}

	@Override
	public void define(final NewRepository repository) {
		LOGGER.debug("Defining rule in repostiory {}", repository.key());
		final NewRule efferentCouplingRule = repository.createRule(RULE_KEY).setType(RuleType.CODE_SMELL)
				.setSeverity(Severity.MAJOR).setGapDescription("for each class inside the package.").setName("Efferent Coupling").setHtmlDescription(
						"The number of other packages that the classes in the package depend upon is an indicator of the package's independence.");
		//The number of classes in other packages that the classes in a package depend upon
		efferentCouplingRule.createParam(PARAM_MAXIMUM).setName(PARAM_MAXIMUM)
				.setDescription("Maximum number of other packages that the classes in the package are allowed to depend upon")
				.setType(RuleParamType.INTEGER).setDefaultValue("25");
		
		defineRemediationTimes(efferentCouplingRule);
	}

	@Override
	public void defineRemediationTimes(final NewRule rule) {
		if(!PackageAnalyzerProperties.shouldRegisterOnPackage(settings) && PackageAnalyzerProperties.shouldRegisterOnAllClasses(settings))
			rule.setDebtRemediationFunction(rule.debtRemediationFunctions().linearWithOffset("12min", "0min"));
		else rule.setDebtRemediationFunction(rule.debtRemediationFunctions().linearWithOffset("5min", "45min"));
	}
	
	@Override
	public void scanModel(final SensorContext context, final ActiveRule rule, final Model<Location> model) {
		final Integer maximum = Integer.valueOf(rule.param(PARAM_MAXIMUM));

		for (final Package<Location> packageToCheck : model.getPackages()) {
			final int efferentCoupling = packageToCheck.getPackageUsages().size();

			LOGGER.debug("Package {}: efferent={}", packageToCheck.getName(), efferentCoupling);

			if (efferentCoupling > maximum) {
				final Set<Class<Location>> classes = selectClassesWithEfferentUsage(packageToCheck.getClasses());

				registerIssue(context, settings, rule, packageToCheck, classes,
						"Reduce number of packages used by this package (allowed: " + maximum + ", actual: "
								+ efferentCoupling + ")");
			}
		}
	}

	/**
	 * Only select classes that are usee classes outside this package.
	 * 
	 * @param packageClasses
	 *            package classes
	 * @return classes that have efferent usages
	 */
	static Set<Class<Location>> selectClassesWithEfferentUsage(final SortedSet<Class<Location>> packageClasses) {
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

		for (final Class<Location> usesClass : packageClass.getUsages()) {
			if (!classPackage.equals(usesClass.getParentPackage())) {
				return true;
			}
		}

		return false;
	}
}
