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

public class UnstableDependencyRuleTest extends BaseRuleTest {
	
	private Settings settings = new MapSettings(new PropertyDefinitions(PackageAnalyzerProperties.definitions()));
	public UnstableDependencyRule subject = new UnstableDependencyRule(settings);
	private SensorContextTester sensorContext = SensorContextTester.create(Paths.get("./src/main/java"));
	private ActiveRule activeRule = Mockito.mock(ActiveRule.class);

	@Before
	public void setup() {
		Mockito.when(activeRule.param("maximum")).thenReturn("30");
		Mockito.when(activeRule.ruleKey()).thenReturn(RuleKey.of("testRepo", "testKey"));
	}
	
	/**
	 * <p>
	 * Package A: Instability: 2/5 -> 40%, Unstable dependencies from other packages: 3,
	 * Unstable Dependency Ratio: 2/5 -> 40% -> Issue
	 * </p>
	 * <p>
	 * Package B: Instability: 1/3 -> 33%, Unstable dependencies from other packages: 1,
	 * Unstable Dependency Ratio: 2/3 -> 66% -> Issue
	 * </p>
	 * <p>
	 * Package C: Instability: 2/4 -> 50%, Unstable dependencies from other packages: 1,
	 * Unstable Dependency Ratio: 1/4 -> 25% -> No Issue
	 * </p>
	 * <p>
	 * Package D: Instability: 3/4 -> 75%, Unstable dependencies from other packages: 0,
	 * Unstable Dependency Ratio: 0/4 -> 0% -> No Issue
	 * </p>
	 */
	private Model<Location> createModel() {
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", location("packageA/package-info.java"));
		final Class<Location> cAA = model.addClass(Name.of("packageA.ClassA"), false, location("packageA/ClassA.java"));
		final Class<Location> cBA = model.addClass(Name.of("packageA.ClassB"), false, location("packageA/ClassB.java"));
		cAA.addUsage(Name.of("packageB.ClassA")); //2 efferent packages, 3 afferent packages
		cBA.addUsage(Name.of("packageC.ClassA"));
		
		model.addPackage("packageB", location("packageB/package-info.java"));
		final Class<Location> cAB = model.addClass(Name.of("packageB.ClassA"), false, location("packageB/ClassA.java"));
		cAB.addUsage(Name.of("packageA.ClassA")); //1 efferent package, 2 afferent packages
		
		model.addPackage("packageC", location("packageC/package-info.java"));
		final Class<Location> cAC = model.addClass(Name.of("packageC.ClassA"), false, location("packageC/ClassA.java"));
		cAC.addUsage(Name.of("packageA.ClassA")); //2 efferent packages, 2 afferent packages
		cAC.addUsage(Name.of("packageD.ClassA"));
		
		model.addPackage("packageD", location("packageD/package-info.java"));
		final Class<Location> cAD = model.addClass(Name.of("packageD.ClassA"), false, location("packageD/ClassA.java"));
		final Class<Location> cBD = model.addClass(Name.of("packageD.ClassB"), false, location("packageD/ClassB.java"));
		cAD.addUsage(Name.of("packageA.ClassA")); //3 efferent packages, 1 afferent package
		cAD.addUsage(Name.of("packageB.ClassA"));
		cBD.addUsage(Name.of("packageA.ClassB"));
		cBD.addUsage(Name.of("packageC.ClassA"));
		return model;
	}
	
	@Test
	public void test() {
		final Model<Location> model = createModel();
		subject.scanModel(sensorContext, activeRule, model);
		
		//2 Issues reported on package A and B
		Assert.assertEquals(2, sensorContext.allIssues().size());
		final Map<String, Issue> issues = sensorContext.allIssues().stream().collect(
				Collectors.toMap(issue -> issue.primaryLocation().inputComponent().key(), Function.identity()));
		Assert.assertTrue("Issue was not reported on package A", issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/package-info.java"));
		Assert.assertTrue("Issue was not reported on package B", issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageB/package-info.java"));
		
	}
	
	@Test
	public void testClasses() {
		settings.setProperty(PackageAnalyzerProperties.ISSUE_MODE_KEY, PackageAnalyzerProperties.ISSUE_MODE_CLASS);
		final Model<Location> model = createModel();
		subject.scanModel(sensorContext, activeRule, model);
		
		//3 Issues reported - 2 classes on package A, and 1 class on package B
		Assert.assertEquals(3, sensorContext.allIssues().size());
		final Map<String, Issue> issues = sensorContext.allIssues().stream().collect(
				Collectors.toMap(issue -> issue.primaryLocation().inputComponent().key(), Function.identity()));
		Assert.assertTrue("Issue was not reported on package A class A", issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/ClassA.java"));
		Assert.assertTrue("Issue was not reported on package A class B", issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/ClassB.java"));
		Assert.assertTrue("Issue was not reported on package B class A", issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageB/ClassA.java"));
		
	}
	
}