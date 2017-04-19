package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import java.io.Serializable;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.measures.Metric;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import nl.futureedge.sonar.plugin.packageanalyzer.model.External;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;

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
		final ActiveRule rule = context.activeRules().find(RuleKey.of(BaseRules.getRepositoryKey(language), ruleKey));
		if (rule == null) {
			LOGGER.info("Rule {}:{} is not active", BaseRules.getRepositoryKey(language), ruleKey);
			return;
		}

		scanModel(context, rule, model);
	}

	protected abstract void scanModel(final SensorContext context, final ActiveRule rule, final Model<Location> model);

	/**
	 * Register an issue.
	 * 
	 * @param context
	 *            sensor context
	 * @param rule
	 *            rule to register issue for
	 * @param model
	 *            object to register issue on (external location)
	 * @param message
	 *            message
	 */
	protected void registerIssue(SensorContext context, ActiveRule rule, External<Location> model, String message) {
		final Location location = model.getExternal();
		if (location == null) {
			LOGGER.warn("Rule {} triggered, but {} did not contain a location to register issues.", rule.ruleKey(),
					model);
		} else {
			final NewIssue issue = context.newIssue().forRule(rule.ruleKey());
			issue.at(issue.newLocation().on(location.getOn()).at(location.getAt()).message(message));
			issue.save();
		}
	}

	/**
	 * Register a measure.
	 * 
	 * @param context
	 *            sensor context
	 * @param metric
	 *            metric to register measure for
	 * @param model
	 *            object to register measure on (external location)
	 * @param value
	 *            value
	 */
	protected <T extends Serializable> void registerMeasure(SensorContext context, Metric<T> metric,
			External<Location> model, T value) {
		final Location location = model.getExternal();
		if (location == null) {
			LOGGER.warn("Measure {} triggered, but {} did not contain a location to register measures.", metric.key(),
					model);
		} else {
			context.<T>newMeasure().forMetric(metric).on(model.getExternal().getOn()).withValue(value).save();
		}
	}
}
