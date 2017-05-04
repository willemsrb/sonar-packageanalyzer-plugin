package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

public class InstabilityRuleTest extends BaseRuleTest {

	private Settings settings = new MapSettings(new PropertyDefinitions(PackageAnalyzerProperties.definitions()));
	private InstabilityRule subject = new InstabilityRule(settings);
	private SensorContextTester sensorContext = SensorContextTester.create(Paths.get("./src/main/java"));
	private ActiveRule activeRule = Mockito.mock(ActiveRule.class);

	@Before
	public void setup() {
		Mockito.when(activeRule.param("maximum")).thenReturn("75");
		Mockito.when(activeRule.ruleKey()).thenReturn(RuleKey.of("testRepo", "testKey"));
	}

	/**
	 * <p>
	 * Package A: Uses 1 package; used by 0 packages -> 1 / 1 -> 100% -> Issue
	 * </p>
	 * <p>
	 * Package B: Uses 1 package; used by 1 package -> 1 / 2 -> 50% -> No issue
	 * </p>
	 * <p>
	 * Package C: Uses 0 packages; used by 1 package -> 0 / 1 -> 0% -> No issue
	 * </p>
	 * <p>
	 * Package D: Uses 0 packages; used by 0 packages -> No issue
	 * </p>
	 */
	private Model<Location> createModel() {
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", location("packageA/package-info.java"));
		final Class<Location> cAA = model.addClass(Name.of("packageA.ClassA"), false, location("packageA/ClassA.java"));
		cAA.addUsage(Name.of("packageB.ClassA"));
		final Class<Location> cAB = model.addClass(Name.of("packageA.ClassB"), false, location("packageA/ClassB.java"));
		cAB.addUsage(Name.of("packageA.ClassA"));
		final Class<Location> cAC = model.addClass(Name.of("packageA.ClassC"), false, location("packageA/ClassC.java"));
		cAC.addUsage(Name.of("packageB.ClassA"));

		model.addPackage("packageB", location("packageB/package-info.java"));
		final Class<Location> cBA = model.addClass(Name.of("packageB.ClassA"), false, null);
		cBA.addUsage(Name.of("packageC.ClassA"));

		model.addPackage("packageC", location("packageC/package-info.java"));

		model.addPackage("packageD", location("packageD/package-info.java"));
		return model;
	}

	@Test
	public void test() {
		final Model<Location> model = createModel();

		subject.scanModel(sensorContext, activeRule, model);

		// Check one issue on packageA
		Assert.assertEquals(1, sensorContext.allIssues().size());
		final Issue issue = sensorContext.allIssues().iterator().next();
		final String message = issue.primaryLocation().message();
		System.out.println("Message: " + message);
		Assert.assertEquals(BaseRuleTest.PROJECT_KEY + ":packageA/package-info.java",
				issue.primaryLocation().inputComponent().key());
		Assert.assertEquals(
				"Reduce number of packages used by this package to lower instability (allowed: 75%, actual: 100%)",
				message);
	}

	@Test
	public void testClasses() {
		settings.setProperty(PackageAnalyzerProperties.ISSUE_MODE_KEY, PackageAnalyzerProperties.ISSUE_MODE_CLASS);
		final Model<Location> model = createModel();

		subject.scanModel(sensorContext, activeRule, model);

		// Check two issues on packageA
		Assert.assertEquals(2, sensorContext.allIssues().size());
		final Map<String, Issue> issues = sensorContext.allIssues().stream().collect(
				Collectors.toMap(issue -> issue.primaryLocation().inputComponent().key(), Function.identity()));

		issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/ClassA.java");
		issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/ClassC.java");
	}
}
