package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import java.util.Set;
import java.util.stream.Collectors;

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
 * Abstractness rule.
 */
public final class AbstractnessRule extends AbstractPackageAnalyzerRule implements PackageAnalyzerRule {

	private static final Logger LOGGER = Loggers.get(AbstractnessRule.class);

	private static final String RULE_KEY = "abstractness";
	private static final String PARAM_MAXIMUM = "maximum";

	private final Settings settings;

	/**
	 * Abstractness rule.
	 */
	public AbstractnessRule(final Settings settings) {
		super(RULE_KEY);
		this.settings = settings;
	}

	@Override
	public void define(final NewRepository repository) {
		LOGGER.debug("Defining rule in repostiory {}", repository.key());
		final NewRule abstractnessRule = repository.createRule(RULE_KEY).setType(RuleType.CODE_SMELL)
				.setSeverity(Severity.MAJOR).setGapDescription("for each class inside the package.").setName("Abstractness").setHtmlDescription(
						"The ratio of the number of abstract classes (and interfaces) in the analyzed package compared to the total number of classes in the analyzed package.<br/>"
								+ "The range for this metric is 0% to 100%, with A=0% indicating a completely concrete package and A=100% indicating a completely abstract package.");
		//Maximum abstractness allowed in a package	
		abstractnessRule.createParam(PARAM_MAXIMUM).setName(PARAM_MAXIMUM)
			.setDescription("Maximum abstractness of a package allowed").setType(RuleParamType.INTEGER)
			.setDefaultValue("75");
		
		defineRemediationTimes(abstractnessRule);
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
			final Set<Class<Location>> classes = packageToCheck.getClasses().stream().filter(Class::isAbstract)
					.collect(Collectors.toSet());
			final int abstractClasses = classes.size();
			final int totalClasses = packageToCheck.getClasses().size();
			final int abstractness = totalClasses == 0 ? 0 : (abstractClasses * 100 / totalClasses);

			LOGGER.debug("Package {}: abstract={}, total={}, abstractness={}", packageToCheck.getName(),
					abstractClasses, totalClasses, abstractness);

			if (abstractness > maximum) {
				registerIssue(context, settings, rule, packageToCheck, classes,
						"Reduce number of abstract classes in this package (allowed: " + maximum + "%, actual: "
								+ abstractness + "%)");
			}
		}
	}
}
