package nl.futureedge.sonar.plugin.packageanalyzer.packageinfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GenerateTest {

	private File baseDirectory;

	@Before
	public void setup() throws IOException {
		baseDirectory = Files.createTempDirectory("packageinfo-generate-test").toFile();
	}

	@After
	public void destroy() {
		deleteDirectory(baseDirectory);
	}

	private void deleteDirectory(final File directory) {
		final File[] children = directory.listFiles();
		for (final File child : children) {
			if (child.isDirectory()) {
				deleteDirectory(child);
			} else {
				System.out.println("Delete: " + child.getAbsolutePath());
				child.delete();
			}
		}
		System.out.println("Delete: " + directory.getAbsolutePath());
		directory.delete();
	}

	@Test
	public void test() throws IOException {
		createFile("base/withinfo/ClassA.java", "package base.withinfo;\n\npublic ClassA {\n}\n");
		createFile("base/withinfo/package-info.java", "base withinfo.second;\n");
		createFile("base/noinfo/ClassA.java", "package base.noinfo;\n\npublic ClassA {\n}\n");

		Generate.main(new String[] { baseDirectory.getAbsolutePath() });

		String contents = readFile("base/noinfo/package-info.java");
		Assert.assertEquals("package base.noinfo;\n", contents);
	}

	@Test
	public void testNotADirectory() throws IOException {
		File file = createFile("base/withinfo/ClassA.java", "package base.withinfo;\n\npublic ClassA {\n}\n");

		Generate.main(new String[] { file.getAbsolutePath() });
	}

	private File createFile(final String filename, final String contents) throws IOException {
		final File file = new File(baseDirectory, filename);
		file.getParentFile().mkdirs();
		try (final FileWriter writer = new FileWriter(file)) {
			writer.append(contents);
		}
		return file;
	}

	private String readFile(String filename) throws IOException  {
		try(final FileReader reader = new FileReader(new File(baseDirectory, filename))) {
			final char[] buffer = new char[256];
			final int length = reader.read(buffer);
			StringBuilder result = new StringBuilder();
			result.append(buffer, 0, length);
			return result.toString();
		}
	}

}
