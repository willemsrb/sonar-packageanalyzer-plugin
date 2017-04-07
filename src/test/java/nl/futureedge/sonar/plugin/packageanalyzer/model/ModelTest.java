package nl.futureedge.sonar.plugin.packageanalyzer.model;

import java.io.PrintStream;

import org.junit.Test;

public class ModelTest {

	@Test
	public void test() {
		Model<Void> model = new Model<>();
		Class<Void> classA = model.addClass(Name.of("nl.futureedge.ClassA"), false, null);
		classA.addUsage(Name.of("nl.futureedge.ClassB"));
		classA.addUsage(Name.of("nl.futureedge.model.ClassK"));
		classA.addUsage(Name.of("nl.futureedge.util.ClassX"));

		Class<Void> classB = model.addClass(Name.of("nl.futureedge.ClassB"), false, null);
		classB.addUsage(Name.of("nl.futureedge.ClassA"));

		Class<Void> classX = model.addClass(Name.of("nl.futureedge.util.ClassX"), false, null);
		classX.addUsage(Name.of("nl.futureedge.ClassA"));

		print(model, System.out);
	}

	private void print(Model<?> model, PrintStream out) {
		for (final Package<?> aPackage : model.getPackages()) {
			out.println(aPackage.toString());
			for (final Class<?> aClass : aPackage.getClasses()) {
				out.println(" - " + aClass.toString());
				for (final Class<?> aUsage : aClass.getUsages()) {
					out.println("    - " + aUsage.toString());
				}
			}
		}
	}
}
