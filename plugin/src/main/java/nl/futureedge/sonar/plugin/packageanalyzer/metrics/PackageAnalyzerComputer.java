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
		LOGGER.info("Rollup identifiers for (type={}): {}", context.getComponent().getType(),
				context.getComponent().getKey());

		// Add own identifiers (from rules)
		final Measure identifier = context
				.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIER.key());
		if (identifier != null && !"".equals(identifier.getStringValue())) {
			identifiers.addAll(Arrays.asList(identifier.getStringValue()));
		}
		LOGGER.info("Own identifier: {}", identifier == null ? "none" : identifier.getStringValue());

		// Add child identifiers
		for (final Measure childMeasure : context
				.getChildrenMeasures(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key())) {
			if(!"".equals(childMeasure.getStringValue())) {
				final String[] childIdentifiers = childMeasure.getStringValue().split(",");
				identifiers.addAll(Arrays.asList(childIdentifiers));
			}
		}

		String result = identifiers.stream().collect(Collectors.joining(","));
		LOGGER.info("Result: {}", result);
		// Set measure
		context.addMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key(), result);
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

		LOGGER.info("Count -> {}", result);

		// Set measure
		context.addMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES.key(), result);
	}

}