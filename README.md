# Package analyzer plugin for SonarQube [![Build Status](https://travis-ci.org/willemsrb/sonar-packageanalyzer-plugin.svg?branch=master)](https://travis-ci.org/willemsrb/sonar-packageanalyzer-plugin) [![Quality Gate](https://sonarqube.com/api/badges/gate?key=nl.future-edge.sonarqube.plugins:sonar-packageanalyzer)](https://sonarqube.com/dashboard/index?id=nl.future-edge.sonarqube.plugins%3Asonar-packageanalyzer)
*Requires SonarQube 6.3*

This plugin analyzes the package structure of your modules.

## Rules
- **Abstractness**  
The ratio of the number of abstract classes (and interfaces) in the analyzed package compared to the total number of classes in the analyze package. The range for this value is 0% to 100%, with A=0% indicating a completely concrete package and A=100% indicating a completely abstract package.

- **Afferent coupling**  
The number of other packages that depend upon classes within the package is an indicator of the package's responsibility.

- **Efferent coupling**  
The number of other packages that the classes in the package depend upon is an indicator of the package's independence.

- **Instability**  
The ratio of efferent coupling (Ce) to total coupling (Ce + Ca) such that I = Ce / (Ce + Ca). This value is an indicator of the package's resilience to change. The range for this value is 0 to 100%, with I=0% indicating a completely stable package and I=100% indicating a completely instable package.

- **Number of classes**  
The number of concrete and abstract classes (and interfaces) in the package is an indicator of the extensibility of the package.

- **Package cycles**  
All elementary package cycles are and reported on all packages, along with the classes participating in the package cycle.

- **Missing package-info.java** (java language only)  
Reports missing package-info.java files on all classes the in analyzed package.

## Issues
Issues raised by rules can be registered on the following locations:
- **Package level only**  
Only registers an issue if a package level component exists (for example: package-info.java for Java projects).

- **Classes level only**  
Registers the issue on (relevant) classes in the package.

- **Package level, fallback to class level** (default setting)  
Register an issue on the package level if possible, else register the issue on classes.

When registering an issue on classes the following can be configured:
- **All classes** (default)
Register the issue on all (relevant) classes in the package.

- **First class only**
Register the issue on the first (relevant) class in the package.

## Metrics
- **Package cycles**  
Number of elementary package cycles.

## Languages
- **Java**
Java is supported through the SonarQube Java Parser (and the SonarQube SSLR)

