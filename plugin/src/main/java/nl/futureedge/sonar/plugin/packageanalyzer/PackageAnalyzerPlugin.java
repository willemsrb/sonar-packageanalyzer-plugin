package nl.futureedge.sonar.plugin.packageanalyzer;

import org.sonar.api.Plugin;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import nl.futureedge.sonar.plugin.packageanalyzer.metrics.PackageAnalyzerComputer;
import nl.futureedge.sonar.plugin.packageanalyzer.metrics.PackageAnalyzerMetrics;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.AbstractnessRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.AfferentCouplingRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.DistanceFromMainSequenceRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.EfferentCouplingRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.InstabilityRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.UnstableDependencyRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.JavaRules;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.MissingPackageInfoRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.NumberOfClassesAndInterfacesRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.PackageDependencyCyclesRule;
import nl.futureedge.sonar.plugin.packageanalyzer.sensor.JavaSensor;
import nl.futureedge.sonar.plugin.packageanalyzer.settings.PackageAnalyzerProperties;

/**
 * Package analyzer plugin.
 */
public final class PackageAnalyzerPlugin implements Plugin {

	private static final Logger LOGGER = Loggers.get(PackageAnalyzerPlugin.class);
	
	public static final String KEY = "package-analyzer";

	@Override
	public void define(final Context context) {
		LOGGER.debug("Defining plugin ...");
		
		// Rules
		context.addExtensions(AbstractnessRule.class,
				AfferentCouplingRule.class,
				DistanceFromMainSequenceRule.class,
				EfferentCouplingRule.class,
				InstabilityRule.class,
				UnstableDependencyRule.class,
				MissingPackageInfoRule.class,
				NumberOfClassesAndInterfacesRule.class,
				PackageDependencyCyclesRule.class);
		
		// Metrics
		context.addExtensions(PackageAnalyzerMetrics.class, PackageAnalyzerComputer.class);
		
		// Settings
		context.addExtensions(PackageAnalyzerProperties.definitions());
		
		// Java
		context.addExtensions(JavaRules.class, JavaSensor.class);
	
		LOGGER.debug("Plugin defined");
	}
}
