package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.server.ws.Definable;
import org.sonarsource.api.sonarlint.SonarLintSide;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;

/**
 * Marker interface for all rules.
 */
@ServerSide
@ScannerSide
@ComputeEngineSide
@SonarLintSide
public interface PackageAnalyzerRule extends Definable<RulesDefinition.NewRepository> {

	/**
	 * Scan the model for issues.
	 * 
	 * @param context
	 *            context (to register issues)
	 * @param language
	 *            language
	 * @param model
	 */
	void scanModel(SensorContext context, String language, Model<Location> model);

	/**
	 * Defining remediation times for the rule.
	 * @param rule
	 * 			Rule to be defined
	 */
	void defineRemediationTimes(final NewRule rule);
	
	/**
	 * Does this rule support the given language?
	 * 
	 * @param language
	 *            language
	 * @return default true
	 */
	default boolean supportsLanguage(String language) {
		return true;
	}
}
