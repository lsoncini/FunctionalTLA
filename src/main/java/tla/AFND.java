package tla;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class AFND extends AF {

	public AFND(final SortedSet<Character> alp, final SortedSet<String> st, final Set<String> fst, final Set<String>[][] table, final String init) {
		this.setAlphabet(alp)
			.setStates(st)
			.setFinalStates(fst)
			.setDeltas(table)
			.setInitialState(init);
	}
	
	public Set<String> getDelta(String state, Character character){
		int i = states.contains(state) ? states.headSet(state).size() : -1;
		int j = alphabet.contains(character) ? alphabet.headSet(character).size() : -1;
		
		if(j==-1 || i==-1)
			return new HashSet<String>();
		
		return deltas[i][j];
	}
	
	public boolean setDelta(String state, Character character, Set<String> positions){
		int i = states.contains(state) ? states.headSet(state).size() : -1;
		int j = alphabet.contains(character) ? alphabet.headSet(character).size() : -1;
		
		if(j==-1 || i==-1 || !this.states.containsAll(positions))
			return Boolean.FALSE;
		
		deltas[i][j] = positions;
		return Boolean.TRUE;
	}
}
