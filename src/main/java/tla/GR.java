package tla;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class GR {

	protected Set<String> nonTerminals;
	protected Set<String> terminals;
	protected Map<String, Set<String>> predicates;
	protected String initialState;

	public GR(final Set<String> nt, final Set<String> t, final Map<String, Set<String>> p, final String is) throws IllegalArgumentException{
		if(!isValid())
			throw new IllegalArgumentException();
		
		this.setNonTerminals(nt)
			.setTerminals(t)
			.setPredicates(p)
			.setInitialState(is);
	}

	/*
	 * 
	 * Getters & Setters.
	 * 
	 */

	public Set<String> getNonTerminals() {
		return nonTerminals;
	}

	public GR setNonTerminals(Set<String> nonTerminals) {
		this.nonTerminals = nonTerminals;
		return this;
	}

	public Set<String> getTerminals() {
		return terminals;
	}

	public GR setTerminals(Set<String> terminals) {
		this.terminals = terminals;
		return this;
	}

	public Map<String, Set<String>> getPredicates() {
		return predicates;
	}

	public GR setPredicates(Map<String, Set<String>> predicates) {
		this.predicates = new HashMap<>();
		addPredicates(predicates);
		return this;
	}
	
	public boolean addPredicate(String from, Set<String> to) {
		Set<String> p = predicates.get(from);
		for(String s : to) {
			if(from.equals(s) || p.contains(s))
				return Boolean.FALSE;
		}
		
		p.addAll(to);
		this.predicates.put(from, p);
		return Boolean.TRUE;
	}
	public boolean addPredicates(Map<String,Set<String>> predicates) {
		boolean flag = true;
		for(Entry<String, Set<String>> p : predicates.entrySet()) {
			if(!addPredicate(p.getKey(), p.getValue()))
				flag = false;
		}
		return flag;
	}

	public String getInitialState() {
		return initialState;
	}

	public GR setInitialState(String initialState) {
		this.initialState = initialState;
		return this;
	}

	public GR toRight() {
		boolean isGRD = true;
		Map<String, Set<String>> ps = new HashMap<String, Set<String>>();
		Set<String> lambda = new HashSet<>();
		lambda.add("\\");
		String initial = getNextState();
		ps.put(initialState, lambda);
		for (Entry<String, Set<String>> p : predicates.entrySet()) {
			for (String s : p.getValue()) {
				String[] strings = s.split(" ");
				if (nonTerminals.contains(strings[0]) && strings.length > 1) {
					Set<String> oldList = ps.getOrDefault(strings[0], new HashSet<>());
					oldList.add(s.substring(strings[0].length()).concat(p.getKey()));
					ps.put(strings[0], oldList);
					isGRD = false;
				} else if (strings.length == 1) {
					Set<String> newStateList = ps.getOrDefault(initial, new HashSet<>());
					
					if (strings[0].equals("\\")) {
						newStateList.add(s);
						ps.put(initial, newStateList);
					} else if (nonTerminals.contains(strings[0])) {
						Set<String> oldList = ps.getOrDefault(p.getKey(), new HashSet<>());
						oldList.add(s);
						ps.put(p.getKey(), oldList);
					} else {
						newStateList.add(s.concat(p.getKey()));
						ps.put(initial, newStateList);
					}
				} else {
					Set<String> oldList = ps.getOrDefault(p.getKey(), new HashSet<>());
					oldList.add(s);
					ps.put(p.getKey(), oldList);
				}
			}
		}
		if (isGRD)
			return this;

		GR newGrammar = new GR(new HashSet<>(ps.keySet()), terminals, ps, initial);
		
		return newGrammar.squash();
	}

	private boolean isValid() {
		//TODO check regular
		return true; //TODO do something
	}
	
	private GR squash() {
		return this; //TODO do something
	}
	
	private String getNextState() {
		return "FIDEL";
	}
}
