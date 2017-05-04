package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import org.sonar.api.server.rule.RulesDefinition;

/**
 * Base rules definition.
 */
public class BaseRules implements RulesDefinition {

	private static final String BASE_KEY = "package-analyzer-";
	private static final String NAME = "Package analyzer";

	private final String language;
	private final PackageAnalyzerRule[] rules;

	/**
	 * Construct rules definition.
	 * 
	 * @param language
	 *            language to register rules for.
	 * @param rules
	 *            rules
	 */
	protected BaseRules(final String language, final PackageAnalyzerRule... rules) {
		this.language = language;
		this.rules = rules;
	}

	/**
	 * Create the correct repository key based on the language.
	 * 
	 * @param language
	 *            language
	 * @return repository key
	 */
	public static String getRepositoryKey(final String language) {
		return BASE_KEY + language;
	}

	/**
	 * Define rules.
	 * 
	 * @param contex
	 *            rules definition context
	 */
	@Override
	public final void define(final Context context) {
		final NewRepository repository = context.createRepository(getRepositoryKey(language), language).setName(NAME);

		for (final PackageAnalyzerRule rule : rules) {
			if (rule.supportsLanguage(language)) {
				rule.define(repository);
			}
		}

		repository.done();
	}
}
