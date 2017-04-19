package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import org.junit.Assert;

public class JohnsonTest {

	@Test
	public void testStackOverflowExample() {
		final Map<String, Set<String>> input = new HashMap<>();
		input.put("Node 0", new HashSet<>(Arrays.asList("Node 1", "Node 2")));
		input.put("Node 1", new HashSet<>(Arrays.asList("Node 0", "Node 2")));
		input.put("Node 2", new HashSet<>(Arrays.asList("Node 0", "Node 1")));

		final List<List<String>> circuits = new Johnson<>(input).getElementaryCircuits();
		final List<List<String>> ordered = orderCircuits(circuits);
		printResult(ordered);
		
		Assert.assertEquals(
				Arrays.asList(
						Arrays.asList("Node 0", "Node 1"),
						Arrays.asList("Node 0", "Node 1", "Node 2"),
						Arrays.asList("Node 0", "Node 2"),
						Arrays.asList("Node 0", "Node 2", "Node 1"),
						Arrays.asList("Node 1", "Node 2")						
						)
				, ordered);
	}

	@Test
	public void testTarjanWikipediaExample() {
		final Map<String, Set<String>> input = new HashMap<>();
		input.put("Node 1", new HashSet<>(Arrays.asList("Node 2")));
		input.put("Node 2", new HashSet<>(Arrays.asList("Node 3")));
		input.put("Node 3", new HashSet<>(Arrays.asList("Node 1")));
		input.put("Node 4", new HashSet<>(Arrays.asList("Node 3", "Node 5")));
		input.put("Node 5", new HashSet<>(Arrays.asList("Node 4", "Node 6")));
		input.put("Node 6", new HashSet<>(Arrays.asList("Node 3", "Node 7")));
		input.put("Node 7", new HashSet<>(Arrays.asList("Node 6")));
		input.put("Node 8", new HashSet<>(Arrays.asList("Node 5", "Node 7", "Node 8")));

		final List<List<String>> circuits = new Johnson<>(input).getElementaryCircuits();
		final List<List<String>> ordered = orderCircuits(circuits);
		printResult(ordered);
		
		Assert.assertEquals(
				Arrays.asList(
						Arrays.asList("Node 1", "Node 2", "Node 3"),
						Arrays.asList("Node 4", "Node 5"),
						Arrays.asList("Node 6", "Node 7"),
						Arrays.asList("Node 8")				
						)
				, ordered);	
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
		final List<List<String>> ordered = orderCircuits(circuits);
		printResult(ordered);
		
		Assert.assertEquals(
				Arrays.asList(
						Arrays.asList("Node 1", "Node 2", "Node 3", "Node 4"),
						Arrays.asList("Node 1", "Node 2", "Node 4")
						)
				, ordered);
	}

	@Test
	public void testOrder() {
		Assert.assertEquals(Arrays.asList("1", "2", "3"), Arrays.asList("1", "2", "3"));
		Assert.assertNotEquals(Arrays.asList("1", "2", "3"), Arrays.asList("2", "1", "3"));
		Assert.assertNotEquals(Arrays.asList("1", "2", "3"), orderCircuit( Arrays.asList("2", "1", "3")));
		Assert.assertNotEquals(Arrays.asList("1", "2", "3"), Arrays.asList("2", "3", "1"));
		Assert.assertEquals(Arrays.asList("1", "2", "3"), orderCircuit(Arrays.asList("2", "3", "1")));
	}

	public static final List<List<String>> orderCircuits(List<List<String>> circuits) {
		List<List<String>> result = new ArrayList<>();
		for (List<String> circuit : circuits) {
			result.add(orderCircuit(circuit));
		}

		result.sort((o1, o2) -> o1.stream().collect(Collectors.joining())
				.compareTo(o2.stream().collect(Collectors.joining())));

		return result;
	}

	public static final List<String> orderCircuit(List<String> circuit) {
		List<String> result = new ArrayList<>(circuit);
		
		String first = result.stream().sorted().findFirst().get();
		
		while(!first.equals(result.get(0))) {
			result.add(result.remove(0));
		}
		return result;
	}

	private void printResult(final List<List<String>> components) {
		System.out.println("Components: ");
		for (List<String> component : components) {
			System.out.print("Component: ");
			for (String part : component) {
				System.out.print(part + ", ");
			}
			System.out.println();
		}
		System.out.println("-----");
	}
}
