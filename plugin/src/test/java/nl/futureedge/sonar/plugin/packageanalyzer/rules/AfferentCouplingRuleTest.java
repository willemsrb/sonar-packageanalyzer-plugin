package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.MapSettings;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.rule.RuleKey;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Name;
import nl.futureedge.sonar.plugin.packageanalyzer.settings.PackageAnalyzerProperties;

public class AfferentCouplingRuleTest extends BaseRuleTest {

	private Settings settings = new MapSettings(new PropertyDefinitions(PackageAnalyzerProperties.definitions()));
	private AfferentCouplingRule subject = new AfferentCouplingRule(settings);
	private SensorContextTester sensorContext = SensorContextTester.create(Paths.get("./src/main/java"));
	private ActiveRule activeRule = Mockito.mock(ActiveRule.class);

	@Before
	public void setup() {
		Mockito.when(activeRule.param("maximum")).thenReturn("1");
		Mockito.when(activeRule.ruleKey()).thenReturn(RuleKey.of("testRepo", "testKey"));
	}

	/**
	 * PackageA -> Used by 2 packages -> Issue PackageB -> Used by 1 package ->
	 * No issue PackageC -> Used by 0 packages -> No issue
	 */
	@Test
	public void test() {
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", location("packageA/package-info.java"));
		model.addClass(Name.of("packageA.ClassA"), true, null);
		model.addClass(Name.of("packageA.ClassB"), true, null);
		model.addClass(Name.of("packageA.ClassC"), true, null);
		model.addPackage("packageB", location("packageB/package-info.java"));
		Class<Location> cBA = model.addClass(Name.of("packageB.ClassA"), true, null);
		cBA.addUsage(Name.of("packageA.ClassA"));
		model.addClass(Name.of("packageB.ClassB"), true, null);
		model.addClass(Name.of("packageB.ClassC"), false, null);
		model.addPackage("packageC", location("packageC/package-info.java"));
		Class<Location> cCA = model.addClass(Name.of("packageC.ClassA"), true, null);
		cCA.addUsage(Name.of("packageA.ClassA"));
		cCA.addUsage(Name.of("packageB.ClassA"));
		model.addClass(Name.of("packageC.ClassB"), true, null);
		model.addClass(Name.of("packageC.ClassC"), false, null);

		subject.scanModel(sensorContext, activeRule, model);

		// Check one issue on packageA
		Assert.assertEquals(1, sensorContext.allIssues().size());
		final Issue issue = sensorContext.allIssues().iterator().next();
		Assert.assertEquals(BaseRuleTest.PROJECT_KEY + ":packageA/package-info.java",
				issue.primaryLocation().inputComponent().key());
	}

}
