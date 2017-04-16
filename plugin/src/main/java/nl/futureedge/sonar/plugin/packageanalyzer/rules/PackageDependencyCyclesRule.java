package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;

/**
 * Package dependency cycles rule.
 */
public class PackageDependencyCyclesRule extends AbstractPackageAnalyzerRule implements PackageAnalyzerRule {

	private static final Logger LOGGER = Loggers.get(NumberOfClassesAndInterfacesRule.class);

	private static final String RULE_KEY = "package-cycle";

	/**
	 * Constructor.
	 */
	public PackageDependencyCyclesRule() {
		super(RULE_KEY);
		LOGGER.info("Instantiating rule");
	}

	@Override
	public void define(final NewRepository repository) {
		repository.createRule(RULE_KEY).setName("Package Dependency Cycles").setHtmlDescription(
				"Package dependency cycles are reported along with the hierarchical paths of packages participating in package dependency cycles.");

	}

	@Override
	public void scanModel(final SensorContext context, final ActiveRule rule, final Model<Location> model) {
	}

}
