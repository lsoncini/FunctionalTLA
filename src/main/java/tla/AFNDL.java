package tla;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class AFNDL extends AF{

	private static final String LAMBDA = "\\";
	
	public AFNDL(SortedSet<String> alp, SortedSet<String> st, Set<String> fst, Set<String>[][] table, String init) {
		if(table.length != st.size() || table[0].length != alp.size())
			throw new IllegalArgumentException("Delta dimensions are wrong (#Rows != #States or #Columns != #Alphabet + 1");
		if (!st.containsAll(fst) || !st.contains(init))
			throw new IllegalArgumentException("All final and initial states must be included in States");
		
		this.setAlphabet(alp)
			.setStates(st)
			.setFinalStates(fst)
			.setDeltas(table)
			.setInitialState(init);
	}
	public AFNDL(final SortedSet<String> alp, final SortedSet<String> st, final Set<String> fst, final String init) {
		@SuppressWarnings("unchecked")
		Set<String>[][] delta = new HashSet[st.size()][alp.size()+1];
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
	
	public Set<String> getDelta(String state, String character){
		int j;
		int i = states.contains(state) ? states.headSet(state).size() : -1;
		
		if(character.equals(LAMBDA))
			j = deltas[0].length - 1;
		else 
			j = alphabet.contains(character) ? alphabet.headSet(character).size() : -1;
		
		if(j==-1 || i==-1)
			return new HashSet<>();
		
		return deltas[i][j] == null ? new HashSet<>() : deltas[i][j];
	}
	
	public boolean setDelta(String state, String character, Set<String> positions){
		int j;
		int i = states.contains(state) ? states.headSet(state).size() : -1;
		if (character.equals(LAMBDA))
			j = deltas[0].length - 1;
		else 
			j = alphabet.contains(character) ? alphabet.headSet(character).size() : -1;
		
		if (positions != null && !positions.isEmpty()) {
			if (j==-1 || i==-1 || !this.states.containsAll(positions))
				return Boolean.FALSE;
		}
		
		deltas[i][j] = positions == null ? new HashSet<>() : positions;
		return Boolean.TRUE;
	}
	
	public Set<String> lambdaClosure(String state) {
		if (!states.contains(state))
			return null;
		
		Set<String> closure = new HashSet<>();
		Set<String> newElements = new HashSet<>();
		closure.add(state);
		do {
			newElements = new HashSet<>(closure);
			for (String s : newElements)
				closure.addAll(getDelta(s, LAMBDA));
		} while (newElements.size() != closure.size());
		
		return closure;
	}
	
	@Override
	public AFND toAFND() {
		Set<String> fStates = new HashSet<>();
		for (String st : getStates()) {
			for (String fst : getFinalStates()) {
				if (lambdaClosure(st).contains(fst)) {
					fStates.add(st);
					break;
				}
			}	
		}
		AFND afnd = new AFND(getAlphabet(), getStates(), fStates, getInitialState());
		
		for(String st : getStates()) {
			for (String c : getAlphabet()) {
				Set<String> aux = new HashSet<String>();
				for (String aSt : lambdaClosure(st)) {
					aux.addAll(getDelta(aSt, c));
				}
				Set<String> deltas = new HashSet<>();
				for (String aSt : aux) 
					deltas.addAll(lambdaClosure(aSt));
				afnd.setDelta(st, c, deltas);
			}
		}
		return afnd;
	}
	
	@Override
	public boolean hasLambdas() {
		return true;
	}

	public AFD toAFD() {
		return toAFND().toAFD();
	}
	
	@Override
	public AFNDL toAFNDL() {
		return this;
	}
}