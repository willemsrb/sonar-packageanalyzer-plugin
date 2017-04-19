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
 * 
 * @param <T> package external type
 */
public final class Analyzer<T> {

	/**
	 * Find package cycles in the given model.
	 * 
	 * @param model
	 *            model
	 * @return package cycles
	 */
	public List<PackageCycle<T>> findPackageCycles(final Model<T> model) {
		final Map<Package<T>, Set<Package<T>>> edges = createEdges(model.getPackages());
		final List<List<Package<T>>> elementaryCircuits = new Johnson<>(edges).getElementaryCircuits();

		return createPackageCycles(elementaryCircuits);

	}

	/**
	 * Create directed graph edges from package usages.
	 * 
	 * @param packages 
	 *            packages
	 * @return graph edges
	 */
	private Map<Package<T>, Set<Package<T>>> createEdges(final Set<Package<T>> packages) {
		final Map<Package<T>, Set<Package<T>>> result = new HashMap<>();
		for (final Package<T> fromPackage : packages) {
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
	private List<PackageCycle<T>> createPackageCycles(final List<List<Package<T>>> elementaryCircuits) {
		final List<PackageCycle<T>> result = new ArrayList<>();
		for (final List<Package<T>> elementaryCircuit : elementaryCircuits) {
			result.add(new PackageCycle<>(elementaryCircuit));
		}

		return result;
	}
}
