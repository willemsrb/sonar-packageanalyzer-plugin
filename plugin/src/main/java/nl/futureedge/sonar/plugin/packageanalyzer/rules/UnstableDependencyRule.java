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
 * Unstable Dependency.
 */
public final class UnstableDependencyRule extends AbstractPackageAnalyzerRule implements PackageAnalyzerRule {

	private static final Logger LOGGER = Loggers.get(UnstableDependencyRule.class);

	private static final String RULE_KEY = "UnstableDependency";
	private static final String PARAM_MAXIMUM = "maximum";

	private final Settings settings;

	/**
	 * Unstable Dependency rule.
	 */
	public UnstableDependencyRule(final Settings settings) {
		super(RULE_KEY);
		this.settings = settings;
	}

	@Override
	public void define(final NewRepository repository) {
		LOGGER.debug("Defining rule in repository {}", repository.key());
		final NewRule unstableDependencyRule = repository.createRule(RULE_KEY).setType(RuleType.CODE_SMELL)
				.setSeverity(Severity.MAJOR).setName("Unstable Dependency").setGapDescription("for each class inside the package.")
				.setHtmlDescription("Stable-dependencies principle states that if a package "
				+ "references another that is more likely to change and therefore less stable, there is a great chance that "
				+ "this package also needs to be updated. This rule describes a package that depends on other packages that are less stable than itself.");
		//Maximum unstableDependenciesRatio allowed in a package	
		unstableDependencyRule.createParam(PARAM_MAXIMUM).setName(PARAM_MAXIMUM)
				.setDescription("Maximum ratio(%) between unstable dependencies and total dependencies to other packages allowed").setType(RuleParamType.INTEGER)
				.setDefaultValue("30");
				
		defineRemediationTimes(unstableDependencyRule);
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
			final Set<Package<Location>> afferentPackages = packageToCheck.getUsedByPackages();
			final int afferentCoupling = afferentPackages.size();
			final int efferentCoupling = packageToCheck.getPackageUsages().size();
			final int totalCoupling = efferentCoupling + afferentCoupling;
			final int instability = totalCoupling == 0 ? 0 : (efferentCoupling * 100) / totalCoupling;
			
			int unstableDependencies = 0;
			for (final Package<Location> packageToCompare : afferentPackages) {
				final int inst = InstabilityRule.calcInstability(packageToCompare);
				if (instability < inst) 
					unstableDependencies++;
			}
			
			final int unstableDependenciesRatio = totalCoupling == 0 ? 0 : (unstableDependencies * 100) / totalCoupling;
			if (unstableDependenciesRatio > maximum) {
				LOGGER.debug("Package with unstableDependencies {}: totalCoupling={}, unstableDependencies={}, unstableDependenciesRatio={}%", 
					packageToCheck.getName(), totalCoupling, unstableDependencies, unstableDependenciesRatio);
				final Set<Class<Location>> classes = AfferentCouplingRule.selectClassesWithAfferentUsage(packageToCheck.getClasses());
				registerIssue(context, settings, rule, packageToCheck, classes, 
					"The ratio between unstable dependencies and the total number of dependencies is too high (allowed: " +
					maximum + "%, actual: " + unstableDependenciesRatio + "%)");
			}
		}
	}
}
