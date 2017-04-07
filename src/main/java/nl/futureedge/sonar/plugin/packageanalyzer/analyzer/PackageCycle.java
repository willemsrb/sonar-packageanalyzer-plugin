package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import java.util.List;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

/**
 * Package cycle.
 */
public final class PackageCycle {

	private final List<Package> packagesInCycle;

	/**
	 * Construct a package cycle.
	 * 
	 * *** not for external use ***
	 * 
	 * @param packagesInCycle
	 *            packages in the cycle
	 */
	PackageCycle(final List<Package> packagesInCycle) {
		this.packagesInCycle = packagesInCycle;
	}

	/**
	 * @return the packages in the cycle (last listed package references the
	 *         first).
	 */
	public List<Package> getPackagesInCycle() {
		return packagesInCycle;
	}
}
