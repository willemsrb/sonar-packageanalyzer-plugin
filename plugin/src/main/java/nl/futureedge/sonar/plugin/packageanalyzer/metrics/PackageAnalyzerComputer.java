package nl.futureedge.sonar.plugin.packageanalyzer.metrics;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import nl.futureedge.sonar.plugin.packageanalyzer.rules.NumberOfClassesAndInterfacesRule;

/**
 * Metrics computer.
 */
public final class PackageAnalyzerComputer implements MeasureComputer {

	private static final Logger LOGGER = Loggers.get(NumberOfClassesAndInterfacesRule.class);

	@Override
	public MeasureComputerDefinition define(final MeasureComputerDefinitionContext definitionContext) {
		return definitionContext.newDefinitionBuilder()
				.setInputMetrics(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIER.key(),
						PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key(),
						PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES.key())
				.setOutputMetrics(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key(),
						PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES.key())
				.build();
	}

	@Override
	public void compute(final MeasureComputerContext context) {
		rollupIdentifiers(context);
		countIdentifiers(context);
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

		String result = identifiers.stream().collect(Collectors.joining(","));
		LOGGER.debug("Result: {}", result);
		// Set measure
		context.addMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key(), result);
	}

	private void addIdentifiers(Set<String> identifiers, Measure measure) {
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

}