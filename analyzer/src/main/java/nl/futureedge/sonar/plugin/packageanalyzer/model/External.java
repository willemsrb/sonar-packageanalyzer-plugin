package nl.futureedge.sonar.plugin.packageanalyzer.model;

/**
 * This object contains a link to an external model.
 * 
 * @param <E>
 *            external type
 */
public interface External<E> {

	/**
	 * @return the external object
	 */
	public E getExternal();
}
