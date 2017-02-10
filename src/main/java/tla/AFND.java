package tla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
	@Override
	public AFD toAFD() {
		SortedSet<String> sts = new TreeSet<>();
		sts.add(initialState);
		Set<String> fsts = new HashSet<>();
		if(getFinalStates().contains(initialState))
			fsts.add(initialState);
		Map<String, Map<String, String>> tableMap = new HashMap<>();
		Set<String> doneSts = new HashSet<String>();
		List<Set<String>> pendingSts = new ArrayList<Set<String>>();
		Set<String> aux = Collections.emptySet();
		aux.add(initialState);
		pendingSts.add(aux);
		
		while (!pendingSts.isEmpty()) {
			Set<String> currentStSet = pendingSts.get(0);
			pendingSts.remove(0);
			String currentSt = getConcatName(currentStSet);
			doneSts.add(currentSt);
			
			for (String sym : getAlphabet()) {
				Set<String> currentDelta = getConcatDelta(currentStSet, sym);
				String currentDeltaName = getConcatName(currentDelta);
				Map<String, String> currentRow = tableMap.getOrDefault(currentSt, Collections.emptyMap());
				currentRow.put(sym, currentDeltaName);
				if (!pendingSts.contains(currentDelta) && !doneSts.contains(currentDeltaName)) {
					pendingSts.add(currentDelta);
					sts.add(currentDeltaName);
					if(isFinalState(currentDelta))
						fsts.add(currentDeltaName);
				}
			}
		}
		
		AFD newAFD = new AFD(getAlphabet(), sts, fsts, getInitialState());
		for (String st : sts) {
			Map<String, String> deltaForSt = tableMap.get(st);
			for (String c : getAlphabet()) {
				newAFD.setDelta(st, c, deltaForSt.get(c));
			}
		}
		
		//check empty empty empty
		return newAFD;
	}
	
	private String getConcatName(Set<String> states){
		SortedSet<String> sortedStates = new TreeSet<>(states);
		String name = sortedStates.stream().reduce("", String::concat);
		while (alphabet.contains(name)) {
			name += "'";
		}
		return name;
	}
	private Set<String> getConcatDelta(Set<String> states, String character){
		Set<String> set = new HashSet<>();
		for (String st : states)
			set.addAll(getDelta(st, character));
		return set;
	}
	private boolean isFinalState(Set<String> states){
		for (String st : states)
			if(finalStates.contains(st))
				return true;
		return false;
	}
}
