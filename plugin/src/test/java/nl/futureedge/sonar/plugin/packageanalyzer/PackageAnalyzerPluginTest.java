package nl.futureedge.sonar.plugin.packageanalyzer;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

public class PackageAnalyzerPluginTest {

	@Test
	public void test() {
		final PackageAnalyzerPlugin subject = new PackageAnalyzerPlugin();
		final Plugin.Context context = new Plugin.Context(SonarRuntimeImpl.forSonarQube(Version.create(5, 6), SonarQubeSide.SERVER));

		Assert.assertEquals(0, context.getExtensions().size());
		subject.define(context);
		Assert.assertTrue(context.getExtensions().size() > 0);
	}

}
