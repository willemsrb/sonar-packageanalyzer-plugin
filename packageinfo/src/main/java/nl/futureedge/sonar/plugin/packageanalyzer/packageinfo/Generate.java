package nl.futureedge.sonar.plugin.packageanalyzer.packageinfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generate package-info.java files.
 */
public final class Generate {

	private static final Logger LOGGER = Logger.getLogger("Generator");
	private static final Pattern PACKAGE_DEFINITION_PATTERN = Pattern.compile("package.*?;");
	private static final String PACKAGE_INFO = "package-info.java";
	
	public static void main(final String[] args) {
		final File baseDirectory = new File(args.length == 0 ? "." : args[0]);
		if (!baseDirectory.isDirectory()) {
			LOGGER.log(Level.SEVERE, baseDirectory.getAbsolutePath() + " is not a directory.");
		} else {
			new Generate().handleDirectory(baseDirectory);
		}
	}

	private  void handleDirectory(final File directory) {
		LOGGER.info("Scanning " + directory.getAbsolutePath());
		if (!directoryContainsPackageInfo(directory)) {
			final String classContents = readFirstJavaFile(directory);
			final String packageDefinition = findPackageDefinition(classContents);
			if (packageDefinition != null) {
				createPackageInfo(directory, packageDefinition);
			}
		}

		Arrays.stream(directory.listFiles(File::isDirectory)).forEach(this::handleDirectory);
	}

	private  boolean directoryContainsPackageInfo(final File directory) {
		return directory.listFiles((file, name) -> PACKAGE_INFO.equals(name)).length > 0;
	}

	private String readFirstJavaFile(final File directory) {
		final File[] javaFiles = directory.listFiles((file, name) -> name.endsWith(".java"));
		final StringBuilder result = new StringBuilder();
		if (javaFiles.length > 0) {
			try (final FileReader reader = new FileReader(javaFiles[0])) {
				final char[] buffer = new char[2048];
				final int length = reader.read(buffer);
				result.append(buffer, 0, length);
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "Could not read " + javaFiles[0].getAbsolutePath(), e);
			}
		}
		return result.toString();
	}

	private String findPackageDefinition(final String classContents) {
		final Matcher matcher = PACKAGE_DEFINITION_PATTERN.matcher(classContents);
		if (matcher.find()) {
			return matcher.group();
		} else {
			return null;
		}
	}

	private void createPackageInfo(final File directory, final String packageDefinition) {
		final File packageInfoFile = new File(directory, PACKAGE_INFO);
		LOGGER.log(Level.INFO, "Creating " + packageInfoFile.getAbsolutePath());
		try (final FileWriter writer = new FileWriter(packageInfoFile)) {
			writer.write(packageDefinition);
			writer.write("\n");
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "Could not create " + packageInfoFile.getAbsolutePath(), e);
		}

	}
}
