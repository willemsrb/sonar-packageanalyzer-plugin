package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

/**
 * Efferent coupling rule.
 */
public final class EfferentCouplingRule extends AbstractPackageAnalyzerRule implements PackageAnalyzerRule {
	
	private static final Logger LOGGER = Loggers.get(EfferentCouplingRule.class);

	private static final String RULE_KEY = "efferent-coupling";
	private static final String PARAM_MAXIMUM = "maximum";

	/**
	 * Efferent coupling rule.
	 */
	public EfferentCouplingRule() {
		super(RULE_KEY);
	}

	@Override
	public void define(final NewRepository repository) {
		LOGGER.info("Defining rule in repostiory {}", repository.key());
		final NewRule efferentCouplingRule = repository.createRule(RULE_KEY).setName("Efferent Coupling")
				.setHtmlDescription(
						"The number of other packages that the classes in the package depend upon is an indicator of the package's independence.");
		efferentCouplingRule.createParam(PARAM_MAXIMUM).setName(PARAM_MAXIMUM)
				.setDescription(
						"Maximum number of other packages that the classes in the package are allowed to depend upon")
				.setType(RuleParamType.INTEGER).setDefaultValue("25");
	}

	@Override
	public void scanModel(final SensorContext context, final ActiveRule rule, final Model<Location> model) {
		final Integer maximum = Integer.valueOf(rule.param(PARAM_MAXIMUM));

		for (final Package<Location> packageToCheck : model.getPackages()) {
			final int efferentCoupling = packageToCheck.getPackageUsages().size();

			LOGGER.debug("Package {}: efferent={}", packageToCheck.getName(), efferentCoupling);

			if (efferentCoupling > maximum) {
				registerIssue(context, rule, packageToCheck, "Reduce number of packages used by this package (allowed: "
						+ maximum + ", actual: " + efferentCoupling + ")");
			}
		}
	}
}
