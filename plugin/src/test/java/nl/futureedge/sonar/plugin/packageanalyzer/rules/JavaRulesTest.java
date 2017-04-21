package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import org.junit.Test;
import org.sonar.api.config.MapSettings;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.server.rule.RulesDefinition;

import nl.futureedge.sonar.plugin.packageanalyzer.settings.PackageAnalyzerProperties;

public class JavaRulesTest {
	private Settings settings = new MapSettings(new PropertyDefinitions(PackageAnalyzerProperties.definitions()));

	@Test
	public void test() {
		JavaRules subject = new JavaRules(
				new AbstractnessRule(settings),
				new AfferentCouplingRule(settings),
				new EfferentCouplingRule(settings),
				new InstabilityRule(settings),
				new MissingPackageInfoRule(),
				new NumberOfClassesAndInterfacesRule(settings),
				new PackageDependencyCyclesRule(settings)
				);
		
		RulesDefinition.Context context = new RulesDefinition.Context();
		subject.define(context);
	}
}
