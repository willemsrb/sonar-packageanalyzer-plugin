package nl.futureedge.sonar.plugin.packageanalyzer.sensor;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.Location;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.PackageAnalyzerRule;

public abstract class AbstractSensor implements Sensor {

	private final String language;
	private final PackageAnalyzerRule[] rules;

	public AbstractSensor(final String language, final PackageAnalyzerRule... rules) {
		this.language=language;
		this.rules = rules;
	}
	
	@Override
	public void describe(final SensorDescriptor descriptor) {
		descriptor.name("Package Analyzer Sensor ("+language+")");
		descriptor.onlyOnLanguage(language);
	}

	@Override
	public void execute(final SensorContext context) {
		final Model<Location> model = buildModel(context);
		
		for(final PackageAnalyzerRule rule : rules) {
			rule.scanModel(context, language, model);
		}
	} 
	
	protected abstract Model<Location> buildModel(final SensorContext context);
}
