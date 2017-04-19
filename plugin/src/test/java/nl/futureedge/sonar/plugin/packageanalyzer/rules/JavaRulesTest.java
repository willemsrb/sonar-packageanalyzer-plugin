package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;

public class JavaRulesTest {

	@Test
	public void test() {
		JavaRules subject = new JavaRules(
				new AbstractnessRule(),
				new AfferentCouplingRule(),
				new EfferentCouplingRule(),
				new InstabilityRule(),
				new MissingPackageInfoRule(),
				new NumberOfClassesAndInterfacesRule(),
				new PackageDependencyCyclesRule()
				);
		
		RulesDefinition.Context context = new RulesDefinition.Context();
		subject.define(context);
	}
}
