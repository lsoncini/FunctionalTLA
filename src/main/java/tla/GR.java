package tla;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class GR {

	protected Set<String> nonTerminals;
	protected Set<String> terminals;
	protected Map<String, Set<String>> predicates;
	protected String initialState;
	
	private static final String LAMBDA = "\\";

	public GR(final Set<String> nt, final Set<String> t, final Map<String, Set<String>> p, final String is) throws IllegalArgumentException{
		
		if(!Collections.disjoint(nt, t))
				throw new IllegalArgumentException("Non Terminals and Terminals must be disjoint");
		this.setNonTerminals(nt)
			.setTerminals(t)
			.setPredicates(p);
		if(!setInitialState(is))
			throw new IllegalArgumentException("Initial symbol not included in Non-Terminal set.");
		if(!isValid())
			throw new IllegalArgumentException("Not a regular grammar.");
		this.simplify();
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
	
	public boolean removePredicate(String from, String to) {
		Set<String> p = predicates.get(from);
		if(!p.contains(to))
			return Boolean.FALSE;
		p.remove(to);
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

	public boolean setInitialState(String initialState) {
		if (!nonTerminals.contains(initialState))
			return Boolean.FALSE;
		this.initialState = initialState;
		return Boolean.TRUE;
	}

	public GR toRight() {
		boolean isGRD = true;
		Map<String, Set<String>> ps = new HashMap<String, Set<String>>();
		Set<String> lambda = new HashSet<>();
		lambda.add(LAMBDA);
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
					
					if (strings[0].equals(LAMBDA)) {
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
		for(Entry<String, Set<String>> ps : predicates.entrySet()) {
			if(!nonTerminals.contains(ps.getKey()))
				return false;
			for(String p : ps.getValue()) {
				if (p.equals(LAMBDA))
					continue;
				String[] symbols = p.split(" ");
				boolean hasNT = false;
				boolean hasT = false;
				for (int i = 0; i < symbols.length; i++) {
					if(nonTerminals.contains(symbols[i])) {
						if(hasNT)
							return false;
						hasNT = true;
					} else if(terminals.contains(symbols[i])) {
						if(hasT)
							return false;
						hasT = true;
					}
				}
				if(!hasNT && !hasT)
					return false;
			}
		}
		for (String state : nonTerminals) {
			if(!predicates.containsKey(state) || predicates.get(state).isEmpty())
				return false;
		}
		return true;
	}
	
	private GR squash() {
		for(String nt : nonTerminals) {
			boolean hasChanged = false;
			do {
				hasChanged = false;
				Set<String> ps = predicates.get(nt);
				for(String p : ps) {
					String[] symbols = p.split(" ");
					if(symbols.length == 1 && nonTerminals.contains(p)){
						this.removePredicate(nt, p);
						this.addPredicate(nt, predicates.get(p));
						hasChanged = true;
					}
				}
			} while (hasChanged);
			
		}
		return this.simplify();
	}
	
	private String getNextState() {
		return "FIDEL";
	}
	
	private GR simplify() {
		Set<String> productiveSymbols = new HashSet<>();
		Set<String> reachableSymbols = new HashSet<>();
		reachableSymbols.add(initialState);
		for(Entry<String, Set<String>> ps : predicates.entrySet()) {
			for(String p : ps.getValue()) {
				if(terminals.contains(p) || p.equals(LAMBDA)) {
					productiveSymbols.add(ps.getKey());
					break;
				}
			}
		}
		boolean hasChanged = false;
		do {
			hasChanged = false;
			for(Entry<String, Set<String>> ps : predicates.entrySet()) {
				String key = ps.getKey();
				if(productiveSymbols.contains(key) && !reachableSymbols.contains(key))
					continue;
				for(String p : ps.getValue()) {
					String[] s = p.split(" ");
					String nt = null;
					for (int i = 0; i < s.length; i++) {
						if(nonTerminals.contains(s[i])) {
							nt = s[i];
							break;
						}
					}
					
					if(nt != null){
						if(!productiveSymbols.contains(key) && productiveSymbols.contains(nt)) {
							productiveSymbols.add(key);
							hasChanged = true;
							if(!reachableSymbols.contains(key))
								break;
						}
						if(reachableSymbols.contains(key) && !reachableSymbols.contains(nt)) {
							reachableSymbols.add(nt);
							hasChanged = true;
						}
					}
				}
			}
		} while (hasChanged);
		
		Set<String> killSet = nonTerminals.stream().filter(nt -> !productiveSymbols.contains(nt) || !reachableSymbols.contains(nt)).collect(Collectors.toSet());
		killSet.forEach(nt -> remove(nt));
		return this;
	}
	
	private void remove(String nt){
		//TODO remove everything
	}
}
