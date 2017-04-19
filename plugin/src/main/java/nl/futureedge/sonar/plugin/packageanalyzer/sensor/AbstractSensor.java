package nl.futureedge.sonar.plugin.packageanalyzer.sensor;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.BaseRules;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.Location;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.PackageAnalyzerRule;

/**
 * Base sensor implementation; contains all logic but not the language specific
 * model.
 */
public abstract class AbstractSensor implements Sensor {

	private static final Logger LOGGER = Loggers.get(AbstractSensor.class);

	private final String language;
	private final PackageAnalyzerRule[] rules;

	public AbstractSensor(final String language, final PackageAnalyzerRule... rules) {
		this.language = language;
		this.rules = rules;
	}

	@Override
	public final void describe(final SensorDescriptor descriptor) {
		descriptor.name("Package Analyzer Sensor (" + language + ")");
		descriptor.onlyOnLanguage(language);
		descriptor.createIssuesForRuleRepository(BaseRules.getRepositoryKey(language));
	}

	@Override
	public final void execute(final SensorContext context) {
		LOGGER.info("Build package model ...");
		final Model<Location> model = buildModel(context);
		LOGGER.info("Package model built, analyzing model for issues ...");

		for (final PackageAnalyzerRule rule : rules) {
			if (rule.supportsLanguage(language)) {
				LOGGER.info("Executing rule: {}", rule);
				rule.scanModel(context, language, model);
			}
		}
		LOGGER.info("Analysis done");
	}

	protected abstract Model<Location> buildModel(final SensorContext context);
}
