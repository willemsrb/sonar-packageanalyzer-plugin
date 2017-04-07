package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

/**
 * Base rule implementation.
 */
public abstract class AbstractPackageAnalyzerRule implements PackageAnalyzerRule {

	private static final Logger LOGGER = Loggers.get(AbstractPackageAnalyzerRule.class);

	private final String ruleKey;
	
	/**
	 * Constructor.
	 *
	 * @param ruleKey
	 *            rule key
	 */
	protected AbstractPackageAnalyzerRule(final String ruleKey) {
		this.ruleKey = ruleKey;
	}
	
	@Override
	public final void scanModel(final SensorContext context, final String language, final Model<Location> model) {
		final ActiveRule rule = context.activeRules().findByInternalKey(Rules.getRepositoryKey(language), ruleKey);
		if(rule == null) {
			LOGGER.debug("Rule {}:{} is not active", Rules.getRepositoryKey(language), ruleKey);
			return;
		}
			
		scanModel(context, rule, model);
	}

	protected abstract void scanModel(final SensorContext context, final ActiveRule rule, final Model<Location> model);


	protected void registerIssue(SensorContext context, ActiveRule rule, Package<Location> modelPackage, String message) {
		final Location location = modelPackage.getExternal();
		if(location == null) {
			LOGGER.warn("Rule {} triggered, but package {} did not contain a location to register issues.", rule.ruleKey());
		}else {
			final NewIssue issue = context.newIssue().forRule(rule.ruleKey());
			issue.at(issue.newLocation().on(location.getOn()).at(location.getAt())
					.message(message));
			issue.save();
		}
	}
}
