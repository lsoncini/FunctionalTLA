package tla;
import java.util.Set;
import java.util.SortedSet;

public abstract class AF {
	protected SortedSet<Character> alphabet;
	protected SortedSet<String> states;
	protected Set<String> finalStates;
	protected Set<String>[][] deltas;
	protected String initialState;
	
	/*
	 * 
	 * Getters & Setters. 
	 * 
	 * */
	
	public SortedSet<Character> getAlphabet() {
		return alphabet;
	}
	public AF setAlphabet(SortedSet<Character> alphabet) {
		this.alphabet = alphabet;
		return this;
	}
	public SortedSet<String> getStates() {
		return states;
	}
	public AF setStates(SortedSet<String> states) {
		this.states = states;
		return this;
	}
	public Set<String> getFinalStates() {
		return finalStates;
	}
	public AF setFinalStates(Set<String> finalStates) {
		for(String s : finalStates){
			if(!states.contains(s))
				return this;
		}
		this.finalStates = finalStates;
		return this;
	}
	public Set<String>[][] getDeltas() {
		return deltas;
	}
	public AF setDeltas(Set<String>[][] deltas) {
		this.deltas = deltas;
		return this;		
	}
	public String getInitialState() {
		return initialState;
	}
	public AF setInitialState(String initialState) {
		if(states.contains(initialState))
			this.initialState = initialState;
		return this;
	}
}
