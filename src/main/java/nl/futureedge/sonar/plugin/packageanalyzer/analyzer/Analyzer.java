package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

/**
 * Analyzer.
 */
public final class Analyzer {

	/**
	 * Find package cycles in the given model.
	 * 
	 * @param model
	 *            model
	 * @return package cycles
	 */
	public List<PackageCycle> findPackageCycles(final Model model) {
		final Map<Package, Set<Package>> edges = createEdges(model.getPackages());
		final List<List<Package>> elementaryCircuits = new Johnson<>(edges).getElementaryCircuits();

		return createPackageCycles(elementaryCircuits);

	}

	/**
	 * Create directed graph edges from package usages.
	 * 
	 * @param packages
	 *            packages
	 * @return graph edges
	 */
	private Map<Package, Set<Package>> createEdges(final Set<Package> packages) {
		final Map<Package, Set<Package>> result = new HashMap<>();
		for (final Package fromPackage : packages) {
			result.put(fromPackage, fromPackage.getPackageUsages());
		}
		return result;
	}

	/**
	 * Map elementary circuits to package cycles.
	 * 
	 * @param elementaryCircuits
	 *            elementary circuits
	 * @return package cycles
	 */
	private List<PackageCycle> createPackageCycles(final List<List<Package>> elementaryCircuits) {
		final List<PackageCycle> result = new ArrayList<>();
		for (final List<Package> elementaryCircuit : elementaryCircuits) {
			result.add(new PackageCycle(elementaryCircuit));
		}

		return result;
	}
}
