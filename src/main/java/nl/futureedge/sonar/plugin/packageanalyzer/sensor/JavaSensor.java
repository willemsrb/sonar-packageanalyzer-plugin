package nl.futureedge.sonar.plugin.packageanalyzer.sensor;

import java.io.IOException;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.java.JavaClasspath;
import org.sonar.java.ast.parser.JavaParser;
import org.sonar.java.resolve.SemanticModel;
import org.sonar.plugins.java.api.semantic.Symbol.TypeSymbol;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.Modifier;
import org.sonar.plugins.java.api.tree.Tree;

import com.sonar.sslr.api.typed.ActionParser;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Name;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.AbstractPackageAnalyzerRule;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.Location;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.PackageAnalyzerRule;

/**
 * Java sensor.
 */
public final class JavaSensor extends AbstractSensor {

	private static final Logger LOGGER = Loggers.get(AbstractPackageAnalyzerRule.class);
	private static final String LANGUAGE = "java";

	private final Settings settings;

	public JavaSensor(final Settings settings, final PackageAnalyzerRule... rules) {
		super(LANGUAGE, rules);
		this.settings = settings;
	}

	@Override
	protected Model<Location> buildModel(final SensorContext context) {
		// Result
		final Model<Location> model = new Model<>();
		final ModelCreatingTreeVisitor visitor = new ModelCreatingTreeVisitor(model);

		// Read all source files
		final ActionParser<Tree> parser = JavaParser.createParser();
		final JavaClasspath javaClassPath = new JavaClasspath(settings, context.fileSystem());
		final FileSystem fs = context.fileSystem();
		for (final InputFile file : fs.inputFiles(fs.predicates().hasLanguage(LANGUAGE))) {
			// Parse source
			LOGGER.debug("Analyzing {}", file.relativePath());
			try {
				visitor.on(file);
				final Tree tree = parser.parse(file.contents());

				// Create semantic model
				SemanticModel.createFor((CompilationUnitTree) tree, javaClassPath.getElements());

				// Read into model
				tree.accept(visitor);

			} catch (IOException e) {
				LOGGER.warn("Could not read contents for {}", file.relativePath(), e);
			}
		}
		return model;
	}

	private static final class ModelCreatingTreeVisitor extends BaseTreeVisitor {
		private final Model<Location> model;
		private InputFile on;
		private Class<Location> modelClass;

		public ModelCreatingTreeVisitor(final Model<Location> model) {
			this.model = model;
		}

		protected void on(final InputFile on) {
			this.on = on;
		}

		@Override
		public void visitClass(final ClassTree tree) {
			final TypeSymbol symbol = tree.symbol();
			if (!symbol.isUnknown()) {
				LOGGER.debug("Adding {} to model", symbol.name());
				final Name name = Name.of(symbol.name());
				final boolean isAbstract = tree.modifiers().contains(Modifier.ABSTRACT);
				final Location location = new Location(on, on.newRange(tree.firstToken().line(),
						tree.firstToken().column(), tree.lastToken().line(), tree.lastToken().column()));

				modelClass = model.addClass(name, isAbstract, location);
			}
			super.visitClass(tree);
		}
	}
}
