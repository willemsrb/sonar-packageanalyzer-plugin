package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class JohnsonTest {

	@Test
	public void testStackOverflowExample() {
		final Map<String,Set<String>> input = new HashMap<>();
		input.put("Node 0", new HashSet<>(Arrays.asList("Node 1", "Node 2")));
		input.put("Node 1", new HashSet<>(Arrays.asList("Node 0", "Node 2")));
		input.put("Node 2", new HashSet<>(Arrays.asList("Node 0", "Node 1")));

		final List<List<String>> circuits = new Johnson<>(input).getElementaryCircuits();
		printResult(circuits);
	}
	
	@Test
	public void testTarjanWikipediaExample() {
		final Map<String,Set<String>> input = new HashMap<>();
		input.put("Node 1", new HashSet<>(Arrays.asList("Node 2")));
		input.put("Node 2", new HashSet<>(Arrays.asList("Node 3")));
		input.put("Node 3", new HashSet<>(Arrays.asList("Node 1")));
		input.put("Node 4", new HashSet<>(Arrays.asList("Node 3", "Node 5")));
		input.put("Node 5", new HashSet<>(Arrays.asList("Node 4", "Node 6")));
		input.put("Node 6", new HashSet<>(Arrays.asList("Node 3", "Node 7")));
		input.put("Node 7", new HashSet<>(Arrays.asList("Node 6")));
		input.put("Node 8", new HashSet<>(Arrays.asList("Node 5", "Node 7", "Node 8")));

		final List<List<String>> circuits = new Johnson<>(input).getElementaryCircuits();
		printResult(circuits);
	}
	
	@Test
	public void test() {
		final Map<String, Set<String>> input = new HashMap<>();
		input.put("Node 1", new HashSet<>(Arrays.asList("Node 2")));
		input.put("Node 2", new HashSet<>(Arrays.asList("Node 3", "Node 4")));
		input.put("Node 3", new HashSet<>(Arrays.asList("Node 4")));
		input.put("Node 4", new HashSet<>(Arrays.asList("Node 1")));
		input.put("Node 5", Collections.emptySet());

		Johnson<String> johson = new Johnson<>(input);
		johson.getElementaryCircuits();
		final List<List<String>> circuits = johson.getElementaryCircuits();
		printResult(circuits);
	}
	
	private void printResult(final List<List<String>> components) {
		for(List<String> component : components) {
			System.out.print("Component: ");
			for(String part : component) {
				System.out.print(part + ", ");
			}
			System.out.println();
		}
	}
}
