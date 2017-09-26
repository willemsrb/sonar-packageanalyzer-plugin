package nl.futureedge.sonar.plugin.packageanalyzer.metrics;

import org.junit.Assert;
import org.junit.Test;

public class PackageAnalyzerMetricsTest {

	@Test
	public void test() {
		Assert.assertEquals(6,  new PackageAnalyzerMetrics().getMetrics().size());
	}
	
}
