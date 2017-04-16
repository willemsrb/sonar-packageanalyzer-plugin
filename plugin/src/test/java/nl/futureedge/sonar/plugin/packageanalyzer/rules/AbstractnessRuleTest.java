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

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Name;

public class AbstractnessRuleTest extends BaseRuleTest {

	private AbstractnessRule subject = new AbstractnessRule();
	private SensorContextTester sensorContext = SensorContextTester.create(Paths.get("./src/main/java"));
	private ActiveRule activeRule = Mockito.mock(ActiveRule.class);

	@Before
	public void setup() {
		Mockito.when(activeRule.param("maximum")).thenReturn("75");
		Mockito.when(activeRule.ruleKey()).thenReturn(RuleKey.of("testRepo", "testKey"));
	}

	/**
	 * PackageA -> 3 abstract from 3 classes -> 100% -> Issue
	 * PackageB -> 0 classes -> 0% -> No issue
	 * PackageC -> 1 abstract from 2 classes -> 50% -> No issue
	 */
	@Test
	public void test() {
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", location("packageA/package-info.java"));
		model.addClass(Name.of("packageA.ClassA"), true, null);
		model.addClass(Name.of("packageA.ClassB"), true, null);
		model.addClass(Name.of("packageA.ClassC"), true, null);
		model.addPackage("packageB", location("packageB/package-info.java"));
		model.addPackage("packageC", location("packageC/package-info.java"));
		model.addClass(Name.of("packageC.ClassA"), true, null);
		model.addClass(Name.of("packageC.ClassB"), true, null);
		model.addClass(Name.of("packageC.ClassC"), false, null);

		subject.scanModel(sensorContext, activeRule, model);

		// Check one issue on packageA
		Assert.assertEquals(1, sensorContext.allIssues().size());
		final Issue issue = sensorContext.allIssues().iterator().next();
		Assert.assertEquals(BaseRuleTest.PROJECT_KEY + ":packageA/package-info.java", issue.primaryLocation().inputComponent().key());
	}

}
