package tla;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class GR {

	protected Set<String> nonTerminals;
	protected Set<String> terminals;
	protected Map<String, Set<String>> predicates;
	protected String initialSymbol;
	
	private static final String LAMBDA = "\\";

	public GR(final Set<String> nt, final Set<String> t, final Map<String, Set<String>> p, final String is) throws IllegalArgumentException{
		
		if(!Collections.disjoint(nt, t))
				throw new IllegalArgumentException("Non Terminals and Terminals must be disjoint");
		this.setNonTerminals(nt)
			.setTerminals(t)
			.setPredicates(p);
		if(!setInitialState(is))
			throw new IllegalArgumentException("Initial symbol not included in Non-Terminal set.");
//		System.out.println("\n\nNOT VALIDATED GRAMMAR:\n\n");
//		System.out.println(this);
		if(!isValid())
			throw new IllegalArgumentException("Not a regular grammar.");
//		System.out.println("\n\nPSEUDO GRAMMAR:\n\n");
//		System.out.println(this);
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
	
	public void addPredicate(String from, Set<String> to) {
		Set<String> p = predicates.getOrDefault(from, new HashSet<>());
		for(String s : to) {
			if(from.equals(s) || p.contains(s))
				return;
		}
		
		p.addAll(to);
		this.predicates.put(from, p);
	}
	
	public void removePredicate(String from, String to) {
		Set<String> p = predicates.get(from);
		if(!p.contains(to))
			return;
		p.remove(to);
		this.predicates.put(from, p);
	}
	
	public void addPredicates(Map<String,Set<String>> predicates) {
		for(Entry<String, Set<String>> p : predicates.entrySet())
			addPredicate(p.getKey(), p.getValue());
	}

	public String getInitialState() {
		return initialSymbol;
	}

	public boolean setInitialState(String initialState) {
		if (!nonTerminals.contains(initialState))
			return Boolean.FALSE;
		this.initialSymbol = initialState;
		return Boolean.TRUE;
	}

	public GR toRight() {
		boolean isGRD = true;
		Map<String, Set<String>> ps = new HashMap<String, Set<String>>();
		Set<String> lambda = new HashSet<>();
		lambda.add(LAMBDA);
		String initial = getNextState();
		ps.put(initialSymbol, lambda);
		for (Entry<String, Set<String>> p : predicates.entrySet()) {
			for (String s : p.getValue()) {
				String[] strings = s.split(" ");
				if (nonTerminals.contains(strings[0]) && strings.length > 1) {
					Set<String> oldList = ps.getOrDefault(strings[0], new HashSet<>());
					oldList.add(s.replaceFirst(strings[0]+" ", "").concat(" ").concat(p.getKey()));
					ps.put(strings[0], oldList);
					isGRD = false;
				} else if (strings.length == 1) {
					Set<String> newStateList = ps.getOrDefault(initial, new HashSet<>());
					
					if (strings[0].equals(LAMBDA)) {
						newStateList.add(p.getKey());
						ps.put(initial, newStateList);
					} else if (nonTerminals.contains(strings[0])) {
						Set<String> oldList = ps.getOrDefault(p.getKey(), new HashSet<>());
						oldList.add(s);
						ps.put(p.getKey(), oldList);
					} else {
						newStateList.add(s.concat(" ").concat(p.getKey()));
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
					} else if (terminals.contains(symbols[i])) {
						if (hasT)
							return false;
						hasT = true;
					}
				}
				if (!hasNT && !hasT)
					return false;
			}
		}
		for (String state : nonTerminals) {
			if (!predicates.containsKey(state) || predicates.get(state).isEmpty())
				return false;
		}
		return true;
	}
	
	private GR squash() {
		for (String nt : nonTerminals) {
			boolean hasChanged = false;
			do {
				hasChanged = false;
				Set<String> ps = predicates.get(nt);
				Set<String> toRemoveForNT = new HashSet<>();
				Set<String> toAddForNT = new HashSet<>();
				for (String p : ps) {
					String[] symbols = p.split(" ");
					if (symbols.length == 1 && nonTerminals.contains(p)){
						toRemoveForNT.add(p);
						toAddForNT.addAll(predicates.get(p));
						hasChanged = true;
					}
				}
				toRemoveForNT.forEach(s -> removePredicate(nt, s));
				addPredicate(nt, toAddForNT);
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
		reachableSymbols.add(initialSymbol);
		for (Entry<String, Set<String>> ps : predicates.entrySet()) {
			for (String p : ps.getValue()) {
				if (terminals.contains(p) || p.equals(LAMBDA)) {
					productiveSymbols.add(ps.getKey());
					break;
				}
			}
		}
		boolean hasChanged = false;
		do {
			hasChanged = false;
			for (Entry<String, Set<String>> ps : predicates.entrySet()) {
				String key = ps.getKey();
				if (productiveSymbols.contains(key) && !reachableSymbols.contains(key))
					continue;
				for (String p : ps.getValue()) {
					String[] s = p.split(" ");
					String nt = null;
					for (int i = 0; i < s.length; i++) {
						if (nonTerminals.contains(s[i])) {
							nt = s[i];
							break;
						}
					}
					
					if (nt != null){
						if (!productiveSymbols.contains(key) && productiveSymbols.contains(nt)) {
							productiveSymbols.add(key);
							hasChanged = true;
							if (!reachableSymbols.contains(key))
								break;
						}
						if (reachableSymbols.contains(key) && !reachableSymbols.contains(nt)) {
							reachableSymbols.add(nt);
							hasChanged = true;
						}
					}
				}
			}
		} while (hasChanged);
		
		Set<String> killSet = nonTerminals.stream().filter(nt -> !productiveSymbols.contains(nt) || !reachableSymbols.contains(nt)).collect(Collectors.toSet());
		this.removeSymbols(killSet);
		return this;
	}
	
	private void removeSymbols(Set<String> nts){
		nts.forEach(nt -> predicates.remove(nt));
		for (Entry<String, Set<String>> ps : predicates.entrySet()) {
			for (String p : ps.getValue()) {
				String[] s = p.split(" ");
				for (int i = 0; i < s.length; i++) {
					if (nts.contains(s[i])) {
						this.removePredicate(ps.getKey(), p);
						break;
					}
				}
			}
		}
		nonTerminals.removeAll(nts);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append(" {\n");
		sb.append(String.format("\t Alphabet: %s,\n", getTerminals().toString()))
		  .append(String.format("\t Non-terminals: %s,\n", getNonTerminals().toString()))
		  .append(String.format("\t Initial: %s,\n", getInitialState()))
		  .append(String.format("\t Predicates: {\n", getInitialState()));
		
		for (Entry<String, Set<String>> ps : predicates.entrySet()) {
			String left = ps.getKey();
			String right = "";
			boolean flag = false;
			for (String p : ps.getValue()) {
				String fullPredicate = "";
				if(flag)
					fullPredicate += "|";
				fullPredicate += p.replace(" ", "");
				right += fullPredicate;
				flag = true;
			}
			sb.append(String.format("\t\t %s -> %s\n", left, right));
		}
		sb.append("\t }\n").append("}\n");
		return sb.toString();
	}
	
	public AFNDL toAFNDL() {
		GR rlg = this.toRight();
		SortedSet<String> alp = new TreeSet<>(rlg.getTerminals());
		SortedSet<String> sts = new TreeSet<>(rlg.getNonTerminals());
		Set<String> fsts = new HashSet<>();
		AFNDL ans = new AFNDL(alp, sts, fsts, rlg.getInitialState());
		
		for (String st : sts) {
			Set<String> preds = rlg.getPredicates().get(st);
			for (String ps : preds) {
				if(ps.equals(LAMBDA)) {
					fsts.add(st);
				} else {
					String[] p = ps.split(" ");
					String c = p[0];
					String s = p[1];
					Set<String> old = ans.getDelta(st, c);
					old.add(s);
					ans.setDelta(st, c, old);
				}
			}
		}
		return ans;
	}
	public AFND toAFND() {
		return toAFNDL().toAFND();
	}
	public AFD toAFD() {
		return toAFNDL().toAFD();
	}
}
