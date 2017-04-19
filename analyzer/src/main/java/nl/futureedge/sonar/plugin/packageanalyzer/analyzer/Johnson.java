package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Donald B Johnson's algorithm for finding all the elementary circuits of a
 * directed graph.
 * 
 * @param <T>
 *            Vertex type
 */
public final class Johnson<T> {

	// Input
	// integer list array Ak(n)
	private final Map<T, Set<T>> edges;

	// Variables
	// integer list array B(n)
	private final Map<T, Set<T>> usedEdges = new HashMap<>();
	// logical array blocked(n)
	private final Map<T, Boolean> blocked = new HashMap<>();
	// empty stack
	private final List<T> stack = new ArrayList<>();
	// CUSTOM: Completed edges
	private final List<T> completedVertices = new ArrayList<>();

	// Output
	private List<List<T>> elementaryCircuits;

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
	public Johnson(final Map<T, Set<T>> edges) {
		// Ak := adjacency structure of strong component K with least vertex in
		// subgraph of G induced by {s, s + 1, n}.
		this.edges = edges;

	}

	// procedure UNBLOCK(integer value u)
	private void unblock(final T vertex) {
		// blocked(u) := false
		blocked.put(vertex, false);

		// for w in B(u) do
		// begin
		// delete w from B(u)
		// if blocked(w) then UNBLOCK(w)
		// end
		final Set<T> verticesToUnblock = usedEdges.put(vertex, new HashSet<>());
		for (final T vertexToUnblock : verticesToUnblock) {
			if (blocked.get(vertexToUnblock)) {
				unblock(vertexToUnblock);
			}
		}
	}

	// logical procedure CIRCUIT(integer value v)
	private boolean circuit(final T start, final T currentVertex) {
		// logical f
		// f := false
		boolean result = false;

		// stack v
		stack.add(currentVertex);
		// blocked(v) := true
		blocked.put(currentVertex, true);

		// for w in Ak(v)
		for (final T nextVertex : edges.get(currentVertex)) {
			// do
			// if w = s
			if (nextVertex.equals(start)) {
				// then
				// begin
				// output circuit composed of stack followed by s
				// f := true
				// end
				final List<T> elementaryCircuit = new ArrayList<>(stack);
				// CUSTOM: We skip adding S as it is implicit in the result
				//elementaryCircuit.add(start)

				elementaryCircuits.add(elementaryCircuit);

				result = true;
			} else {
				// else
				// if not blocked(w)
				// then
				// if CIRCUIT(w)
				if (!blocked.get(nextVertex) && circuit(start, nextVertex)) {
					// then
					// f := true
					result = true;
				}
			}
		}

		// if f
		if (result) {
			// then
			// UNBLOCK(v)
			unblock(currentVertex);
		} else {
			// else
			// for w in Ak(v)
			for (final T nextVertex : edges.get(currentVertex)) {
				// do
				// if v not in B(w) then put v on B(w)
				usedEdges.get(nextVertex).add(currentVertex);
			}
		}
		// unstack v
		stack.remove(stack.size() - 1);

		// CIRCUIT := f
		return result;

	}

	private void calculate() {
		elementaryCircuits = new ArrayList<>();
		// s := 1
		// while s < n do
		// begin
		// if Ak != EMPTY
		// else s:= n
		// end
		for (final T vertex : edges.keySet()) {
			// for I in Vk do
			// begin
			// blocked(i) := false
			// B(i) := empty set
			// end
			for (final T clean : edges.keySet()) {
				usedEdges.put(clean, new HashSet<>());

				// Block completed vertices to skip duplicate circuits
				// this implements Vk
				blocked.put(clean, completedVertices.contains(clean));
			}

			circuit(vertex, vertex);

			completedVertices.add(vertex);
		}
	}

	/**
	 * Return (and calculate) the elementary circuits in the given list of
	 * edges.
	 * 
	 * @return List of elementary circuits (a elementary circuits is a list of
	 *         objects from the list of edges; the last object in the list
	 *         implicitly points to the first)
	 */
	public synchronized List<List<T>> getElementaryCircuits() {
		if (elementaryCircuits == null) {
			calculate();
		}

		return elementaryCircuits;
	}
}
