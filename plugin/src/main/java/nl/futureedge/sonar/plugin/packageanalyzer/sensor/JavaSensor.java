package nl.futureedge.sonar.plugin.packageanalyzer.sensor;

import java.io.IOException;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.sonar.sslr.api.typed.ActionParser;

import nl.futureedge.sonar.plugin.packageanalyzer.java.JavaClasspath;
import nl.futureedge.sonar.plugin.packageanalyzer.java.api.semantic.Symbol;
import nl.futureedge.sonar.plugin.packageanalyzer.java.api.semantic.Symbol.TypeSymbol;
import nl.futureedge.sonar.plugin.packageanalyzer.java.api.tree.BaseTreeVisitor;
import nl.futureedge.sonar.plugin.packageanalyzer.java.api.tree.ClassTree;
import nl.futureedge.sonar.plugin.packageanalyzer.java.api.tree.CompilationUnitTree;
import nl.futureedge.sonar.plugin.packageanalyzer.java.api.tree.IdentifierTree;
import nl.futureedge.sonar.plugin.packageanalyzer.java.api.tree.Modifier;
import nl.futureedge.sonar.plugin.packageanalyzer.java.api.tree.PackageDeclarationTree;
import nl.futureedge.sonar.plugin.packageanalyzer.java.api.tree.Tree;
import nl.futureedge.sonar.plugin.packageanalyzer.java.ast.parser.JavaParser;
import nl.futureedge.sonar.plugin.packageanalyzer.java.resolve.SemanticModel;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Name;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.Location;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.PackageAnalyzerRule;

/**
 * Java sensor.
 */
public final class JavaSensor extends AbstractSensor {

	private static final Logger LOGGER = Loggers.get(JavaSensor.class);
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

		// Scan only main files for the 'current' language.
		final FilePredicate filesToScan = fs.predicates().and(fs.predicates().hasType(Type.MAIN),
				fs.predicates().hasLanguage(LANGUAGE));

		for (final InputFile file : fs.inputFiles(filesToScan)) {
			// Parse source
			LOGGER.debug("Analyzing source file: {}", file.relativePath());
			try {
				visitor.on(file);
				final Tree tree = parser.parse(file.contents());

				// Create semantic model
				LOGGER.debug("Creating semantic model ...");
				SemanticModel.createFor((CompilationUnitTree) tree, javaClassPath.getElements());

				// Read into model
				LOGGER.debug("Reading model ...");
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

		private boolean inPackageName;
		private StringBuilder packageName = new StringBuilder();

		public ModelCreatingTreeVisitor(final Model<Location> model) {
			this.model = model;
		}

		protected void on(final InputFile on) {
			this.on = on;
		}

		@Override
		public void visitPackage(final PackageDeclarationTree tree) {
			LOGGER.debug("Package tree: {}", tree);

			if (on.relativePath().endsWith("package-info.java")) {
				inPackageName = true;
				packageName.setLength(0);
				super.visitPackage(tree);
				inPackageName = false;

				LOGGER.debug("Adding package {} to model", packageName);
				final Location location = new Location(on, on.newRange(tree.firstToken().line(),
						tree.firstToken().column(), tree.lastToken().line(), tree.lastToken().column()));
				model.addPackage(packageName.toString(), location);
			}
		}

		@Override
		public void visitIdentifier(final IdentifierTree tree) {
			if (inPackageName) {
				if (packageName.length() > 0) {
					packageName.append('.');
				}
				packageName.append(tree.name());
			} else {
				LOGGER.debug("Identifier {}", tree);
				final Symbol symbol = tree.symbol();
				if (!symbol.isUnknown() && symbol.isTypeSymbol()) {
					final String fqn = symbol.type().fullyQualifiedName();
					LOGGER.debug("Adding class usage {}", fqn);
					modelClass.addUsage(Name.of(fqn));
				}
			}
		}

		@Override
		public void visitClass(final ClassTree tree) {
			LOGGER.debug("Class tree: {}", tree);
			final TypeSymbol symbol = tree.symbol();
			if (!symbol.isUnknown()) {
				LOGGER.debug("Adding class {} to model", symbol.type().fullyQualifiedName());
				final Name name = Name.of(symbol.type().fullyQualifiedName());
				final boolean isAbstract = tree.modifiers().contains(Modifier.ABSTRACT);
				final Location location = new Location(on, on.newRange(tree.firstToken().line(),
						tree.firstToken().column(), tree.lastToken().line(), tree.lastToken().column()));

				modelClass = model.addClass(name, isAbstract, location);
			}
			super.visitClass(tree);
		}
	}
}
