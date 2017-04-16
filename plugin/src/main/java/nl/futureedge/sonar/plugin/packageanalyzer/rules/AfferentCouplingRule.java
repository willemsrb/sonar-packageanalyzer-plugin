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
 * Afferent coupling rule.
 */
public class AfferentCouplingRule extends AbstractPackageAnalyzerRule implements PackageAnalyzerRule {

	private static final Logger LOGGER = Loggers.get(EfferentCouplingRule.class);

	private static final String RULE_KEY = "afferent-coupling";
	private static final String PARAM_MAXIMUM = "maximum";

	/**
	 * Afferent coupling rule.
	 */
	public AfferentCouplingRule() {
		super(RULE_KEY);
	}

	@Override
	public void define(final NewRepository repository) {
		final NewRule afferentCouplingsRule = repository.createRule(RULE_KEY).setName("Afferent Coupling")
				.setHtmlDescription(
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
				registerIssue(context, rule, packageToCheck,
						"Reduce number of packages that use this package (allowed: " + maximum + ", actual: "
								+ afferentCoupling + ")");
			}
		}
	}
}
