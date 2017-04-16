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

public class InstabilityRuleTest extends BaseRuleTest {

	private InstabilityRule subject = new InstabilityRule();
	private SensorContextTester sensorContext = SensorContextTester.create(Paths.get("./src/main/java"));
	private ActiveRule activeRule = Mockito.mock(ActiveRule.class);

	@Before
	public void setup() {
		Mockito.when(activeRule.param("maximum")).thenReturn("75");
		Mockito.when(activeRule.ruleKey()).thenReturn(RuleKey.of("testRepo", "testKey"));
	}

	/**
	 * Package A: Uses 1 package; used by 0 packages -> 1 / 1 -> 100% -> Issue
	 * Package B: Uses 1 package; used by 1 package -> 1 / 2 -> 50% -> No issue
	 * Package C: Uses 0 packages; used by 1 package -> 0 / 1 -> 0% -> No issue
	 * Package D: Uses 0 packages; used by 0 packages -> No issue
	 */
	@Test
	public void test() {
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", location("packageA/package-info.java"));
		Class<Location> classA = model.addClass(Name.of("packageA.ClassA"), false , null); 
		classA.addUsage(Name.of("packageB.ClassA"));
		model.addPackage("packageB", location("packageB/package-info.java"));
		Class<Location> classB = model.addClass(Name.of("packageB.ClassA"), false , null); 
		classB.addUsage(Name.of("packageC.ClassA"));
		model.addPackage("packageC", location("packageC/package-info.java"));
		model.addPackage("packageD", location("packageD/package-info.java"));

		subject.scanModel(sensorContext, activeRule, model);

		// Check one issue on packageA
		Assert.assertEquals(1, sensorContext.allIssues().size());
		final Issue issue = sensorContext.allIssues().iterator().next();
		Assert.assertEquals(BaseRuleTest.PROJECT_KEY + ":packageA/package-info.java",
				issue.primaryLocation().inputComponent().key());
	}

}
