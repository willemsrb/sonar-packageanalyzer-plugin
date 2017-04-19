package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
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
 * Abstractness rule.
 */
public class AbstractnessRule extends AbstractPackageAnalyzerRule implements PackageAnalyzerRule {

	private static final Logger LOGGER = Loggers.get(InstabilityRule.class);

	private static final String RULE_KEY = "abstractness";
	private static final String PARAM_MAXIMUM = "maximum";

	/**
	 * Abstractness rule.
	 */
	public AbstractnessRule() {
		super(RULE_KEY);
	}

	@Override
	public void define(final NewRepository repository) {
		LOGGER.debug("Defining rule in repostiory {}", repository.key());
		final NewRule abstractnessRule = repository.createRule(RULE_KEY).setType(RuleType.CODE_SMELL)
				.setSeverity(Severity.MAJOR).setName("Abstractness").setHtmlDescription(
						"The ratio of the number of abstract classes (and interfaces) in the analyzed package compared to the total number of classes in the analyzed package.<br/>"
								+ "The range for this metric is 0% to 100%, with A=0% indicating a completely concrete package and A=100% indicating a completely abstract package.");
		abstractnessRule.createParam(PARAM_MAXIMUM).setName(PARAM_MAXIMUM)
				.setDescription("Maximum abstractness of a package allowed").setType(RuleParamType.INTEGER)
				.setDefaultValue("75");
	}

	@Override
	public void scanModel(final SensorContext context, final ActiveRule rule, final Model<Location> model) {
		final Integer maximum = Integer.valueOf(rule.param(PARAM_MAXIMUM));

		for (final Package<Location> packageToCheck : model.getPackages()) {
			final int abstractClasses = (int) packageToCheck.getClasses().stream().filter(Class::isAbstract).count();
			final int totalClasses = packageToCheck.getClasses().size();
			final int abstractness = totalClasses == 0 ? 0 : (abstractClasses * 100 / totalClasses);

			LOGGER.debug("Package {}: abstract={}, total={}, abstractness={}", packageToCheck.getName(),
					abstractClasses, totalClasses, abstractness);

			if (abstractness > maximum) {
				registerIssue(context, rule, packageToCheck,
						"Reduce number of abstract classes in this package (allowed: " + maximum + "%, actual: "
								+ abstractness + "%)");
			}
		}
	}
}
