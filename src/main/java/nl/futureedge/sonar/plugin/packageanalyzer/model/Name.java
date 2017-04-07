package nl.futureedge.sonar.plugin.packageanalyzer.model;

/**
 * Name.
 */
public final class Name {

	private final String packageName;
	private final String className;

	/**
	 * Construct a new name.
	 * 
	 * @param packageName
	 *            package name
	 * @param className
	 *            class name
	 */
	public Name(final String packageName, final String className) {
		this.packageName = packageName;
		this.className = className;
	}

	/**
	 * Construct a new name based on a (Java) fully qualified name.
	 * 
	 * @param fullyQualifiedName
	 *            fully qualified name
	 * @return name
	 */
	public static Name of(final String fullyQualifiedName) {
		final int lastPoint = fullyQualifiedName.lastIndexOf('.');
		if (lastPoint == -1) {
			return new Name("", fullyQualifiedName);
		} else {
			return new Name(fullyQualifiedName.substring(0, lastPoint), fullyQualifiedName.substring(lastPoint + 1));
		}
	}

	/**
	 * @return package name
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return class name
	 */
	public String getClassName() {
		return className;
	}
}