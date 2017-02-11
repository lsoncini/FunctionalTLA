package tla;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class AFD extends AF{
	
	public AFD(final SortedSet<String> alp, final SortedSet<String> st, final Set<String> fst, final Set<String>[][] table, final String init) {
		if (table.length != st.size() || table[0].length != alp.size())
			throw new IllegalArgumentException("Delta dimensions are wrong (#Rows != #States or #Columns != #Alphabet");
		if (!st.containsAll(fst) || !st.contains(init))
			throw new IllegalArgumentException("All final and initial states must be included in States");
		
		this.setAlphabet(alp)
			.setStates(st)
			.setFinalStates(fst)
			.setDeltas(table)
			.setInitialState(init);
	}
	public AFD(final SortedSet<String> alp, final SortedSet<String> st, final Set<String> fst, final String init) {
		@SuppressWarnings("unchecked")
		Set<String>[][] delta = new HashSet[st.size()][alp.size()];
		for (int i = 0; i < delta.length; i++) {
			for (int j = 0; j < delta[i].length; j++) {
				delta[i][j] = new HashSet<>();
			}
		}
		if (!st.containsAll(fst) || !st.contains(init))
			throw new IllegalArgumentException("All final and initial states must be included in States");
		
		this.setAlphabet(alp)
		.setStates(st)
		.setFinalStates(fst)
		.setDeltas(delta)
		.setInitialState(init);
	}
	
	public GR toGR() {
		Set<String> terminals = new HashSet<>();
		alphabet.forEach(c -> terminals.add(c));
		Set<String> nonTerminals = new HashSet<>();
		nonTerminals.addAll(states);
		String initialState = this.initialState;
		Map<String, Set<String>> predicates = new HashMap<String, Set<String>>();
		
		for(String st : states) {
			for(String c: alphabet) {
				String r = getDelta(st, c);
				if(r!=null) {
					Set<String> to = predicates.getOrDefault(st, new HashSet<>());
					to.add(c.concat(" ").concat(r));
					predicates.put(st,to);
					nonTerminals.add(st);
					nonTerminals.add(r);
					if(finalStates.contains(r)) {
						Set<String> aux = predicates.getOrDefault(st, new HashSet<>());
						aux.add(c);
						predicates.put(st,aux);
					}
				}
			}
		}
		if(finalStates.contains(initialState)) {
			Set<String> aux = predicates.getOrDefault(initialState, new HashSet<>());
			aux.add(String.valueOf("\\"));
			predicates.put(initialState,aux);
		}		
		return new GR(nonTerminals, terminals, predicates, initialState);
	}

	public String getDelta(String state, String character) {
		int i = states.contains(state) ? states.headSet(state).size() : -1;
		int j = alphabet.contains(character) ? alphabet.headSet(character).size() : -1;
		
		if(j==-1 || i==-1)
			return null;
		return deltas[i][j].stream().findFirst().orElse(null);
	}
	
	public boolean setDelta(String state, String character, String delta){
		int i = states.contains(state) ? states.headSet(state).size() : -1;
		int j = alphabet.contains(character) ? alphabet.headSet(character).size() : -1;
		
		if(delta != null) {
			if(j==-1 || i==-1 || !states.contains(delta))
				return Boolean.FALSE;
		}
		Set<String> set = new HashSet<>();
		if(delta != null)
			set.add(delta);
		deltas[i][j] = set;
		return Boolean.TRUE;
	}

	@Override
	public AFD toAFD() {
		return this;
	}
	@Override
	public AFND toAFND() {
		return new AFND(getAlphabet(), getStates(), getFinalStates(), getDeltas(), getInitialState());
	}
	
	@Override
	public AFNDL toAFNDL() {
		AFNDL ans = new AFNDL(getAlphabet(), getStates(), getFinalStates(), getInitialState());
		for (String st : getStates()) {
			for (String c : getAlphabet()) {
				Set<String> thisSet = new HashSet<String>();
				String thisDelta = getDelta(st, c);
				if (thisDelta != null) {
					thisSet.add(thisDelta);
					ans.setDelta(st, c, thisSet);	
				}
			}
		}
		return ans;
	}
	
	public AFD addTrap() {
		String trapState = "TRAP_STATE";
		SortedSet<String> sts = new TreeSet<>(getStates());
		sts.add(trapState);
		AFD ans = new AFD(getAlphabet(), sts, getFinalStates(), getInitialState());
		for (String st : getStates()) {
			for (String c : getAlphabet()) {
				String delta = getDelta(st, c);
				if (delta == null) {
					ans.setDelta(st, c, trapState);
				} else {
					ans.setDelta(st, c, delta);
				}
			}
		}
		for (String c : getAlphabet()) {
			ans.setDelta(trapState, c, trapState);
		}
		return ans;
	}
	
	public AFD minimalAFD() {
		AFD original = this.addTrap();
		List<Set<String>> lastList = new ArrayList<Set<String>>();
		Map<String, Set<String>> lastMap = new HashMap<String,Set<String>>();
		lastList.add(new HashSet<>(original.getStates()));
		for (String st : original.getStates())
			lastMap.put(st, original.getStates());
		Set<String> fsts = new HashSet<>(original.getFinalStates());
		Set<String> nfsts = new HashSet<String>(original.getStates().stream().filter(s -> !original.getFinalStates().contains(s)).collect(Collectors.toSet()));
		List<Set<String>> currentList = new ArrayList<>(); 
		Map<String, Set<String>> currentMap = new HashMap<String,Set<String>>();
		for (String state : fsts)
			currentMap.put(state, fsts);
		for (String state : nfsts)
			currentMap.put(state, nfsts);
		
		currentList.add(fsts);
		currentList.add(nfsts);
		while (!currentList.equals(lastList)) {
			lastList = currentList;
			currentList = new ArrayList<Set<String>>();
			lastMap = currentMap;
			currentMap = new HashMap<>();
			for (String st : original.getStates()) {
				boolean wasIntroduced = false;
				for (int i = 0; i < currentList.size(); i++) {
					Set<String> current = currentList.get(i);
					if (lastMap.get(st).containsAll(current)) {
						boolean belongs = true;
						for (String c : original.getAlphabet()) {
							String aSt = current.stream().findFirst().orElse(null);
							if (!lastMap.get(original.getDelta(st, c)).equals(lastMap.get(original.getDelta(aSt, c)))) {
								belongs = false;
								break;
							}	
						}
						if (belongs) {
							wasIntroduced = true;
							current.add(st);
							currentMap.put(st, current);
							break;
						}
					}
				}
				if (!wasIntroduced) {
					Set<String> newSet = new HashSet<>();
					newSet.add(st);
					currentList.add(newSet);
					currentMap.put(st, newSet);
				}
			}
		}
		SortedSet<String> minimalSts = new TreeSet<>();
		for (Set<String> s : currentList) {
			if (s.contains(original.getInitialState()))
				minimalSts.add(original.getInitialState());
			else {
				minimalSts.add(s.stream().findFirst().orElse(null));
			}
		}
		SortedSet<String> minimalAlp = new TreeSet<>(original.getAlphabet());
		SortedSet<String> minimalFsts = new TreeSet<>(original.getFinalStates().stream().filter(st -> minimalSts.contains(st)).collect(Collectors.toSet()));
		AFD ans = new AFD(minimalAlp, minimalSts, minimalFsts, original.getInitialState());
		for (String st : minimalSts) {
			for (String c : minimalAlp) {
				String old = original.getDelta(st, c);
				for (String aSt : currentMap.get(old)) {
					if (minimalSts.contains(aSt)) {
						ans.setDelta(st, c, aSt);						
					}
				}
			}
		}
		return ans;
	}
}
