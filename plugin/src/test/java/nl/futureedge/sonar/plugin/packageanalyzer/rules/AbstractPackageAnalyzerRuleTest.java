package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.MapSettings;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.NewRule;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;
import nl.futureedge.sonar.plugin.packageanalyzer.settings.PackageAnalyzerProperties;

public class AbstractPackageAnalyzerRuleTest extends BaseRuleTest {

	private Settings settings = new MapSettings(new PropertyDefinitions(PackageAnalyzerProperties.definitions()));
	private TestRule subject = new TestRule(settings);
	private SensorContextTester sensorContext = SensorContextTester.create(Paths.get("./src/main/java"));
	private ActiveRule activeRule = Mockito.mock(ActiveRule.class);

	@Before
	public void setup() {
		Mockito.when(activeRule.ruleKey()).thenReturn(RuleKey.of("testRepo", "testKey"));
	}

	@Test
	public void active() {
		final ActiveRules activeRules = Mockito.mock(ActiveRules.class);
		sensorContext.setActiveRules(activeRules);
		Mockito.when(activeRules.find(RuleKey.of("package-analyzer-test-no", "test"))).thenReturn(null);
		Mockito.when(activeRules.find(RuleKey.of("package-analyzer-test-yes", "test"))).thenReturn(activeRule);

		// Model
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", location("packageA/package-info.java"));

		// Check only no issue, because test-no:test is not active
		subject.scanModel(sensorContext, "test-no", model);
		Assert.assertEquals(0, sensorContext.allIssues().size());

		// Check only one issue, because test-yes:test is active
		subject.scanModel(sensorContext, "test-yes", model);
		Assert.assertEquals(1, sensorContext.allIssues().size());
	}

	@Test
	public void noLocation() {
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", null);
		model.addPackage("packageB", location("packageB/package-info.java"));

		subject.scanModel(sensorContext, activeRule, model);

		// Check only one issue, because no pacakgeA does not have a location
		Assert.assertEquals(1, sensorContext.allIssues().size());
	}

	public static final class TestRule extends AbstractPackageAnalyzerRule {

		private final Settings settings;

		protected TestRule(Settings settings) {
			super("test");
			this.settings = settings;
		}

		@Override
		public void define(NewRepository context) {
			// Unused
		}
		
		@Override
		public void defineRemediationTimes(NewRule rule) {
			// Unused
		}

		@Override
		protected void scanModel(SensorContext context, ActiveRule rule, Model<Location> model) {
			for (Package<Location> modelPackage : model.getPackages()) {
				registerIssue(context, settings, rule, modelPackage, modelPackage.getClasses(), "Issue");
			}

		}

	}

}
