package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import java.io.Serializable;
import java.util.Set;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Metric;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.External;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;
import nl.futureedge.sonar.plugin.packageanalyzer.settings.PackageAnalyzerProperties;

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
			LOGGER.debug("Rule {}:{} is not active", BaseRules.getRepositoryKey(language), ruleKey);
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
	protected final void registerIssue(final SensorContext context, final ActiveRule rule, final Class<Location> model,
			final String message) {
		newIssue(context, rule, model, model.getParentPackage().getClasses().size(), message);
	}

	private boolean newIssue(final SensorContext context, final ActiveRule rule, final External<Location> model, double gap, final String message) {
		final Location location = model.getExternal();
		if (location == null) {
			LOGGER.debug("Rule {} triggered, but {} did not contain a location to register issue", rule.ruleKey(),
					model);
			return false;
		} else {
			LOGGER.debug("Rule {} triggered, registering issue on {}", rule.ruleKey(), model);
			final NewIssue issue = context.newIssue().forRule(rule.ruleKey());
			issue.at(issue.newLocation().on(location.getOn()).at(location.getAt()).message(message));
			//adding gap factor for remediation time
			issue.gap(gap);
			issue.save();
			return true;
		}
	}

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
	protected final void registerIssue(final SensorContext context, final Settings settings, final ActiveRule rule,
			final Package<Location> modelPackage, final Set<Class<Location>> modelClasses, final String message) {
		LOGGER.debug("registerIssue(context={}, settings={}, rule={}, package={}, classes={}, message={}", context,
				settings, rule, modelPackage, modelClasses, message);

		boolean registered = false;
		if (PackageAnalyzerProperties.shouldRegisterOnPackage(settings)
				&& newIssue(context, rule, modelPackage, modelClasses.size(), message)) {
			registered = true;
		}

		if (!registered && PackageAnalyzerProperties.shouldRegisterOnClasses(settings) && !modelClasses.isEmpty()) {
			if (PackageAnalyzerProperties.shouldRegisterOnAllClasses(settings)) {
				for (final Class<Location> modelClass : modelClasses)
					newIssue(context, rule, modelClass, 0, message); //Remediation times on all classes have constant values
			} else 
				newIssue(context, rule, modelClasses.iterator().next(), modelClasses.size(), message);
			registered = true;
		}

		if (!registered) {
			LOGGER.warn("Rule {} triggered, but {} did not contain a location to register issues.", rule.ruleKey(),
					modelPackage);
		}
	}
	
	/**
	 * Register an issue.
	 * 
	 * @param context
	 *            sensor context
	 * @param rule
	 *            rule to register issue for
	 * @param model
	 *            object to register issue on (external location)
	 * @param cycleSize
	 *			  number of packages in the cycle, used to calculate remediation time
	 * @param message
	 *            message
	 */
	protected final void registerIssue(final SensorContext context, final Settings settings, final ActiveRule rule,
			final Package<Location> modelPackage, final int cycleSize, final String message) {
		LOGGER.debug("registerIssue(context={}, settings={}, rule={}, package={}, cycleSize={}, message={}", context, 
			settings, rule, modelPackage, cycleSize, message);
		newIssue(context, rule, modelPackage, cycleSize, message);
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
	protected final <T extends Serializable> void registerMeasure(final SensorContext context, final Metric<T> metric,
			final External<Location> model, final T value) {
		final Location location = model.getExternal();
		if (location == null) {
			LOGGER.warn("Measure {} triggered, but {} did not contain a location to register measures.", metric.key(),
					model);
		} else {
			context.<T>newMeasure().forMetric(metric).on(model.getExternal().getOn()).withValue(value).save();
		}
	}
}
