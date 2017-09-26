package nl.futureedge.sonar.plugin.packageanalyzer.settings;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Qualifiers;

/**
 * Package analyzer properties.
 */
public final class PackageAnalyzerProperties {

	/**
	 * Categorie.
	 */
	private static final String CATEGORY = "Package analyzer";

	/**
	 * Issue mode.
	 */
	public static final String ISSUE_MODE_KEY = "sonar.package-analyzer.issue-mode";
	/**
	 * Issue mode: packages.
	 */
	public static final String ISSUE_MODE_PACKAGE = "packages";
	/**
	 * Issue mode: fallback.
	 */
	public static final String ISSUE_MODE_FALLBACK = "fallback";
	/**
	 * Issue mode: classes.
	 */
	public static final String ISSUE_MODE_CLASS = "classes";

	/**
	 * Class mode.
	 */
	public static final String CLASS_MODE_KEY = "sonar.package-analyzer.class-mode";
	/**
	 * Class mode: first.
	 */
	public static final String CLASS_MODE_FIRST = "first";
	/**
	 * Class mode; all.
	 */
	public static final String CLASS_MODE_ALL = "all";

	private PackageAnalyzerProperties() {
		// Not instantiable
	}

	/**
	 * Property definitions.
	 *
	 * @return property definitions
	 */
	public static List<PropertyDefinition> definitions() {
		final PropertyDefinition issueMode = PropertyDefinition.builder(ISSUE_MODE_KEY).name("Issue mode")
				.description(
						"How to report issues (only on packages, on packages and fallback to classes, only on classes)")
				.category(CATEGORY).type(PropertyType.SINGLE_SELECT_LIST)
				.options(ISSUE_MODE_PACKAGE, ISSUE_MODE_FALLBACK, ISSUE_MODE_CLASS).defaultValue(ISSUE_MODE_FALLBACK)
				.index(100).onQualifiers(Qualifiers.PROJECT).build();

		final PropertyDefinition classMode = PropertyDefinition.builder(CLASS_MODE_KEY).name("Class mode")
				.description(
						"How to report issues when using class modes (on all classes, on the first class in the package)")
				.category(CATEGORY).type(PropertyType.SINGLE_SELECT_LIST).options(CLASS_MODE_ALL, CLASS_MODE_FIRST)
				.defaultValue(CLASS_MODE_ALL).index(200).onQualifiers(Qualifiers.PROJECT).build();

		return Arrays.asList(issueMode, classMode);
	}

	/**
	 * Should issues be registered using fallback?
	 *
	 * @param settings
	 *            settings
	 * @return true, if issues should be registered using fallback
	 */
	 public static boolean shouldRegisterOnFallback(final Settings settings) {
		final String issueMode = settings.getString(ISSUE_MODE_KEY);
		return ISSUE_MODE_FALLBACK.equals(issueMode);
	 }
	 
	/**
	 * Should issues be registered on packages?
	 *
	 * @param settings
	 *            settings
	 * @return true, if issues should be registered on packages
	 */
	public static boolean shouldRegisterOnPackage(final Settings settings) {
		final String issueMode = settings.getString(ISSUE_MODE_KEY);
		return ISSUE_MODE_PACKAGE.equals(issueMode) || ISSUE_MODE_FALLBACK.equals(issueMode);
	}

	/**
	 * Should issues be registered on classes?
	 *
	 * @param settings
	 *            settings
	 * @return true, if issues should be registered on classes
	 */
	public static boolean shouldRegisterOnClasses(final Settings settings) {
		final String issueMode = settings.getString(ISSUE_MODE_KEY);
		return ISSUE_MODE_CLASS.equals(issueMode) || ISSUE_MODE_FALLBACK.equals(issueMode);
	}

	/**
	 * Should issues be registered on all classes (or the first only)?
	 *
	 * @param settings
	 *            settings
	 * @return true, if issues should be registered on all classes
	 */
	public static boolean shouldRegisterOnAllClasses(final Settings settings) {
		final String classMode = settings.getString(CLASS_MODE_KEY);
		return CLASS_MODE_ALL.equals(classMode);
	}
}
