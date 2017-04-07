package nl.futureedge.sonar.plugin.packageanalyzer.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class.
 * 
 * @param <E> external type
 */
public final class Class<E> {

	private final Model<E> parentModel;
	private final Package<E> parentPackage;
	private final String name;
	private boolean isAbstract;
	private E external;

	private final Set<Class<E>> classUsages = new HashSet<>();

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
		getParentPackage().addPackageUsage(usageClass.getParentPackage());
		classUsages.add(usageClass);
	}

	/**
	 * @return class usages
	 */
	public Set<Class<E>> getUsages() {
		return Collections.unmodifiableSet(classUsages);
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
		return that != null 
				&& this.getClass() == that.getClass()
				&& Objects.equals(this.parentModel, ((Class<E>) that).parentModel)
				&& Objects.equals(this.parentPackage, ((Class<E>) that).parentPackage)
				&& Objects.equals(this.name, ((Class<E>) that).name);
	}

	@Override
	public String toString() {
		return "Class [package=" + parentPackage.getName() + ", name=" + name + "]";
	}

}
