package nl.futureedge.sonar.plugin.packageanalyzer;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import nl.futureedge.sonar.plugin.packageanalyzer.rules.AbstractnessRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.AfferentCouplingRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.EfferentCouplingRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.InstabilityRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.JavaRules;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.MissingPackageInfoRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.NumberOfClassesAndInterfacesRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.PackageDependencyCyclesRule;
import nl.futureedge.sonar.plugin.packageanalyzer.sensor.JavaSensor;

public class PackageAnalyzerPluginTest {

	@Test
	public void test() {
		final PackageAnalyzerPlugin subject = new PackageAnalyzerPlugin();
		final Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(5, 6), SonarQubeSide.SERVER));

		Assert.assertEquals(0, context.getExtensions().size());
		subject.define(context);
		
		// Rules
		Assert.assertTrue(context.getExtensions().contains(AbstractnessRule.class));
		Assert.assertTrue(context.getExtensions().contains(AfferentCouplingRule.class));
		Assert.assertTrue(context.getExtensions().contains(EfferentCouplingRule.class));
		Assert.assertTrue(context.getExtensions().contains(InstabilityRule.class));
		Assert.assertTrue(context.getExtensions().contains(MissingPackageInfoRule.class));
		Assert.assertTrue(context.getExtensions().contains(NumberOfClassesAndInterfacesRule.class));
		Assert.assertTrue(context.getExtensions().contains(PackageDependencyCyclesRule.class));
		
		// Java
		Assert.assertTrue(context.getExtensions().contains(JavaRules.class));
		Assert.assertTrue(context.getExtensions().contains(JavaSensor.class));
		
	}

}
