package tla;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class AFND extends AF {

	public AFND(final SortedSet<String> alp, final SortedSet<String> st, final Set<String> fst, final Set<String>[][] table, final String init) {
		if(table.length != st.size() || table[0].length != alp.size())
			throw new IllegalArgumentException("Delta dimensions are wrong (#Rows != #States or #Columns != #Alphabet");
		this.setAlphabet(alp)
			.setStates(st)
			.setFinalStates(fst)
			.setDeltas(table)
			.setInitialState(init);
	}
	public AFND(final SortedSet<String> alp, final SortedSet<String> st, final Set<String> fst, final String init) {
		@SuppressWarnings("unchecked")
		Set<String>[][] delta = new HashSet[st.size()][alp.size()];
		for (int i = 0; i < delta.length; i++) {
			for (int j = 0; j < delta[i].length; j++) {
				delta[i][j] = Collections.emptySet();
			}
		}
		this.setAlphabet(alp)
		.setStates(st)
		.setFinalStates(fst)
		.setDeltas(delta)
		.setInitialState(init);
 	}
	
	public Set<String> getDelta(String state, String character){
		int i = states.contains(state) ? states.headSet(state).size() : -1;
		int j = alphabet.contains(character) ? alphabet.headSet(character).size() : -1;
		
		if(j==-1 || i==-1)
			return new HashSet<>();
		
		return deltas[i][j] == null ? new HashSet<>() : deltas[i][j];
	}
	
	public boolean setDelta(String state, String character, Set<String> positions){
		int i = states.contains(state) ? states.headSet(state).size() : -1;
		int j = alphabet.contains(character) ? alphabet.headSet(character).size() : -1;
		
		if(j==-1 || i==-1 || !this.states.containsAll(positions))
			return Boolean.FALSE;
		
		deltas[i][j] = positions;
		return Boolean.TRUE;
	}
}
