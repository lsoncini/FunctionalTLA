package tla;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

public class AFDTest {

	private static final SortedSet<String> STATES = new TreeSet<String>(Arrays.asList(new String[]{"A", "B", "C"}));
	private static final SortedSet<String> FINAL_STATES = new TreeSet<String>(Arrays.asList(new String[]{"A", "B", "C"}));
	private static final String INITIAL_STATE = "A";
	private static final SortedSet<String> ALPHABET = new TreeSet<String>(Arrays.asList(new String[]{"a","b","c"}));
	
	private AFD afd;
	@Before
	public void populate() {
		afd = new AFD(ALPHABET, STATES, FINAL_STATES, INITIAL_STATE);
		afd.setDelta("A", "a", delta)
	}
	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
