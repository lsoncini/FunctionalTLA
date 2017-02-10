package tla;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Test {
	private static final SortedSet<String> AFD_STATES = new TreeSet<String>(Arrays.asList(new String[]{"A", "B", "C", "D"}));
	private static final SortedSet<String> AFD_FINAL_STATES = new TreeSet<String>(Arrays.asList(new String[]{"A", "B", "C", "D"}));
	private static final String AFD_INITIAL_STATE = "A";
	private static final SortedSet<String> AFD_ALPHABET = new TreeSet<String>(Arrays.asList(new String[]{"0","1","2"}));
	
	private static final SortedSet<String> AFNDL_STATES = new TreeSet<String>(Arrays.asList(new String[]{"A", "B", "C"}));
	private static final SortedSet<String> AFNDL_FINAL_STATES = new TreeSet<String>(Arrays.asList(new String[]{"C"}));
	private static final String AFNDL_INITIAL_STATE = "A";
	private static final SortedSet<String> AFNDL_ALPHABET = new TreeSet<String>(Arrays.asList(new String[]{"0","1","2"}));
	
	private static final Set<String> GR_NT = new HashSet<String>(Arrays.asList(new String[]{"A", "B", "C"}));
	private static final String GR_INITIAL_STATE = "A";
	private static final Set<String> GR_ALPHABET = new HashSet<String>(Arrays.asList(new String[]{"a","b","c"}));

	public static void main(String[] args) {
		System.out.println("----------------------------------------");
		System.out.println("FIRST TEST");
		System.out.println("----------------------------------------");
		AFD afd = new AFD(AFD_ALPHABET, AFD_STATES, AFD_FINAL_STATES, AFD_INITIAL_STATE);
		afd.setDelta("A", "0", "B");
		afd.setDelta("A", "1", "C");
		afd.setDelta("A", "2", "D");
		
		afd.setDelta("B", "0", "B");
		afd.setDelta("B", "1", "C");
		afd.setDelta("B", "2", "D");
		
		afd.setDelta("C", "1", "C");
		afd.setDelta("C", "2", "D");
		
		afd.setDelta("D", "2", "D");
		
		System.out.println(afd);
		//GR gr = new GR(GR_NT, GR_ALPHABET, new HashMap<String, Set<String>>(), GR_INITIAL_STATE);
		System.out.println(afd.toGR());
		
		System.out.println("----------------------------------------");
		System.out.println("SECOND TEST");
		System.out.println("----------------------------------------");
		AFNDL afndl = new AFNDL(AFNDL_ALPHABET, AFNDL_STATES, AFNDL_FINAL_STATES, AFNDL_INITIAL_STATE);
		
		afndl.setDelta("A", "0", new HashSet<String>(Arrays.asList(new String[]{"A"})));
		afndl.setDelta("A", "\\", new HashSet<String>(Arrays.asList(new String[]{"B"})));
		
		afndl.setDelta("B", "1", new HashSet<String>(Arrays.asList(new String[]{"B"})));
		afndl.setDelta("B", "\\", new HashSet<String>(Arrays.asList(new String[]{"C"})));

		afndl.setDelta("C", "2", new HashSet<String>(Arrays.asList(new String[]{"C"})));

		System.out.println(afndl);
		AFND afnd = afndl.toAFND();
		System.out.println(afnd);
		AFD afd2 = afnd.toAFD();
		System.out.println(afd2);
		
	}

}
