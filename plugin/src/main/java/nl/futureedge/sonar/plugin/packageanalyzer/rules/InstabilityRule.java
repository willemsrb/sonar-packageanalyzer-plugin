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

import nl.futureedge.sonar.plugin.packageanalyzer.settings.PackageAnalyzerProperties;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

/**
 * Instability rule.
 */
public final class InstabilityRule extends AbstractPackageAnalyzerRule implements PackageAnalyzerRule {

	private static final Logger LOGGER = Loggers.get(InstabilityRule.class);

	private static final String RULE_KEY = "instability";
	private static final String PARAM_MAXIMUM = "maximum";

	private final Settings settings;

	/**
	 * Instability rule.
	 */
	public InstabilityRule(final Settings settings) {
		super(RULE_KEY);
		this.settings = settings;
	}

	@Override
	public void define(final NewRepository repository) {
		LOGGER.debug("Defining rule in repostiory {}", repository.key());
		final NewRule instabilityRule = repository.createRule(RULE_KEY).setType(RuleType.CODE_SMELL)
				.setSeverity(Severity.MAJOR).setName("Instability").setHtmlDescription(
						"The ratio of efferent coupling (Ce) to total coupling (Ce + Ca) such that I = Ce / (Ce + Ca). This metric is an indicator of the package's resilience to change.<br/>"
								+ "The range for this metric is 0 to 100%, with I=0% indicating a completely stable package and I=100% indicating a completely instable package.");
		//Remediation times
		if(PackageAnalyzerProperties.shouldRegisterOnClasses(settings) && PackageAnalyzerProperties.shouldRegisterOnAllClasses(settings))
				instabilityRule.setDebtRemediationFunction(instabilityRule.debtRemediationFunctions().constantPerIssue("15min"));
			else instabilityRule.setDebtRemediationFunction(instabilityRule.debtRemediationFunctions().linearWithOffset("7min", "1h"));
			instabilityRule.setGapDescription("for each class inside the package.");
		//Maximum instability allowed in a package	
		instabilityRule.createParam(PARAM_MAXIMUM).setName(PARAM_MAXIMUM)
				.setDescription("Maximum instability (%) of a package allowed").setType(RuleParamType.INTEGER)
				.setDefaultValue("75");
	}

	@Override
	public void scanModel(final SensorContext context, final ActiveRule rule, final Model<Location> model) {
		final Integer maximum = Integer.valueOf(rule.param(PARAM_MAXIMUM));

		for (final Package<Location> packageToCheck : model.getPackages()) {
			final int afferentCoupling = packageToCheck.getUsedByPackages().size();
			final int efferentCoupling = packageToCheck.getPackageUsages().size();
			final int totalCoupling = efferentCoupling + afferentCoupling;
			final int instability = totalCoupling == 0 ? 0 : (efferentCoupling * 100) / totalCoupling;

			LOGGER.debug("Package {}: efferent={}, total={}, instability={}", packageToCheck.getName(),
					efferentCoupling, totalCoupling, instability);

			if (instability > maximum) {
				final Set<Class<Location>> classes = EfferentCouplingRule
						.selectClassesWithEfferentUsage(packageToCheck.getClasses());

				registerIssue(context, settings, rule, packageToCheck, classes,
						"Reduce number of packages used by this package to lower instability (allowed: " + maximum
								+ "%, actual: " + instability + "%)");
			}
		}
	}
}
