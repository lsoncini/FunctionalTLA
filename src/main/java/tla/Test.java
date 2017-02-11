package tla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	
	private static final Set<String> GR_NT = new HashSet<String>(Arrays.asList(new String[]{"S", "T", "V", "X", "Q"}));
	private static final String GR_INITIAL_STATE = "S";
	private static final Set<String> GR_ALPHABET = new HashSet<String>(Arrays.asList(new String[]{"a","b","c","d","e"}));
	
	private static final Set<String> GR2_NT = new HashSet<String>(Arrays.asList(new String[]{"S", "A", "B"}));
	private static final String GR2_INITIAL_STATE = "S";
	private static final Set<String> GR2_ALPHABET = new HashSet<String>(Arrays.asList(new String[]{"a","b"}));

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
		
		System.out.println("----------------------------------------");
		System.out.println("THIRD TEST");
		System.out.println("----------------------------------------\n");
		System.out.println("-----------PART 1: AFD -> GR------------\n");
		System.out.println(afd2.toGR());
		System.out.println("-----------PART 2: AFND -> GR-----------\n");
		System.out.println(afnd.toGR());
		System.out.println("-----------PART 3: AFNDL -> GR----------\n");
		System.out.println(afndl.toGR());
		
		System.out.println("----------------------------------------");
		System.out.println("FOURTH TEST");
		System.out.println("----------------------------------------\n");
		System.out.println("-----------PART 1: AFD -> AFND----------\n");
		System.out.println(afd2.toAFND());
		System.out.println("-----------PART 2: AFD -> AFNDL---------\n");
		System.out.println(afd2.toAFNDL());
		System.out.println("-----------PART 3: AFND -> AFNDL--------\n");
		System.out.println(afnd.toAFNDL());
		
		System.out.println("----------------------------------------");
		System.out.println("FIFTH TEST");
		System.out.println("----------------------------------------\n");
		Map<String, Set<String>> predicates = new HashMap<String, Set<String>>();
		predicates.put("S", new HashSet<String>(Arrays.asList(new String[]{"T a","V b","b","X c","e"})));
		predicates.put("T", new HashSet<String>(Arrays.asList(new String[]{"T a","V b","b"})));
		predicates.put("V", new HashSet<String>(Arrays.asList(new String[]{"V b","b"})));
		predicates.put("X", new HashSet<String>(Arrays.asList(new String[]{"X c","Q d"})));
		predicates.put("Q", new HashSet<String>(Arrays.asList(new String[]{"c"})));
		GR gr = new GR(GR_NT, GR_ALPHABET, predicates, GR_INITIAL_STATE);
		System.out.println(gr);
		System.out.println("\n\nRIGHT LINEAR GRAMMAR:\n\n");
		System.out.println(gr.toRight());
		
		System.out.println("----------------------------------------");
		System.out.println("SIXTH TEST");
		System.out.println("----------------------------------------\n");
		Map<String, Set<String>> predicates2 = new HashMap<String, Set<String>>();
		predicates2.put("S", new HashSet<String>(Arrays.asList(new String[]{"A a","B b"})));
		predicates2.put("A", new HashSet<String>(Arrays.asList(new String[]{"\\","b"})));
		predicates2.put("B", new HashSet<String>(Arrays.asList(new String[]{"\\","a"})));
		
		GR gr2 = new GR(GR2_NT, GR2_ALPHABET, predicates2, GR2_INITIAL_STATE);
		System.out.println(gr2);
		System.out.println("\n\nRIGHT LINEAR GRAMMAR:\n\n");
		System.out.println(gr2.toRight());
	}

}
