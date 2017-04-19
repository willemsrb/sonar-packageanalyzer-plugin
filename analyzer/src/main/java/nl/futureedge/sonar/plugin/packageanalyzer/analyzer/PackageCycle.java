package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import java.util.List;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

/**
 * Package cycle.
 * 
 * @param <T> package external type
 */
public final class PackageCycle<T> {

	private final List<Package<T>> packagesInCycle;

	/**
	 * Construct a package cycle.
	 * 
	 * *** not for external use ***
	 * 
	 * @param packagesInCycle
	 *            packages in the cycle
	 */
	PackageCycle(final List<Package<T>> packagesInCycle) {
		this.packagesInCycle = packagesInCycle;
	}

	/**
	 * @return the packages in the cycle (last listed package references the
	 *         first).
	 */
	public List<Package<T>> getPackagesInCycle() {
		return packagesInCycle;
	}
}
