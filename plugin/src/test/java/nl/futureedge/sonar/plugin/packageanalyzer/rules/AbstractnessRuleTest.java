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

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Name;
import nl.futureedge.sonar.plugin.packageanalyzer.settings.PackageAnalyzerProperties;

public class AbstractnessRuleTest extends BaseRuleTest {

	private Settings settings = new MapSettings(new PropertyDefinitions(PackageAnalyzerProperties.definitions()));
	private AbstractnessRule subject = new AbstractnessRule(settings);
	private SensorContextTester sensorContext = SensorContextTester.create(Paths.get("./src/main/java"));
	private ActiveRule activeRule = Mockito.mock(ActiveRule.class);
	private Model<Location> model = new Model<>();

	/**
	 * <p>
	 * PackageA -> 3 abstract from 3 classes -> 100% -> Issue
	 * </p>
	 * <p>
	 * PackageB -> 0 classes -> 0% -> No issue
	 * </p>
	 * <p>
	 * PackageC -> 1 abstract from 2 classes -> 50% -> No issue
	 * </p>
	  * <p>
	 * PackageD -> 2 abstract from 3 classes -> 66% -> Issue
	 * </p>
	 */
	@Before
	public void setup() {
		Mockito.when(activeRule.param("maximum")).thenReturn("60");
		Mockito.when(activeRule.ruleKey()).thenReturn(RuleKey.of("testRepo", "testKey"));

		model.addPackage("packageA", location("packageA/package-info.java"));
		model.addClass(Name.of("packageA.ClassA"), true, null);
		model.addClass(Name.of("packageA.ClassB"), true, null);
		model.addClass(Name.of("packageA.ClassC"), true, null);
		model.addPackage("packageB", location("packageB/package-info.java"));
		model.addPackage("packageC", location("packageC/package-info.java"));
		model.addClass(Name.of("packageC.ClassA"), true, null);
		model.addClass(Name.of("packageC.ClassB"), false, null);
		model.addPackage("packageD", null);
		model.addClass(Name.of("packageD.ClassA"), false, location("packageD/ClassA.java"));
		model.addClass(Name.of("packageD.ClassB"), true, location("packageD/ClassB.java"));
		model.addClass(Name.of("packageD.ClassC"), true,location("packageD/ClassC.java"));
	}

	@Test
	public void test() {
		settings.setProperty(PackageAnalyzerProperties.ISSUE_MODE_KEY, PackageAnalyzerProperties.ISSUE_MODE_PACKAGE);
		subject.scanModel(sensorContext, activeRule, model);

		// Check one issue on packageA
		Assert.assertEquals(1, sensorContext.allIssues().size());
		final Issue issue = sensorContext.allIssues().iterator().next();
		Assert.assertEquals(BaseRuleTest.PROJECT_KEY + ":packageA/package-info.java",
				issue.primaryLocation().inputComponent().key());
	}

	@Test
	public void testFallback() {
		//settings.setProperty(PackageAnalyzerProperties.ISSUE_MODE_KEY, PackageAnalyzerProperties.ISSUE_MODE_FALLBACK);
		subject.scanModel(sensorContext, activeRule, model);

		// Check one issue on packageA
		Assert.assertEquals(3, sensorContext.allIssues().size());	
		Map<String,Issue> issues = sensorContext.allIssues().stream().collect(Collectors.toMap(issue -> issue.primaryLocation().inputComponent().key(),
                Function.identity()));
		
		issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/package-info.java");
		issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageD/ClassB.java");
		issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageD/ClassC.java");
	}
	
	@Test
	public void testClasses() {
		settings.setProperty(PackageAnalyzerProperties.ISSUE_MODE_KEY, PackageAnalyzerProperties.ISSUE_MODE_CLASS);
		settings.setProperty(PackageAnalyzerProperties.CLASS_MODE_KEY, PackageAnalyzerProperties.CLASS_MODE_FIRST);
		subject.scanModel(sensorContext, activeRule, model);

		// Check one issue on packageA
		Assert.assertEquals(1, sensorContext.allIssues().size());	
		Map<String,Issue> issues = sensorContext.allIssues().stream().collect(Collectors.toMap(issue -> issue.primaryLocation().inputComponent().key(),
                Function.identity()));
		
		//issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/package-info.java");
		issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageD/ClassB.java");
		//issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageD/ClassC.java");
	}
}
