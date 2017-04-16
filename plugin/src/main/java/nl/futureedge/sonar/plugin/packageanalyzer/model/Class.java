package nl.futureedge.sonar.plugin.packageanalyzer.model;

import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Class.
 * 
 * @param <E>
 *            external type
 */
public final class Class<E> implements Comparable<Class<E>> {

	private final Model<E> parentModel;
	private final Package<E> parentPackage;
	private final String name;
	private boolean isAbstract;
	private E external;

	private final SortedSet<Class<E>> classUsages = new TreeSet<>();
	private final SortedSet<Class<E>> usedByClasses = new TreeSet<>();

	/**
	 * Construct a new class.
	 * 
	 * *** not for external use ***
	 * 
	 * @param parentModel
	 *            parent model
	 * @param parentPackage
	 *            parent package
	 * @param name
	 *            class name
	 */
	Class(final Model<E> parentModel, final Package<E> parentPackage, final String name) {
		this.parentPackage = parentPackage;
		this.parentModel = parentModel;
		this.name = name;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return fully qualified name
	 */
	public String getFullyQualifiedName() {
		StringBuilder result = new StringBuilder();
		result.append(parentPackage.getName());
		if (result.length() > 0) {
			result.append(".");
		}
		result.append(name);
		return result.toString();
	}

	/**
	 * Set abstract.
	 * 
	 * *** not for external use ***
	 * 
	 * @param isAbstract
	 *            is the class abstract?
	 */
	void setAbstract(final boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	/**
	 * @return is the class abstract?
	 */
	public boolean isAbstract() {
		return isAbstract;
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
	 * Add a usage on the class.
	 * 
	 * @param usageName
	 *            usage
	 */
	public void addUsage(final Name usageName) {
		final Class<E> usageClass = parentModel.getClass(usageName);

		if (this.parentPackage.getName().equals(usageName.getPackageName())
				&& this.name.equals(usageName.getClassName())) {
			// Ignore self usage
		} else {
			getParentPackage().addPackageUsage(usageClass.getParentPackage());
			classUsages.add(usageClass);
			usageClass.usedByClasses.add(this);
		}
	}

	/**
	 * @return class usages
	 */
	public SortedSet<Class<E>> getUsages() {
		return Collections.unmodifiableSortedSet(classUsages);
	}

	/**
	 * @return used by classes
	 */
	public SortedSet<Class<E>> getUsedByClasses() {
		return Collections.unmodifiableSortedSet(usedByClasses);
	}

	/**
	 * @return parent package
	 */
	public Package<E> getParentPackage() {
		return parentPackage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(parentPackage, name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(final Object that) {
		return that != null && this.getClass() == that.getClass()
				&& Objects.equals(this.parentModel, ((Class<E>) that).parentModel)
				&& Objects.equals(this.parentPackage, ((Class<E>) that).parentPackage)
				&& Objects.equals(this.name, ((Class<E>) that).name);
	}

	@Override
	public String toString() {
		return "Class [package=" + parentPackage.getName() + ", name=" + name + ", external="
				+ (external == null ? "missing" : "filled") + "]";
	}

	@Override
	public int compareTo(Class<E> that) {
		int result = this.parentPackage.compareTo(that.parentPackage);
		if (result == 0) {
			result = this.name.compareTo(that.name);
		}

		return result;
	}
}
