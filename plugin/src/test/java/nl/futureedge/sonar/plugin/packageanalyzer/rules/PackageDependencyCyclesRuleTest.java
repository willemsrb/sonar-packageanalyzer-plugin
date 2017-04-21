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
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", location("packageA/package-info.java"));
		model.addClass(Name.of("packageA.ClassB"), true, null).addUsage(Name.of("packageB.classB"));
		model.addClass(Name.of("packageA.ClassD"), true, null).addUsage(Name.of("packageB.classC"));
		Class<Location> classAA = model.addClass(Name.of("packageA.ClassA"), true, null);
		classAA.addUsage(Name.of("packageB.classD"));
		classAA.addUsage(Name.of("packageB.classC"));
		classAA.addUsage(Name.of("packageB.classE"));
		classAA.addUsage(Name.of("packageB.classX"));
		model.addClass(Name.of("packageA.ClassC"), true, null).addUsage(Name.of("packageB.classB"));
		model.addClass(Name.of("packageB.ClassA"), true, null).addUsage(Name.of("packageA.classA"));

		subject.scanModel(sensorContext, activeRule, model);

		// Check one issue on packageA
		Assert.assertEquals(1, sensorContext.allIssues().size());
		final Issue issue = sensorContext.allIssues().iterator().next();
		System.out.println("Message: " + issue.primaryLocation().message());
		Assert.assertEquals(BaseRuleTest.PROJECT_KEY + ":packageA/package-info.java",
				issue.primaryLocation().inputComponent().key());
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
	
}
