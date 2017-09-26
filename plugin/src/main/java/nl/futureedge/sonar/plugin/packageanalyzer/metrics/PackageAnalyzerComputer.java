package nl.futureedge.sonar.plugin.packageanalyzer.metrics;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * Metrics computer.
 */
public final class PackageAnalyzerComputer implements MeasureComputer {

	private static final Logger LOGGER = Loggers.get(PackageAnalyzerComputer.class);
	
	private static final String METRIC_DIRECTORIES_NUMBER = CoreMetrics.DIRECTORIES_KEY;

	@Override
	public MeasureComputerDefinition define(final MeasureComputerDefinitionContext definitionContext) {
		return definitionContext.newDefinitionBuilder()
				.setInputMetrics(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIER.key(),
						METRIC_DIRECTORIES_NUMBER,
						PackageAnalyzerMetrics.AVERAGE_DEGREE.key(),
						PackageAnalyzerMetrics.PACKAGE_DEGREE.key(),
						PackageAnalyzerMetrics.VERTICE_DEGREE.key(),
						PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key(),
						PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES.key())
				.setOutputMetrics(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key(),
						PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES.key(),
						PackageAnalyzerMetrics.AVERAGE_DEGREE.key(),
						PackageAnalyzerMetrics.PACKAGE_DEGREE.key())
				.build();
	}

	@Override
	public void compute(final MeasureComputerContext context) {
		rollupIdentifiers(context);
		countIdentifiers(context);
		calcAverageDegree(context);
	}

	private void rollupIdentifiers(final MeasureComputerContext context) {
		final Set<String> identifiers = new TreeSet<>();
		LOGGER.debug("Rollup identifiers for (type={}): {}", context.getComponent().getType(),
				context.getComponent().getKey());

		// Add own identifiers (from rules)
		final Measure identifier = context
				.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIER.key());
		LOGGER.debug("Own identifier: {}", identifier == null ? "none" : identifier.getStringValue());
		addIdentifiers(identifiers, identifier);

		// Add child identifiers
		for (final Measure childMeasure : context
				.getChildrenMeasures(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key())) {
			addIdentifiers(identifiers, childMeasure);
		}

		final String result = identifiers.stream().collect(Collectors.joining(","));
		LOGGER.debug("Result: {}", result);
		// Set measure
		context.addMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key(), result);
	}

	private void addIdentifiers(final Set<String> identifiers, final Measure measure) {
		if (measure != null && !"".equals(measure.getStringValue())) {
			final String[] childIdentifiers = measure.getStringValue().split(",");
			identifiers.addAll(Arrays.asList(childIdentifiers));
		}
	}

	private void countIdentifiers(final MeasureComputerContext context) {
		final Measure identifiers = context
				.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key());

		final int result;
		if (identifiers == null || "".equals(identifiers.getStringValue())) {
			result = 0;
		} else {
			result = identifiers.getStringValue().split(",").length;
		}

		LOGGER.debug("Count -> {}", result);

		// Set measure
		context.addMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES.key(), result);
	}
	
	private void calcAverageDegree(final MeasureComputerContext context) {
		int totalDegree = 0;
		float result = 0.0f;
		final Measure dir = context.getMeasure(METRIC_DIRECTORIES_NUMBER);
		final int directories = dir != null ? dir.getIntValue() : 1;
		
		// Add child values, if any
		for (final Measure childMeasure : context
				.getChildrenMeasures(PackageAnalyzerMetrics.PACKAGE_DEGREE.key()))
			totalDegree += childMeasure.getIntValue();
			
		final Measure self = context
				.getMeasure(PackageAnalyzerMetrics.VERTICE_DEGREE.key());
				
		// Add self value if it does not have any children
		if (self != null && totalDegree == 0)
			totalDegree = self.getIntValue();
		
		context.addMeasure(PackageAnalyzerMetrics.PACKAGE_DEGREE.key(), totalDegree);
		
		// Calculate Average Degree
		final Measure totalMeasure = context
				.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEGREE.key());
				
		if (context.getComponent().getType().equals(Component.Type.FILE) || 
				context.getComponent().getType().equals(Component.Type.DIRECTORY))
			result = totalMeasure.getIntValue();
		else result = (float)2* totalMeasure.getIntValue() / directories;
		
		context.addMeasure(PackageAnalyzerMetrics.AVERAGE_DEGREE.key(), result);
	}

}