package tla;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class AFD extends AF{
	
	public AFD(final SortedSet<String> alp, final SortedSet<String> st, final Set<String> fst, final Set<String>[][] table, final String init) {
		this.setAlphabet(alp)
			.setStates(st)
			.setFinalStates(fst)
			.setDeltas(table)
			.setInitialState(init);
	}
	
	public GR toGR() {
		Set<String> nonTerminals = new HashSet<>();
		alphabet.forEach(c -> nonTerminals.add(String.valueOf(c)));
		Set<String> terminals = new HashSet<>();
		terminals.addAll(states);
		String initialState = this.initialState;
		Map<String, Set<String>> predicates = new HashMap<String, Set<String>>();
		
		for(String st : states) {
			for(String c: alphabet) {
				String r = getDelta(st, c);
				if(r!=null) {
					Set<String> to = new HashSet<>();
					to.add(String.valueOf(c).concat(st));
					predicates.put(st,to);
					nonTerminals.add(st);
					nonTerminals.add(r);
					if(finalStates.contains(r)) {
						Set<String> aux = new HashSet<>();
						aux.add(String.valueOf(c));
						predicates.put(st,aux);
					}
				}
			}
		}
		if(finalStates.contains(initialState)) {
			Set<String> aux = new HashSet<>();
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
		
		if(j==-1 || i==-1 || !this.states.contains(delta))
			return Boolean.FALSE;
		
		Set<String> set = new HashSet<>();
		set.add(delta);
		deltas[i][j] = set;
		return Boolean.TRUE;
	}
}
