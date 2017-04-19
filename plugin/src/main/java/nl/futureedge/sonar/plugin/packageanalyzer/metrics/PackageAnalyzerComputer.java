package nl.futureedge.sonar.plugin.packageanalyzer.metrics;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;

/**
 * Metrics computer.
 */
public final class PackageAnalyzerComputer implements MeasureComputer {

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

		// Add own identifiers (from rules)
		final Measure identifier = context
				.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIER.key());
		if (identifier != null) {
			identifiers.addAll(Arrays.asList(identifier.getStringValue()));
		}

		// Add child identifiers
		for (final Measure childMeasure : context
				.getChildrenMeasures(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key())) {
			String[] childIdentifiers = childMeasure.getStringValue().split(",");
			identifiers.addAll(Arrays.asList(childIdentifiers));
		}

		// Set measure
		context.addMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key(),
				identifiers.stream().collect(Collectors.joining(",")));
	}

	private void countIdentifiers(final MeasureComputerContext context) {
		final Measure identifiers = context
				.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key());

		final int result;
		if (identifiers == null) {
			result = 0;
		} else {
			result = identifiers.getStringValue().split(",").length;
		}

		// Set measure
		context.addMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES.key(), result);
	}

}