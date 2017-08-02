package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import java.nio.file.Paths;
import java.util.Collections;
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

import nl.futureedge.sonar.plugin.packageanalyzer.metrics.PackageAnalyzerMetrics;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Name;
import nl.futureedge.sonar.plugin.packageanalyzer.settings.PackageAnalyzerProperties;

public class PackageDependencyCyclesRuleTest extends BaseRuleTest {

	private Settings settings = new MapSettings(new PropertyDefinitions(PackageAnalyzerProperties.definitions()));
	private PackageDependencyCyclesRule subject = new PackageDependencyCyclesRule(settings);
	private SensorContextTester sensorContext = SensorContextTester.create(Paths.get("./src/main/java"));
	private ActiveRule activeRule = Mockito.mock(ActiveRule.class);

	@Before
	public void setup() {
		Mockito.when(activeRule.param("maximum")).thenReturn("2");
		Mockito.when(activeRule.ruleKey()).thenReturn(RuleKey.of("testRepo", "testKey"));
	}

	@Test
	public void test() {
		final Model<Location> model = createModel();

		subject.scanModel(sensorContext, activeRule, model);

		// Check one issue on packageA (no location on any part of packageB)
		Assert.assertEquals(1, sensorContext.allIssues().size());
		final Issue issue = sensorContext.allIssues().iterator().next();
		String message = issue.primaryLocation().message();
		System.out.println("Message: " + message);
		Assert.assertEquals(BaseRuleTest.PROJECT_KEY + ":packageA/package-info.java",
				issue.primaryLocation().inputComponent().key());
		Assert.assertEquals("Break the package cycle containing the following cycle of packages: packageA (ClassA references classC, classD, classE, classX, ClassB references classB, ClassC references classB, ClassD references classC), packageB (ClassA references classA)", message);
	}

	private Model<Location> createModel() {
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", location("packageA/package-info.java"));
		model.addClass(Name.of("packageA.ClassB"), true, location("packageA/ClassB.java")).addUsage(Name.of("packageB.classB"));
		model.addClass(Name.of("packageA.ClassD"), true, null).addUsage(Name.of("packageB.classC"));
		Class<Location> classAA = model.addClass(Name.of("packageA.ClassA"), true, null);
		classAA.addUsage(Name.of("packageB.classD"));
		classAA.addUsage(Name.of("packageB.classC"));
		classAA.addUsage(Name.of("packageB.classE"));
		classAA.addUsage(Name.of("packageB.classX"));
		model.addClass(Name.of("packageA.ClassC"), true, location("packageA/ClassC.java")).addUsage(Name.of("packageB.classB"));
		model.addClass(Name.of("packageA.ClassE"), true, location("packageA/ClassE.java")).addUsage(Name.of("packageA.classA"));
		model.addClass(Name.of("packageB.ClassA"), true, null).addUsage(Name.of("packageA.classA"));
		
		return model;
	}
	
	private Model<Location> createFallbackModel() {
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", null);
		model.addClass(Name.of("packageA.ClassA"), false, location("packageA/ClassA.java")).addUsage(Name.of("packageB.ClassA"));
		model.addClass(Name.of("packageA.ClassB"), false, location("packageA/ClassB.java")).addUsage(Name.of("packageB.ClassB"));
		model.addPackage("packageB", null);
		model.addClass(Name.of("packageB.ClassA"), false, location("packageB/ClassA.java")).addUsage(Name.of("packageC.ClassA"));
		model.addClass(Name.of("packageB.ClassB"), false, location("packageB/ClassB.java")).addUsage(Name.of("packageA.ClassB"));
		model.addPackage("packageC", null);
		model.addClass(Name.of("packageC.ClassA"), false, location("packageC/ClassA.java")).addUsage(Name.of("packageB.ClassA"));
		model.addClass(Name.of("packageC.ClassA"), false, location("packageC/ClassA.java")).addUsage(Name.of("packageA.ClassB"));
		
		return model;
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
		
		issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/ClassB.java");
		issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/ClassC.java");
	}
	
	

	@Test
	public void testMultiple() {
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", location("packageA/package-info.java"));
		model.addClass(Name.of("packageA.ClassA"), false, null).addUsage(Name.of("packageB.classA"));
		model.addClass(Name.of("packageA.ClassA"), false, null).addUsage(Name.of("packageC.classA"));
		model.addPackage("packageB", location("packageB/package-info.java"));
		model.addClass(Name.of("packageB.ClassA"), false, null).addUsage(Name.of("packageA.classA"));
		model.addClass(Name.of("packageB.ClassA"), false, null).addUsage(Name.of("packageC.classA"));
		model.addPackage("packageC", location("packageC/package-info.java"));
		model.addClass(Name.of("packageC.ClassA"), false, null).addUsage(Name.of("packageA.classA"));
		model.addClass(Name.of("packageC.ClassA"), false, null).addUsage(Name.of("packageB.classA"));

		subject.scanModel(sensorContext, activeRule, model);

		// Check issues on all packages
		Assert.assertEquals(12, sensorContext.allIssues().size());
		
		// Check measures
		Assert.assertEquals("1,2,3,5", sensorContext.measure(BaseRuleTest.PROJECT_KEY + ":packageA/package-info.java", PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIER).value());
		Assert.assertEquals("1,2,3,4", sensorContext.measure(BaseRuleTest.PROJECT_KEY + ":packageB/package-info.java", PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIER).value());
		Assert.assertEquals("2,3,4,5", sensorContext.measure(BaseRuleTest.PROJECT_KEY + ":packageC/package-info.java", PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIER).value());
			
	}
	
	
	@Test
	public void testFallbackPackageAll() {
		settings.setProperty(PackageAnalyzerProperties.ISSUE_MODE_KEY, PackageAnalyzerProperties.ISSUE_MODE_FALLBACK);
		settings.setProperty(PackageAnalyzerProperties.CLASS_MODE_KEY, PackageAnalyzerProperties.CLASS_MODE_ALL);
		
		final Model<Location> model = createFallbackModel();	
		subject.scanModel(sensorContext, activeRule, model);
		
		//Instead of 7 issues (one for each package involved in a cycle), since no package location is available,
		//there should be 9 issues (all classes affected of each package)
		Assert.assertEquals(9, sensorContext.allIssues().size());
		
		final Map<Issue, String> issues = sensorContext.allIssues().stream().collect(
				Collectors.toMap(Function.identity(), issue -> issue.primaryLocation().inputComponent().key()));
		// There should be 2 issues associated with these classes
		Assert.assertEquals(2, Collections.frequency(issues.values(), BaseRuleTest.PROJECT_KEY + ":packageA/ClassA.java"));
		Assert.assertEquals(2, Collections.frequency(issues.values(), BaseRuleTest.PROJECT_KEY + ":packageA/ClassB.java"));
		Assert.assertEquals(2, Collections.frequency(issues.values(), BaseRuleTest.PROJECT_KEY + ":packageB/ClassA.java"));
		Assert.assertEquals(2, Collections.frequency(issues.values(), BaseRuleTest.PROJECT_KEY + ":packageC/ClassA.java"));
		// There should be 1 issue associated with this class
		Assert.assertEquals(1, Collections.frequency(issues.values(), BaseRuleTest.PROJECT_KEY + ":packageB/ClassB.java"));
		// This class should not have any issue associated
		Assert.assertFalse(issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageC/ClassB.java"));
		
	}
	
	@Test
	public void testFallbackPackageFirst() {
		settings.setProperty(PackageAnalyzerProperties.ISSUE_MODE_KEY, PackageAnalyzerProperties.ISSUE_MODE_FALLBACK);
		settings.setProperty(PackageAnalyzerProperties.CLASS_MODE_KEY, PackageAnalyzerProperties.CLASS_MODE_FIRST);
		
		final Model<Location> model = createFallbackModel();
		
		subject.scanModel(sensorContext, activeRule, model);
		
		// Instead of 3 issues (one for each cycle), since no package location is available,
		//there should be 7 issues (one for each package of each cycle)
		Assert.assertEquals(7, sensorContext.allIssues().size());
		
		final Map<Issue, String> issues = sensorContext.allIssues().stream().collect(
				Collectors.toMap(Function.identity(), issue -> issue.primaryLocation().inputComponent().key()));
		// There should be 2 issues associated with these classes
		Assert.assertEquals(2, Collections.frequency(issues.values(), BaseRuleTest.PROJECT_KEY + ":packageB/ClassA.java"));
		Assert.assertEquals(2, Collections.frequency(issues.values(), BaseRuleTest.PROJECT_KEY + ":packageA/ClassB.java"));
		Assert.assertEquals(2, Collections.frequency(issues.values(), BaseRuleTest.PROJECT_KEY + ":packageC/ClassA.java"));
		// There should be 1 issue associated with this class
		Assert.assertEquals(1, Collections.frequency(issues.values(), BaseRuleTest.PROJECT_KEY + ":packageB/ClassB.java"));
		// These classes should not have any issue associated
		Assert.assertFalse(issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageA/ClassA.java"));
		Assert.assertFalse(issues.containsKey(BaseRuleTest.PROJECT_KEY + ":packageC/ClassB.java"));
		
	}
	
}
