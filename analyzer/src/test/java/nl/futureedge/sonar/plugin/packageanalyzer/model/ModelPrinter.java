package nl.futureedge.sonar.plugin.packageanalyzer.model;

import java.io.PrintStream;

public class ModelPrinter {

	public static void print(Model<?> model, PrintStream out) {
		out.println("Model:");
		for (final Package<?> aPackage : model.getPackages()) {
			out.println(aPackage);
			for(final Package<?> usesPackage: aPackage.getPackageUsages()) {
				out.println(" - uses " + usesPackage);
			}
			for(final Package<?> usedByPackage: aPackage.getUsedByPackages()) {
				out.println(" - used by " + usedByPackage);
			}
			
			for (final Class<?> aClass : aPackage.getClasses()) {
				out.println(" - " + aClass);
				
				for(final Class<?> usesClass : aClass.getUsages()) {
					out.println("    - uses " + usesClass);
				}
				for(final Class<?> usedByClass : aClass.getUsedByClasses()) {
					out.println("    - used by " + usedByClass);
				}
			}
		}		
	}
}
