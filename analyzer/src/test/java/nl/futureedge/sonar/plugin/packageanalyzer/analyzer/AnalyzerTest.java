package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Name;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

public class AnalyzerTest {

	@Test
	public void test() {
		Model<String> model = new Model<>();
		model.addPackage("packageA", "packageA");
		model.addPackage("packageB", "packageB");
		model.addPackage("packageC", "packageC");

		Class<String> classA = model.addClass(Name.of("packageA.ClassA"), false, "ClassA");
		classA.addUsage(Name.of("packageB.ClassB"));
		classA.addUsage(Name.of("packageC.ClassC"));

		Class<String> classB = model.addClass(Name.of("packageB.ClassB"), false, "ClassB");
		classB.addUsage(Name.of("packageA.ClassA"));
		classB.addUsage(Name.of("packageC.ClassC"));

		Class<String> classC = model.addClass(Name.of("packageC.ClassC"), false, "ClassC");
		classC.addUsage(Name.of("packageA.ClassA"));
		classC.addUsage(Name.of("packageB.ClassB"));

		Analyzer<String> analyzer = new Analyzer<>();
		List<PackageCycle<String>> packageCycles = analyzer.findPackageCycles(model);
		
		List<List<String>> result = new ArrayList<>();
		for(PackageCycle<String> packageCycle : packageCycles) {
			List<String> cycle = new ArrayList<>();
			for(Package<String> packageInCycle : packageCycle.getPackagesInCycle()) {
				cycle.add(packageInCycle.getExternal());
			}
			result.add(cycle);
		}
		
		List<List<String>> ordered = JohnsonTest.orderCircuits(result);
		
		Assert.assertEquals(
				Arrays.asList(
						Arrays.asList("packageA", "packageB"),
						Arrays.asList("packageA", "packageB", "packageC"),
						Arrays.asList("packageA", "packageC"),
						Arrays.asList("packageA", "packageC", "packageB"),
						Arrays.asList("packageB", "packageC")						
						)
				, ordered);

	}

}
