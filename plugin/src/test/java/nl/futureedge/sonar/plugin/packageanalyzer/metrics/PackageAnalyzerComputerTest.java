package nl.futureedge.sonar.plugin.packageanalyzer.metrics;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.ce.measure.Component.Type;
import org.sonar.api.ce.measure.MeasureComputer.MeasureComputerDefinition;
import org.sonar.api.ce.measure.test.TestComponent;
import org.sonar.api.ce.measure.test.TestMeasureComputerContext;
import org.sonar.api.ce.measure.test.TestMeasureComputerDefinitionContext;
import org.sonar.api.ce.measure.test.TestSettings;
import org.sonar.api.measures.CoreMetrics;

public class PackageAnalyzerComputerTest {

	private final PackageAnalyzerComputer subject = new PackageAnalyzerComputer();

	private static final String METRIC_DIRECTORIES_NUMBER = CoreMetrics.DIRECTORIES_KEY;
	
	private TestMeasureComputerDefinitionContext definitionContext;
	private MeasureComputerDefinition definition;

	private TestSettings settings;
	private TestComponent component;

	private TestMeasureComputerContext context;

	@Before
	public void setup() {
		definitionContext = new TestMeasureComputerDefinitionContext();
		definition = subject.define(definitionContext);

		settings = new TestSettings();
		component = new TestComponent("test", Type.PROJECT, null);
		
		context = new TestMeasureComputerContext(component, settings, definition);	
		
		context.addInputMeasure(METRIC_DIRECTORIES_NUMBER, 7);
	}

	@Test
	public void testDefinition() {
		final TestMeasureComputerDefinitionContext definitionContext = new TestMeasureComputerDefinitionContext();
		final MeasureComputerDefinition definition = subject.define(definitionContext);

		Assert.assertNotNull(definition);
		Assert.assertEquals(7, definition.getInputMetrics().size());
		Assert.assertTrue(definition.getInputMetrics().contains(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIER.key()));
		Assert.assertTrue(definition.getInputMetrics().contains(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key()));
		Assert.assertTrue(definition.getInputMetrics().contains(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES.key()));
		Assert.assertTrue(definition.getInputMetrics().contains(METRIC_DIRECTORIES_NUMBER));

		Assert.assertEquals(4, definition.getOutputMetrics().size());
		Assert.assertTrue(definition.getOutputMetrics().contains(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key()));
		Assert.assertTrue(definition.getOutputMetrics().contains(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES.key()));
	}

	@Test
	public void testEmpty() {
		subject.compute(context);

		Assert.assertEquals("", context.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key()).getStringValue());
		Assert.assertEquals(0, context.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES.key()).getIntValue());
		Assert.assertEquals(0, context.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEGREE.key()).getIntValue());
		Assert.assertEquals(0, context.getMeasure(PackageAnalyzerMetrics.AVERAGE_DEGREE.key()).getDoubleValue(), 0);
	}

	@Test
	public void testOwnDegree() {
		context.addInputMeasure(PackageAnalyzerMetrics.VERTICE_DEGREE.key(), 7);
		
		subject.compute(context);
		
		Assert.assertEquals(7, context.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEGREE.key()).getIntValue());
		Assert.assertEquals(2, context.getMeasure(PackageAnalyzerMetrics.AVERAGE_DEGREE.key()).getDoubleValue(), 0.1);
	}
	
	@Test
	public void testChildDegree() {
		//Testing on a project component: AverageDegree = 2 * numEdges / numVertices = 2 * 17 / 3
		context.addChildrenMeasures(PackageAnalyzerMetrics.PACKAGE_DEGREE.key(), 5, 6, 6);
		
		subject.compute(context);
		
		Assert.assertEquals(17, context.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEGREE.key()).getIntValue());
		Assert.assertEquals(4.85, context.getMeasure(PackageAnalyzerMetrics.AVERAGE_DEGREE.key()).getDoubleValue(), 0.1);
	}
	
	@Test
	public void testOwnIdentifiers() {
		context.addInputMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIER.key(), "1,2,3");
		
		subject.compute(context);

		Assert.assertEquals("1,2,3", context.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key()).getStringValue());
		Assert.assertEquals(3, context.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES.key()).getIntValue());
	}
	
	@Test
	public void testChildIdentifiers() {
		context.addChildrenMeasures(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key(), "1,2,3", "2,3,4,5");
		
		subject.compute(context);

		Assert.assertEquals("1,2,3,4,5", context.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key()).getStringValue());
		Assert.assertEquals(5, context.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES.key()).getIntValue());
	}


	@Test
	public void testAllIdentifiers() {
		context.addInputMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIER.key(), "1,2,3");
		context.addChildrenMeasures(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key(), "2,3,4", "3,4,5");
		
		subject.compute(context);

		Assert.assertEquals("1,2,3,4,5", context.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIERS.key()).getStringValue());
		Assert.assertEquals(5, context.getMeasure(PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES.key()).getIntValue());
	}

}
