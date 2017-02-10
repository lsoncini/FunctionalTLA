package tla;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Test {
	private static final SortedSet<String> AFD_STATES = new TreeSet<String>(Arrays.asList(new String[]{"A", "B", "C"}));
	private static final SortedSet<String> AFD_FINAL_STATES = new TreeSet<String>(Arrays.asList(new String[]{"A", "B", "C"}));
	private static final String AFD_INITIAL_STATE = "A";
	private static final SortedSet<String> AFD_ALPHABET = new TreeSet<String>(Arrays.asList(new String[]{"a","b","c"}));
	
	private static final Set<String> GR_NT = new HashSet<String>(Arrays.asList(new String[]{"A", "B", "C"}));
	private static final String GR_INITIAL_STATE = "A";
	private static final Set<String> GR_ALPHABET = new HashSet<String>(Arrays.asList(new String[]{"a","b","c"}));

	public static void main(String[] args) {
		
		AFD afd = new AFD(AFD_ALPHABET, AFD_STATES, AFD_FINAL_STATES, AFD_INITIAL_STATE);
		System.out.println(afd);
		//GR gr = new GR(GR_NT, GR_ALPHABET, new HashMap<String, Set<String>>(), GR_INITIAL_STATE);
		System.out.println(afd.toGR());
		
	}

}
