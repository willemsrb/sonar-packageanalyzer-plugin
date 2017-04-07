package nl.futureedge.sonar.plugin.packageanalyzer.model;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Model.
 * 
 * @param <E> external type 
 */
public final class Model<E> {

	private Map<String, Package<E>> packages = new TreeMap<>();

	/**
	 * Add a class (and if needed its package) to the model.
	 * 
	 * @param name
	 *            package and class name
	 * @param isAbstract
	 *            is the class abstract?
	 * @param external
	 *            external object to register on the class
	 *            {@link Class#getExternal()}
	 * @return class
	 */
	public Class<E> addClass(final Name name, final boolean isAbstract, final E external) {
		return getPackage(name.getPackageName()).addClass(name.getClassName(), isAbstract, external);
	}

	/**
	 * Add a package to the model.
	 * 
	 * @param packageName
	 *            package name
	 * @param external
	 *            external object to register on the package
	 *            {@link Package#getExternal()}
	 * @return package
	 */
	public Package<E> addPackage(final String packageName, final E external) {
		final Package<E> thePackage = getPackage(packageName);
		thePackage.setExternal(external);
		return thePackage;
	}

	/**
	 * Get a package from the model; create the package if it doesn't exist.
	 * 
	 * *** not for external use ***
	 * 
	 * @param packageName
	 *            package name
	 */
	Package<E> getPackage(final String packageName) {
		if (!packages.containsKey(packageName)) {
			packages.put(packageName, new Package<>(this, packageName));
		}
		return packages.get(packageName);
	}

	/**
	 * Get a class from the model; create the package and the class if they
	 * don't exist.
	 * 
	 * *** not for external use ***
	 * 
	 * @param name
	 *            package and class name
	 * @return class
	 */
	Class<E> getClass(final Name name) {
		return getPackage(name.getPackageName()).getClass(name.getClassName());
	}

	/**
	 * @return packages
	 */
	public Set<Package<E>> getPackages() {
		return packages.values().stream().collect(Collectors.toSet());
	}

}
