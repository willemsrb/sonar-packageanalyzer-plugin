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

import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

public class DistanceFromMainSequenceRule extends AbstractPackageAnalyzerRule implements PackageAnalyzerRule {
	
	private static final Logger LOGGER = Loggers.get(UnstableDependencyRule.class);

	private static final String RULE_KEY = "distanceFromMainSequence";
	private static final String PARAM_MAXIMUM = "maximum";
	
	/**
	 * Distance from main sequence rule.
	 */
	public DistanceFromMainSequenceRule(final Settings settings) {
		super(RULE_KEY, settings);
	}
	
	@Override
	public void define(final NewRepository repository) {
		LOGGER.debug("Defining rule in repository {}", repository.key());
		final NewRule DistanceFromMainSequenceRule = repository.createRule(RULE_KEY).setType(RuleType.CODE_SMELL)
				.setSeverity(Severity.MAJOR).setName("Distance From Main Sequence").setGapDescription("for each related class inside the package.")
				.setHtmlDescription("This metric has the primary rationale where abstractness and stability of packages should be closely connected."
				+ "The Perpendicular Distance of a Package from the Idealized Line D = A + I - 1, has the ideal value of D = 0. <br/>"
				+ "Instability is associated with a package that depends on outter ones, and therefore any changes on these may cause changes on said package. <br/>"
				+ "Thus, this rule suggests that the more abstract a package is, the more stable it should be, and vice versa.");
		//Maximum unstableDependenciesRatio allowed in a package	
		DistanceFromMainSequenceRule.createParam(PARAM_MAXIMUM).setName(PARAM_MAXIMUM)
				.setDescription("Maximum Distance From Main Sequence(%) of a package allowed").setType(RuleParamType.INTEGER)
				.setDefaultValue("70");
				
		defineRemediationTimes(DistanceFromMainSequenceRule);
	}
	
	@Override
	public void scanModel(final SensorContext context, final ActiveRule rule, final Model<Location> model) {
		final Integer maximum = Integer.valueOf(rule.param(PARAM_MAXIMUM));

		for (final Package<Location> packageToCheck : model.getPackages()) {
			final int instability = InstabilityRule.calcInstability(packageToCheck);
			final int abstractness = AbstractnessRule.calcAbstractness(packageToCheck);
			final int distance = Math.abs(abstractness + instability - 100);

			if (distance > maximum) {
				final Set<Class<Location>> classes = packageToCheck.getClasses().stream().filter(Class::isAbstract)
						.collect(Collectors.toSet());
				classes.addAll(EfferentCouplingRule.selectClassesWithEfferentUsage(packageToCheck.getClasses()));
				
				registerIssue(context, getSettings(), rule, packageToCheck, classes, 
						"Distance from main sequence value is too high (" + distance + "%)" + ", consider refactoring the package in order to lower it"
						+ " (values for instability: " + instability + "%" + ", abstractness: " + abstractness + "%).");
			}
		}
	}

}
