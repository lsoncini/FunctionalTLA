package tla;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GR {

	protected List<String> nonTerminals;
	protected List<String> terminals;
	protected Map<String, List<String>> predicates;
	protected String initialState;

	public GR(final List<String> nt, final List<String> t, final Map<String, List<String>> p, final String is) {
		this.nonTerminals = nt;
		this.terminals = t;
		this.predicates = p;
		this.initialState = is;
	}

	/*
	 * 
	 * Getters & Setters.
	 * 
	 */

	public List<String> getNonTerminals() {
		return nonTerminals;
	}

	public void setNonTerminals(List<String> nonTerminals) {
		this.nonTerminals = nonTerminals;
	}

	public List<String> getTerminals() {
		return terminals;
	}

	public void setTerminals(List<String> terminals) {
		this.terminals = terminals;
	}

	public Map<String, List<String>> getPredicates() {
		return predicates;
	}

	public void setPredicates(Map<String, List<String>> predicates) {
		this.predicates = predicates;
	}

	public String getInitialState() {
		return initialState;
	}

	public void setInitialState(String initialState) {
		this.initialState = initialState;
	}

	public GR toRight() {
		boolean isGRD = true;
		Map<String, List<String>> ps = new HashMap<String, List<String>>();
		List<String> lambda = new ArrayList<>();
		lambda.add("\\");
		String initial = getNextState();
		ps.put(initialState, lambda);
		for (Entry<String, List<String>> p : predicates.entrySet()) {
			for (String s : p.getValue()) {
				String[] strings = s.split(" ");
				if (nonTerminals.contains(strings[0]) && strings.length > 1) {
					List<String> oldList = ps.getOrDefault(strings[0], new ArrayList<>());
					oldList.add(s.substring(strings[0].length()).concat(p.getKey()));
					ps.put(strings[0], oldList);
					isGRD = false;
				} else if (strings.length == 1) {
					List<String> newStateList = ps.getOrDefault(initial, new ArrayList<>());
					
					if (strings[0].equals("\\")) {
						newStateList.add(s);
						ps.put(initial, newStateList);
					} else if (nonTerminals.contains(strings[0])) {
						List<String> oldList = ps.getOrDefault(p.getKey(), new ArrayList<>());
						oldList.add(s);
						ps.put(p.getKey(), oldList);
					} else {
						newStateList.add(s.concat(p.getKey()));
						ps.put(initial, newStateList);
					}
				} else {
					List<String> oldList = ps.getOrDefault(p.getKey(), new ArrayList<>());
					oldList.add(s);
					ps.put(p.getKey(), oldList);
				}
			}
		}
		if (isGRD)
			return this;

		return new GR(new ArrayList<>(ps.keySet()), terminals, ps, initial);	
	}

	private String getNextState() {
		return "FIDEL";
	}
}
