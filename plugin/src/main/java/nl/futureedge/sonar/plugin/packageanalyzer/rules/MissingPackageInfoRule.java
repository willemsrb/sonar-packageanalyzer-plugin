package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

/**
 * Missing package-info.java rule.
 */
public final class MissingPackageInfoRule extends AbstractPackageAnalyzerRule implements PackageAnalyzerRule {

	private static final Logger LOGGER = Loggers.get(MissingPackageInfoRule.class);

	private static final String RULE_KEY = "missing-package-info";

	/**
	 * Missing package-info.java rule.
	 */
	public MissingPackageInfoRule() {
		super(RULE_KEY);
	}

	@Override
	public void define(final NewRepository repository) {
		LOGGER.debug("Defining rule in repostiory {}", repository.key());
		repository.createRule(RULE_KEY).setType(RuleType.CODE_SMELL).setSeverity(Severity.BLOCKER)
				.setName("Missing package-info.java").setHtmlDescription(
						"When a package-info.java file is missing, no issues can be reported on the package level.");
	}
	
	@Override
	public void defineRemediationTimes(NewRule rule) {
		// Unused
	}

	@Override
	public boolean supportsLanguage(final String language) {
		return "java".equals(language);
	}

	@Override
	public void scanModel(final SensorContext context, final ActiveRule rule, final Model<Location> model) {
		for (final Package<Location> packageToCheck : model.getPackages()) {
			LOGGER.debug("Package {}: extenal={}", packageToCheck.getExternal());

			if (packageToCheck.getExternal() == null) {
				for (final Class<Location> classToReport : packageToCheck.getClasses()) {
					registerIssue(context, rule, classToReport, "Add a package-info.java to the package.");
				}
			}
		}
	}
}
