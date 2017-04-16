package nl.futureedge.sonar.plugin.packageanalyzer.test.quux;

import nl.futureedge.sonar.plugin.packageanalyzer.test.Bar;

public enum Zed{
	
	ONE;
	
	private Bar bar;
	
	private Zed() {
		bar = new Bar();
	}
}