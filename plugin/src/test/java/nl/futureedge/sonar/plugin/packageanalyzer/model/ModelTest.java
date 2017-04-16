package nl.futureedge.sonar.plugin.packageanalyzer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class ModelTest {

	@Test
	public void test() {
		Model<String> model = new Model<>();
		Class<String> classA = model.addClass(Name.of("nl.futureedge.ClassA"), false, "classAExternal");
		classA.addUsage(Name.of("nl.futureedge.ClassB"));
		classA.addUsage(Name.of("nl.futureedge.model.ClassK"));
		classA.addUsage(Name.of("nl.futureedge.util.ClassX"));
		classA.addUsage(Name.of("InTheDefaultPackage"));

		model.addPackage("nl.futureedge", "packageExternal");

		Class<String> classB = model.addClass(Name.of("nl.futureedge.ClassB"), true, "classBExternal");
		classB.addUsage(Name.of("nl.futureedge.ClassA"));
		classB.addUsage(Name.of("nl.futureedge.ClassB"));

		Class<String> classX = model.addClass(Name.of("nl.futureedge.util.ClassX"), false, "classXExternal");
		classX.addUsage(Name.of("nl.futureedge.ClassA"));

		ModelPrinter.print(model, System.out);

		Assert.assertEquals(4, model.getPackages().size());
		final Map<String, Package<String>> packages = model.getPackages().stream()
				.collect(Collectors.toMap(Package::getName, Function.identity()));
		assertContainsPackageUsages(packages, "nl.futureedge", "nl.futureedge.model", "nl.futureedge.util", "");
		assertContainsPackageUsages(packages, "nl.futureedge.model");
		assertContainsPackageUsages(packages, "nl.futureedge.util", "nl.futureedge");

		Assert.assertEquals("packageExternal", packages.get("nl.futureedge").getExternal());
		Assert.assertEquals(null, packages.get("nl.futureedge.model").getExternal());

		Assert.assertEquals(2, packages.get("nl.futureedge").getClasses().size());
		final Map<String, Class<String>> classes = packages.get("nl.futureedge").getClasses().stream()
				.collect(Collectors.toMap(Class::getName, Function.identity()));
		assertContainsClassUsages(classes, "ClassA", "nl.futureedge.ClassB", "nl.futureedge.model.ClassK",
				"nl.futureedge.util.ClassX", ".InTheDefaultPackage");
		assertContainsClassUsages(classes, "ClassB", "nl.futureedge.ClassA");

		Assert.assertEquals("classBExternal", classes.get("ClassB").getExternal());
		Assert.assertEquals(true, classes.get("ClassB").isAbstract());
		Assert.assertEquals(false, classes.get("ClassA").isAbstract());
	}

	@Test
	public void testEquals() {
		final Model<String> model1 = new Model<>();
		final Model<String> model2 = new Model<>();
		Assert.assertEquals(model1, model1);
		Assert.assertNotEquals(model1, model2);

		final Package<String> package1a = model1.addPackage("a", null);
		final Package<String> package1a2 = model1.addPackage("a", "does not matter");
		final Package<String> package1b = model1.addPackage("b", null);
		final Package<String> package2a = model2.addPackage("a", null);
		Assert.assertEquals(package1a, package1a);
		Assert.assertEquals(package1a2, package1a2);
		Assert.assertNotEquals(package1a, null);
		Assert.assertNotEquals(package1a, new Object());
		Assert.assertNotEquals(package1a, package1b);
		Assert.assertNotEquals(package1a, package2a);
		
		final Class<String> class1aa = package1a.addClass("a", true, null);
		final Class<String> class1aa2 = package1a.addClass("a", false, "does not matter");
		final Class<String> class1ab = package1a.addClass("b", true, null);
		final Class<String> class1ba = package1b.addClass("a", true, null);
		final Class<String> class2aa = package2a.addClass("a", true, null);
		Assert.assertEquals(class1aa, class1aa);
		Assert.assertEquals(class1aa, class1aa2);
		Assert.assertNotEquals(class1aa, null);
		Assert.assertNotEquals(class1aa, new Object());
		Assert.assertNotEquals(class1aa, class1ab);
		Assert.assertNotEquals(class1aa, class1ba);
		Assert.assertNotEquals(class1aa, class2aa);
	}

	private void assertContainsPackageUsages(Map<String, Package<String>> packages, String packageName,
			String... packageUsages) {
		Package<String> packageToTest = packages.get(packageName);
		Assert.assertNotNull("Package " + packageName + " does not exist in the model", packageToTest);

		Set<String> packageUsagesInPackageToTest = packageToTest.getPackageUsages().stream().map(Package::getName)
				.collect(Collectors.toSet());
		List<String> packageUsagesToTest = new ArrayList<>(Arrays.asList(packageUsages));

		packageUsagesToTest.removeAll(packageUsagesInPackageToTest);
		Assert.assertTrue("Package " + packageName + " does not contain usages: " + packageUsagesToTest,
				packageUsagesToTest.isEmpty());

		packageUsagesInPackageToTest.removeAll(Arrays.asList(packageUsages));
		Assert.assertTrue("Package " + packageName + " also contains usages: " + packageUsagesInPackageToTest,
				packageUsagesInPackageToTest.isEmpty());
	}

	private void assertContainsClassUsages(Map<String, Class<String>> classes, String className,
			String... classUsages) {
		Class<String> classToTest = classes.get(className);
		Assert.assertNotNull("Class " + className + " does not exist in the package", classToTest);

		Set<String> classUsagesInClassToTest = classToTest.getUsages().stream()
				.map(clazz -> clazz.getParentPackage().getName() + "." + clazz.getName()).collect(Collectors.toSet());
		List<String> classUsagesToTest = new ArrayList<>(Arrays.asList(classUsages));

		classUsagesToTest.removeAll(classUsagesInClassToTest);
		Assert.assertTrue("Class " + className + " does not contain usages: " + classUsagesToTest,
				classUsagesToTest.isEmpty());

		classUsagesInClassToTest.removeAll(Arrays.asList(classUsages));
		Assert.assertTrue("Class " + className + " also contains usages: " + classUsagesInClassToTest,
				classUsagesInClassToTest.isEmpty());

	}

}
