package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import java.util.Set;

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
				.setSeverity(Severity.MAJOR).setName("Efferent Coupling").setHtmlDescription(
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
				// TODO: only select classes that use classes outside this package
				final Set<Class<Location>> classes = packageToCheck.getClasses();

				registerIssue(context, settings, rule, packageToCheck, classes,
						"Reduce number of packages used by this package (allowed: " + maximum + ", actual: "
								+ efferentCoupling + ")");
			}
		}
	}
}
