package nl.futureedge.sonar.plugin.packageanalyzer.sensor;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.Location;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.PackageAnalyzerRule;

public class AbstractSensorTest {

	private final TestRule rule1 = new TestRule();
	private final TestRule rule2 = new TestRule();
	private final TestSensor sensor = new TestSensor(rule1, rule2);

	@Test
	public void describe() {
		DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
		sensor.describe(descriptor);
		Assert.assertEquals("Package Analyzer Sensor (TEST)", descriptor.name());
		Assert.assertEquals(Arrays.asList("TEST"), descriptor.languages());
	}
	
	@Test
	public void execute() {
		Assert.assertFalse(sensor.called);
		Assert.assertFalse(rule1.called);
		Assert.assertFalse(rule2.called);

		sensor.execute(null);

		Assert.assertTrue(sensor.called);
		Assert.assertTrue(rule1.called);
		Assert.assertTrue(rule2.called);

	}

	private static final class TestSensor extends AbstractSensor {

		boolean called;

		public TestSensor(PackageAnalyzerRule... rules) {
			super("TEST", rules);
		}

		@Override
		protected Model<Location> buildModel(SensorContext context) {
			called = true;
			return new Model<>();
		}

	}

	private static final class TestRule implements PackageAnalyzerRule {

		boolean called;

		@Override
		public void define(NewRepository context) {
			// Unused
		}

		@Override
		public void defineRemediationTimes(NewRule rule) {
			// Unused
		}
		
		@Override
		public void scanModel(SensorContext context, String language, Model<Location> model) {
			called = true;
		}

	}
}
