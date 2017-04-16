package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Robert Tarjan's algorithm for finding strongly connected components in a
 * directed graph.
 *
 * @param <T>
 *            Vertex type
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm">Tarjan's
 *      strongly connected components algorithm on Wikipedia</a>
 */
public final class Tarjan<T> {

	// Input
	private Set<Vertex<T>> vertices;
	private Map<Vertex<T>, Set<Vertex<T>>> edges;

	// Variables
	private int index = 0;
	private List<Vertex<T>> stack = new ArrayList<>();

	// Output
	private List<List<T>> stronglyConnectedComponents;

	/**
	 * Construct an algorithm solver using the given edges (Map&lt;fromVertex,
	 * Set&lt;toVertex>>).
	 * 
	 * Note: the given vertex type need to implement hashCode and equals
	 * correctly.
	 * 
	 * @param edges
	 *            edges
	 */
	public Tarjan(final Map<T, Set<T>> edges) {
		final Map<T, Vertex<T>> verticesMap = createVerticesMap(edges);
		this.vertices = new HashSet<>(verticesMap.values());
		this.edges = createEdges(verticesMap, edges);
	}

	/**
	 * Create a list of all (unique) vertices based on the given edges.
	 * 
	 * @param edges
	 *            edges
	 * @return list of all vertices
	 */
	private static <T> Map<T, Vertex<T>> createVerticesMap(final Map<T, Set<T>> edges) {
		final Map<T, Vertex<T>> result = new HashMap<>();
		for (final Map.Entry<T, Set<T>> edge : edges.entrySet()) {
			final T from = edge.getKey();
			result.put(from, new Vertex<T>(from));
			for (final T to : edge.getValue()) {
				result.put(to, new Vertex<T>(to));
			}
		}

		return result;
	}

	/**
	 * Create a list of edges (for all vertices, even if the vertex is only
	 * referenced as 'to' in an edge).
	 * 
	 * @param vertices
	 *            vertices to use
	 * @param edges
	 *            edges
	 * @return edges
	 */
	private static <T> Map<Vertex<T>, Set<Vertex<T>>> createEdges(final Map<T, Vertex<T>> verticesMap,
			final Map<T, Set<T>> edges) {
		final Map<Vertex<T>, Set<Vertex<T>>> result = new HashMap<>();
		for (final Vertex<T> vertex : verticesMap.values()) {
			result.put(vertex, new HashSet<>());
		}

		for (final Map.Entry<T, Set<T>> edge : edges.entrySet()) {
			final T from = edge.getKey();
			final Set<Vertex<T>> resultSet = result.get(verticesMap.get(from));
			for (final T to : edge.getValue()) {
				resultSet.add(verticesMap.get(to));
			}
		}
		return result;
	}

	private void calculate() {
		stronglyConnectedComponents = new ArrayList<>();

		for (Vertex<T> vertex : vertices) {
			if (vertex.index == null) {
				strongconnect(vertex);
			}
		}
	}

	private void strongconnect(final Vertex<T> vertex) {
		// Set the depth index for v to the smallest unused index
		vertex.index = index;
		vertex.lowlink = index;
		index++;
		stack.add(vertex);
		vertex.onstack = true;

		// Consider successors of v
		for (final Vertex<T> successor : edges.get(vertex)) {
			if (successor.index == null) {
				// Successor w has not yet been visited; recurse on it
				strongconnect(successor);
				vertex.lowlink = Math.min(vertex.lowlink, successor.lowlink);
			} else if (successor.onstack) {
				// Successor w is in stack S and hence in the current SSC
				vertex.lowlink = Math.min(vertex.lowlink, successor.lowlink);
			}
		}

		// If v is a root node, pop the stack and generate a SCC
		if (vertex.lowlink == vertex.index) {
			// Start a new strongly connected component
			final List<T> stronglyConnectedComponent = new ArrayList<>();

			Vertex<T> successor;
			do {
				successor = stack.remove(stack.size() - 1);
				successor.onstack = false;
				// Add w to current strongly connected component
				stronglyConnectedComponent.add(successor.external);

			} while (!vertex.equals(successor));

			// Output the current strongly connected component
			stronglyConnectedComponents.add(stronglyConnectedComponent);
		}
	}

	/**
	 * Return (and calculate) the strongly connected components in the given
	 * list of edges.
	 * 
	 * @return List of strongly connected components (a strongly connected
	 *         component is a list of objects from the list of edges)
	 */
	public synchronized List<List<T>> getStronglyConnectedComponents() {
		if (stronglyConnectedComponents == null) {
			calculate();
		}

		return stronglyConnectedComponents;
	}

	/**
	 * Vertex.
	 *
	 * @param <T>
	 *            object type
	 */
	private static final class Vertex<T> {

		final T external;

		Integer index;
		Integer lowlink;
		boolean onstack;

		public Vertex(final T external) {
			this.external = external;
		}

		@Override
		public int hashCode() {
			return Objects.hash(external);
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(final Object that) {
			return that != null && this.getClass() == that.getClass()
					&& Objects.equals(this.external, ((Vertex<T>) that).external);
		}
	}

}
