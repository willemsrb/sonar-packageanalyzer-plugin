package nl.futureedge.sonar.plugin.packageanalyzer.model;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Package.
 * 
 * @param <E> external type
 */
public final class Package<E> {

	private final Model<E> parentModel;
	private final String name;
	private E external;

	private final Map<String, Class<E>> classes = new TreeMap<>();

	private final Set<Package<E>> packageUsages = new HashSet<>();

	/**
	 * Construct a new package.
	 * 
	 * *** not for external use ***
	 *
	 * @param parentModel
	 *            parent model
	 * @param name
	 *            package name
	 */
	Package(final Model<E> parentModel, final String name) {
		this.parentModel = parentModel;
		this.name = name;
	}

	/**
	 * @return package name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the external object registered on the class
	 */
	public E getExternal() {
		return external;
	}

	/**
	 * Set the external object.
	 * 
	 * *** not for external use ***
	 * 
	 * @param external
	 *            external object
	 */
	void setExternal(final E external) {
		this.external = external;
	}

	/**
	 * @return classes
	 */
	public Set<Class<E>> getClasses() {
		return new LinkedHashSet<>(classes.values());
	}

	/**
	 * Get (create it if it doesn't exist) a class.
	 * 
	 * *** not for external use ***
	 *
	 * @param className
	 *            class name
	 * @return class
	 */
	Class<E> getClass(final String className) {
		if (!classes.containsKey(className)) {
			classes.put(className, new Class<>(parentModel, this, className));
		}
		return classes.get(className);
	}

	/**
	 * Add a class to the package.
	 * 
	 * @param name
	 *            class name
	 * @param isAbstract
	 *            is the class abstract?
	 * @param classExternal
	 *            external object to register on the class
	 *            {@link Class#getExternal()}
	 * @return class
	 */
	public Class<E> addClass(final String className, final boolean isAbstract, final E classExternal) {
		final Class<E> theClass = getClass(className);
		theClass.setExternal(classExternal);
		theClass.setAbstract(isAbstract);
		return theClass;
	}

	/**
	 * Add a package usage.
	 * 
	 * *** not for external use ***
	 *
	 * @param usage
	 *            package usage
	 */
	void addPackageUsage(final Package<E> usage) {
		packageUsages.add(usage);
	}

	/**
	 * @return package usages
	 */
	public Set<Package<E>> getPackageUsages() {
		return packageUsages;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object that) {
		return that != null && this.getClass() == that.getClass()
				&& Objects.equals(this.parentModel, ((Package<E>) that).parentModel)
				&& Objects.equals(this.name, ((Package<E>) that).name);
	}

	@Override
	public String toString() {
		return "Package [name=" + name + "]";
	}


}
