package tla;
import java.util.ArrayList;
import java.util.List;

public abstract class AF {
	protected List<Character> alphabet;
	protected List<String> states;
	protected List<String> finalStates;
	protected List<String>[][] delta;
	
	/*
	 * 
	 * Getters & Setters. 
	 * 
	 * */
	
	public List<Character> getAlphabet() {
		return alphabet;
	}
	public void setAlphabet(List<Character> alphabet) {
		this.alphabet = alphabet;
	}
	public List<String> getStates() {
		return states;
	}
	public void setStates(List<String> states) {
		this.states = states;
	}
	public List<String> getFinalStates() {
		return finalStates;
	}
	public void setFinalStates(List<String> finalStates) {
		this.finalStates = finalStates;
	}
	public List<String>[][] getDelta() {
		return delta;
	}
	public void setDelta(List<String>[][] delta) {
		this.delta = delta;
	}
	
	public List<String> getPosition(String state, Character character){
		int i = -1,j = -1;
		for (int k = 0; k < states.size(); k++) {
			if(states.get(k).equals(state)){
				i = k;
				break;
			}
		}
		for (int k = 0; k < alphabet.size(); k++) {
			if(alphabet.get(k).equals(character)){
				j = k;
				break;
			}
		}
		if(j==-1 || i==-1)
			return new ArrayList<String>();
		return delta[i][j];
	}
	
	public boolean setPosition(String state, Character character, List<String> positions){
		int i = -1,j = -1;
		for (int k = 0; k < states.size(); k++) {
			if(states.get(k).equals(state)){
				i = k;
				break;
			}
		}
		for (int k = 0; k < alphabet.size(); k++) {
			if(alphabet.get(k).equals(character)){
				j = k;
				break;
			}
		}
		
		if(j==-1 || i==-1 || !this.states.containsAll(positions))
			return Boolean.FALSE;
		
		delta[i][j] = positions;
		return Boolean.TRUE;
	}
}
