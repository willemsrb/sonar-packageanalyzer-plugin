package nl.futureedge.sonar.plugin.packageanalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultIndexedFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.fs.internal.Metadata;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.MapSettings;
import org.sonar.api.config.Settings;

import nl.futureedge.sonar.plugin.packageanalyzer.java.JavaClasspathProperties;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Class;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.ModelPrinter;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;
import nl.futureedge.sonar.plugin.packageanalyzer.rules.Location;
import nl.futureedge.sonar.plugin.packageanalyzer.sensor.JavaSensor;

public class JavaSensorTest {

	@Test
	public void test() throws ReflectiveOperationException, IOException {
		final Settings settings = new MapSettings();
		final JavaSensor subject = new JavaSensor(settings);

		final File baseDir = new File("./src/test/java");
		System.out.println("Base directory for scanning: " + baseDir.getCanonicalPath());
		StringBuilder libraries = new StringBuilder(); 
		for(String lib : System.getProperty("java.class.path").split(File.pathSeparator)) {
			try {
				Paths.get(lib);
				if(libraries.length() !=0) {
					libraries.append(',');
				}
				libraries.append(lib);
			} catch(InvalidPathException e) {
				// Eclipse add something weird to the classpath. Just ignore it
			}
		}
		
		System.out.println("Libraries: " + libraries);
		settings.setProperty(JavaClasspathProperties.SONAR_JAVA_LIBRARIES, libraries.toString());

		final SensorContextTester context = SensorContextTester.create(baseDir);
		addInputFile(context.fileSystem(), "Foo.java");
		addInputFile(context.fileSystem(), "Bar.java");
		addInputFile(context.fileSystem(), "package-info.java");

		addInputFile(context.fileSystem(), "quux/Xed.java");
		addInputFile(context.fileSystem(), "quux/Yon.java");
		addInputFile(context.fileSystem(), "quux/Zed.java");
		addInputFile(context.fileSystem(), "quux/package-info.java");

		final Method method = JavaSensor.class.getDeclaredMethod("buildModel", SensorContext.class);
		method.setAccessible(true);
		@SuppressWarnings("unchecked")
		final Model<Location> model = (Model<Location>) method.invoke(subject, context);

		ModelPrinter.print(model, System.out);
	}

	

	private void addInputFile(DefaultFileSystem fileSystem, String filename) throws IOException {
		final String contents = read(fileSystem.baseDirPath().toFile(), filename);
		
		final DefaultIndexedFile file = new DefaultIndexedFile("projectKey", fileSystem.baseDirPath(), "src/main/java/nl/futureedge/sonar/plugin/packageanalyzer/test/"+ filename);
		file.setLanguage("java");

		Metadata metadata = new FileMetadata().readMetadata(new StringReader(contents));

		fileSystem.add(new MyInputFile(file, metadata, contents));
	}
	
	private String read(File baseDir, String filename) throws IOException {
		File file =new File(baseDir, "/nl/futureedge/sonar/plugin/packageanalyzer/test/" + filename);
		System.out.println("Reading: " +  file.getCanonicalPath());
		try(InputStream is = new FileInputStream(file)) {
			return IOUtils.toString(is);
		}
	}

	private static final class MyInputFile extends DefaultInputFile {

		String contents;

		public MyInputFile(DefaultIndexedFile indexedFile, Metadata metadata, String contents) {
			super(indexedFile, null);
			this.setMetadata(metadata);
			this.contents = contents;
		}

		@Override
		public String contents() throws IOException {
			return contents;
		}
	}
}
