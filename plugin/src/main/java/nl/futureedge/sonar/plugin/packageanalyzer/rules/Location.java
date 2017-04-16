package nl.futureedge.sonar.plugin.packageanalyzer.rules;

import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.TextRange;

/**
 * Location (external link for classes and packages in the model).
 */
public final class Location {

	private final InputComponent on;
	private final TextRange at;
	
	/**
	 * Constructor.
	 * @param on on input component
	 * @param at at location in the file
	 */
	public Location(final InputComponent on, final TextRange at) {
		this.on = on;
		this.at = at;
	}

	public InputComponent getOn() {
		return on;
	}

	public TextRange getAt() {
		return at;
	}
	
	
}
