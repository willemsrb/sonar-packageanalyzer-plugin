package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.rule.RuleKey;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Name;

public class MissingPackageInfoRuleTest extends BaseRuleTest {

	private MissingPackageInfoRule subject = new MissingPackageInfoRule();
	private SensorContextTester sensorContext = SensorContextTester.create(Paths.get("./src/main/java"));
	private ActiveRule activeRule = Mockito.mock(ActiveRule.class);

	@Before
	public void setup() {
		Mockito.when(activeRule.param("maximum")).thenReturn("2");
		Mockito.when(activeRule.ruleKey()).thenReturn(RuleKey.of("testRepo", "testKey"));
	}

	/**
	 * PackageA -> contains location -> No issue 
	 * PacakgeB -> no location, 2 classes with location -> 2 issues
	 */
	@Test
	public void test() {
		final Model<Location> model = new Model<>();
		model.addPackage("packageA", location("packageA/package-info.java"));
		model.addClass(Name.of("packageA.ClassA"), true, location("packageA/ClassA.java"));
		model.addClass(Name.of("packageA.ClassB"), true, location("packageA/ClassB.java"));
		model.addPackage("packageB", null);
		model.addClass(Name.of("packageB.ClassA"), true, location("packageB/ClassA.java"));
		model.addClass(Name.of("packageB.ClassB"), true, null);
		model.addClass(Name.of("packageB.ClassC"), true, location("packageB/ClassC.java"));
		subject.scanModel(sensorContext, activeRule, model);

		// Check one issue on packageA
		Assert.assertEquals(2, sensorContext.allIssues().size());

		final List<String> keys = sensorContext.allIssues().stream()
				.map(issue -> issue.primaryLocation().inputComponent().key()).collect(Collectors.toList());
		Assert.assertTrue(keys.contains(BaseRuleTest.PROJECT_KEY + ":packageB/ClassA.java"));
		Assert.assertTrue(keys.contains(BaseRuleTest.PROJECT_KEY + ":packageB/ClassC.java"));
	}
}
