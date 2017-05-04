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

public class NumberOfClassesAndInterfacesRuleTest extends BaseRuleTest {

	private Settings settings = new MapSettings(new PropertyDefinitions(PackageAnalyzerProperties.definitions()));
	private NumberOfClassesAndInterfacesRule subject = new NumberOfClassesAndInterfacesRule(settings);
	private SensorContextTester sensorContext = SensorContextTester.create(Paths.get("./src/main/java"));
	private ActiveRule activeRule = Mockito.mock(ActiveRule.class);

	@Before
	public void setup() {
		Mockito.when(activeRule.param("maximum")).thenReturn("2");
		Mockito.when(activeRule.ruleKey()).thenReturn(RuleKey.of("testRepo", "testKey"));
	}

	/**
	 * <p>
	 * PackageA -> 3 classes -> Issue
	 * </p>
	 * <p>
	 * PackageB -> 0 classes -> No issue
	 * </p>
	 */
	private Model<Location> createModel() {
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", location("packageA/package-info.java"));
		model.addClass(Name.of("packageA.ClassA"), true, location("packageA/ClassA.java"));
		model.addClass(Name.of("packageA.ClassB"), true, null);
		model.addClass(Name.of("packageA.ClassC"), true, location("packageA/ClassC.java"));
		model.addPackage("packageB", location("packageB/package-info.java"));
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
		Assert.assertEquals("Reduce number of classes in package (allowed: 2, actual: 3)", message);
	}
	

	@Test
	public void testOnClasses() {
		settings.setProperty(PackageAnalyzerProperties.ISSUE_MODE_KEY, PackageAnalyzerProperties.ISSUE_MODE_CLASS);
		
		final Model<Location> model = createModel();
		subject.scanModel(sensorContext, activeRule, model);


		// Check two issues on packageA
		Assert.assertEquals(2, sensorContext.allIssues().size());	
		Map<String,Issue> issues = sensorContext.allIssues().stream().collect(Collectors.toMap(issue -> issue.primaryLocation().inputComponent().key(),
                Function.identity()));
		
		issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/ClassA.java");
		issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/ClassC.java");
	}
	
}
