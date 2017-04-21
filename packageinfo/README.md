A little tool to generate package-info.java files; will recurse through directories and create package-info.java files based on the package definition in the <first>.java file in the directory.

Build and run `java -jar target\generate.jar <directory>` where `<directory>` is the directory containing the Java sources (uses current directory if no directory is specified).
