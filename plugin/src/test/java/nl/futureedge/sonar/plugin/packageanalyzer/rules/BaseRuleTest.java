package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultIndexedFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.fs.internal.DefaultTextRange;
import org.sonar.api.batch.fs.internal.Metadata;

public abstract class BaseRuleTest {

	private static final Path MODULE_BASE_DIR = Paths.get("./src/main/java");
	
	public static final String PROJECT_KEY = "projectKey";

	public Location location(final String relativePath) {
		final DefaultIndexedFile indexedFile = new DefaultIndexedFile(PROJECT_KEY, MODULE_BASE_DIR, relativePath);
		final Metadata metadata = new Metadata(1, 1, "hash", new int[] { 10 }, 20);
		final DefaultInputFile on = new DefaultInputFile(indexedFile, null);
		on.setMetadata(metadata);
		final TextRange at = new DefaultTextRange(new DefaultTextPointer(1,0), new DefaultTextPointer(1, 1));
		return new Location(on, at);
	}
}
