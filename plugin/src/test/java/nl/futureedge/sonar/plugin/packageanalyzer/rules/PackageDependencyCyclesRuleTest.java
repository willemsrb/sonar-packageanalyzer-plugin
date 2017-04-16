package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.rule.RuleKey;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Name;

public class PackageDependencyCyclesRuleTest extends BaseRuleTest {

	private PackageDependencyCyclesRule subject = new PackageDependencyCyclesRule();
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

}
