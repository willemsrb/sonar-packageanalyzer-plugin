package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import nl.futureedge.sonar.plugin.packageanalyzer.analyzer.Analyzer;
import nl.futureedge.sonar.plugin.packageanalyzer.analyzer.PackageCycle;
import nl.futureedge.sonar.plugin.packageanalyzer.metrics.PackageAnalyzerMetrics;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

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
		}

	@Override
	public void define(final NewRepository repository) {
		repository.createRule(RULE_KEY).setType(RuleType.CODE_SMELL).setSeverity(Severity.CRITICAL)
				.setName("Package Dependency Cycles").setHtmlDescription(
						"Package dependency cycles are reported along with the hierarchical paths of packages participating in package dependency cycles.");
	}

	@Override
	public void scanModel(final SensorContext context, final ActiveRule rule, final Model<Location> model) {
		// Analyze
		final Analyzer<Location> analyzer = new Analyzer<>();
		final List<PackageCycle<Location>> packageCycles = analyzer.findPackageCycles(model);
		LOGGER.debug("Package cycles: {}", packageCycles.size());

		int packageCycleIdentifier = 0;
		final Map<Package<Location>,StringBuilder> identifierMeasures = new HashMap<>();

		// Rule
		for (final PackageCycle<Location> packageCycle : packageCycles) {
			packageCycleIdentifier++;
			for (final Package<Location> packageInCycle : packageCycle.getPackagesInCycle()) {
				if(identifierMeasures.containsKey(packageInCycle)) {
					identifierMeasures.get(packageInCycle).append(",").append(Integer.toString(packageCycleIdentifier));
				} else {
					identifierMeasures.put(packageInCycle, new StringBuilder(Integer.toString(packageCycleIdentifier)));
				}
				
				final String message = formatMessage(packageCycle, packageInCycle);
				registerIssue(context, rule, packageInCycle, message);
			}
		}
		
		// Measures
		for(final Map.Entry<Package<Location>, StringBuilder> measure : identifierMeasures.entrySet()) {
			registerMeasure(context, PackageAnalyzerMetrics.PACKAGE_DEPENDENCY_CYCLES_IDENTIFIER, measure.getKey(),
					measure.getValue().toString());
		}
	}

	private String formatMessage(final PackageCycle<Location> packageCycle, final Package<Location> forPackage) {
		final StringBuilder message = new StringBuilder();
		message.append("Break the package cycle containing the following cycle of packages: ");

		final List<Package<Location>> packagesInCycle = cycleToPackage(packageCycle.getPackagesInCycle(), forPackage);
		for (int i = 0; i < packagesInCycle.size(); i++) {
			final Package<Location> packageFrom = packagesInCycle.get(i);
			final Package<Location> packageTo = i == packagesInCycle.size() - 1 ? packagesInCycle.get(0)
					: packagesInCycle.get(i + 1);
			
			if(i!=0) {
				message.append(", ");
			}
			message.append("").append(packageFrom.getName()).append(" (");
			boolean firstClass=true;
			for (final Class<Location> classInPackage : packageFrom.getClasses()) {
				if(firstClass) {
					firstClass=false;
				} else {
					message.append(", ");
				}
				Set<Class<Location>> usages = getUsagesOnPackage(classInPackage, packageTo);
				if (!usages.isEmpty()) {
					message.append(classInPackage.getName()).append(" references ");
					message.append(usages.stream().map(Class::getName).collect(Collectors.joining(", ")));
				}
			}
			message.append(")");
		}

		return message.toString();
	}

	private List<Package<Location>> cycleToPackage(final List<Package<Location>> packagesInCycle,
			final Package<Location> forPackage) {
		final List<Package<Location>> result = new ArrayList<>(packagesInCycle);

		while (!result.get(0).equals(forPackage)) {
			result.add(result.remove(0));
		}

		return result;
	}

	private Set<Class<Location>> getUsagesOnPackage(final Class<Location> classInPackage,
			final Package<Location> packageTo) {
		final Set<Class<Location>> result = new HashSet<>();
		for (final Class<Location> usage : classInPackage.getUsages()) {
			if (usage.getParentPackage().getName().equals(packageTo.getName())) {
				result.add(usage);
			}
		}

		return result;
	}

}
