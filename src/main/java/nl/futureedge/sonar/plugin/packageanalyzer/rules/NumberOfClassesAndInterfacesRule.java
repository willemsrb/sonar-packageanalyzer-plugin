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
 * Number of classes rule.
 */
public class NumberOfClassesAndInterfacesRule extends AbstractPackageAnalyzerRule implements PackageAnalyzerRule {
	
	private static final Logger LOGGER = Loggers.get(NumberOfClassesAndInterfacesRule.class);

	private static final String RULE_KEY = "number-of-classes-and-interfaces";
	private static final String PARAM_MAXIMUM = "maximum";

	/**
	 * Constructor.
	 */
	public NumberOfClassesAndInterfacesRule() {
		super(RULE_KEY);
		LOGGER.info("Instantiating rule");
	}

	/**
	 * Define the rule.
	 *
	 * @param repository
	 */
	@Override
	public void define(final NewRepository repository) {
		LOGGER.info("Defining rule in repostiory {}", repository.key());
		final NewRule numberOfClassesAndInterfacesRule = repository.createRule(RULE_KEY)
				.setName("Number of Classes and Interfaces").setHtmlDescription(
						"The number of concrete and abstract classes (and interfaces) in the package is an indicator of the extensibility of the package.");
		numberOfClassesAndInterfacesRule.createParam(PARAM_MAXIMUM).setName(PARAM_MAXIMUM)
				.setDescription("Maximum number of classes and interfaces allowed in the package")
				.setType(RuleParamType.INTEGER).setDefaultValue("50");
	}

	@Override
	public void scanModel(final SensorContext context, final ActiveRule rule, final Model<Location> model) {
		final Integer maximum = Integer.valueOf(rule.param(PARAM_MAXIMUM));

		for (final Package<Location> packageToCheck : model.getPackages()) {
			final int classcount = packageToCheck.getClasses().size();

			if (classcount > maximum) {
				registerIssue(context, rule, packageToCheck, "Reduce number of classes in package (allowed: " + maximum + ", actual: " + classcount +")");
			}
		}
	}
}
