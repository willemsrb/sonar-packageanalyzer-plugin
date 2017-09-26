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

public class DistanceFromMainSequenceRuleTest extends BaseRuleTest {
	
	private Settings settings = new MapSettings(new PropertyDefinitions(PackageAnalyzerProperties.definitions()));
	private DistanceFromMainSequenceRule subject = new DistanceFromMainSequenceRule(settings);
	private SensorContextTester sensorContext = SensorContextTester.create(Paths.get("./src/main/java"));
	private ActiveRule activeRule = Mockito.mock(ActiveRule.class);

	@Before
	public void setup() {
		Mockito.when(activeRule.param("maximum")).thenReturn("30");
		Mockito.when(activeRule.ruleKey()).thenReturn(RuleKey.of("testRepo", "testKey"));
	}

	/**
	 * <p>
	 * PackageA -> 2 abstract from 3 classes -> 66% Abstractness
	 * PackageA -> Uses 2 packages; used by 1 packages -> 2 / 3 -> 66% Instability
	 * PackageA -> D = |A + I - 1| = 32% -> Issue
	 * </p>
	 * <p>
	 * PackageB -> 1 abstract from 3 classes -> 33% Abstractness
	 * PackageB -> Uses 2 packages; used by 1 packages -> 2 / 3 -> 66% Instability
	 * PackageB -> D = |A + I - 1| = 1% -> No Issue
	 * </p>
	 * <p>
	 * PackageC -> 0 abstract from 3 classes -> 0% Abstractness
	 * PackageC -> Uses 1 package; used by 2 packages -> 1 / 3 -> 33% Instability
	 * PackageC -> D = |A + I - 1| = 66% -> Issue
	 * </p>
	 * <p>
	 * PackageD -> 1 abstract from 2 classes -> 50% Abstractness
	 * PackageD -> Uses 1 packages; used by 2 packages -> 1 / 3 -> 33% Instability
	 * PackageD -> D = |A + I - 1| = 17% -> No Issue
	 * </p>
	 */
	private Model<Location> createModel() {
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", location("packageA/package-info.java"));
		final Class<Location> cAA = model.addClass(Name.of("packageA.ClassA"), true, location("packageA/ClassA.java"));
		cAA.addUsage(Name.of("packageB.ClassA"));
		final Class<Location> cAB = model.addClass(Name.of("packageA.ClassB"), false, location("packageA/ClassB.java"));
		cAB.addUsage(Name.of("packageC.ClassB"));
		model.addClass(Name.of("packageA.ClassC"), true, location("packageA/ClassC.java"));
		
		model.addPackage("packageB", location("packageB/package-info.java"));
		final Class<Location> cBA = model.addClass(Name.of("packageB.ClassA"), false, location("packageB/ClassA.java"));
		cBA.addUsage(Name.of("packageD.ClassA"));
		model.addClass(Name.of("packageB.ClassB"), true, location("packageB/ClassB.java"));
		final Class<Location> cBC = model.addClass(Name.of("packageB.ClassC"), false, location("packageB/ClassC.java"));
		cBC.addUsage(Name.of("packageA.ClassC"));
		
		model.addPackage("packageC", location("packageC/package-info.java"));
		model.addClass(Name.of("packageC.ClassA"), false, location("packageC/ClassA.java"));
		model.addClass(Name.of("packageC.ClassB"), false, location("packageC/ClassB.java"));
		final Class<Location> cCC = model.addClass(Name.of("packageC.ClassC"), false, location("packageC/ClassC.java"));
		cCC.addUsage(Name.of("packageD.ClassB"));
		
		model.addPackage("packageD", location("packageD/package-info.java"));
		final Class<Location> cDA = model.addClass(Name.of("packageD.ClassA"), true, location("packageD/ClassA.java"));
		cDA.addUsage(Name.of("packageC.ClassA"));
		model.addClass(Name.of("packageD.ClassB"), false, location("packageD/ClassB.java"));
		
		return model;
	}
	
	@Test
	public void test() {
		final Model<Location> model = createModel();

		subject.scanModel(sensorContext, activeRule, model);

		// Check for two issues on packageA and packageC
		Assert.assertEquals(2, sensorContext.allIssues().size());
		
		// First issue on packageA
		final Issue issue = sensorContext.allIssues().iterator().next();
		final String message = issue.primaryLocation().message();
		System.out.println("Message: " + message);
		Assert.assertEquals(BaseRuleTest.PROJECT_KEY + ":packageA/package-info.java", 
				issue.primaryLocation().inputComponent().key());
		Assert.assertEquals("Distance from main sequence value is too high (32%), consider refactoring the package in order to lower it"
				+ " (values for instability: 66%, abstractness: 66%).", message);
	}
	
	@Test
	public void testClasses() {
		settings.setProperty(PackageAnalyzerProperties.ISSUE_MODE_KEY, PackageAnalyzerProperties.ISSUE_MODE_CLASS);
		final Model<Location> model = createModel();

		subject.scanModel(sensorContext, activeRule, model);

		// Check three issues on packageA and one on packageC
		Assert.assertEquals(4, sensorContext.allIssues().size());
		final Map<String, Issue> issues = sensorContext.allIssues().stream().collect(
				Collectors.toMap(issue -> issue.primaryLocation().inputComponent().key(), Function.identity()));

		Assert.assertTrue(issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/ClassA.java"));
		Assert.assertTrue(issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/ClassB.java"));
		Assert.assertTrue(issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/ClassC.java"));
		Assert.assertTrue(issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageC/ClassC.java"));
	}
}
