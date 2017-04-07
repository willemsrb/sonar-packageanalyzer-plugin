package nl.futureedge.sonar.plugin.packageanalyzer.rules;

/**
 * Java rules definition.
 */
public class JavaRules extends Rules {

	private static final String LANGUAGE = "java";
	
	/**
	 * Constructor.
	 */
	public JavaRules(final PackageAnalyzerRule... rules) {
		super(LANGUAGE, rules);
	}

}
