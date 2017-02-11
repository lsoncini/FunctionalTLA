package tla;
import java.util.Set;
import java.util.SortedSet;

public abstract class AF {
	protected SortedSet<String> alphabet;
	protected SortedSet<String> states;
	protected Set<String> finalStates;
	protected Set<String>[][] deltas;
	protected String initialState;
	
	/*
	 * 
	 * Getters & Setters. 
	 * 
	 * */
	
	public SortedSet<String> getAlphabet() {
		return alphabet;
	}
	public AF setAlphabet(SortedSet<String> alphabet) {
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
		if(!states.containsAll(finalStates))
			return this;
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
	
	public abstract AFD toAFD();
	public abstract AFND toAFND();
	public abstract AFNDL toAFNDL();
	
	public GR toGR() {
		return this.toAFD().toGR();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append(" {\n");
		sb.append(String.format("\t States: %s,\n", getStates().toString()))
		  .append(String.format("\t Alphabet: %s,\n", getAlphabet().toString()))
		  .append(String.format("\t Finals: %s,\n", getFinalStates().toString()))
		  .append(String.format("\t Initial: %s,\n", getInitialState()))
		  .append(String.format("\t Delta: {\n", getInitialState()));
		int i = 0,j = 0;
		for (String st : getStates()) {
			j = 0;
			for (String c : getAlphabet()) {
				sb.append(String.format("\t\t d(%s, %s): %s\n", st,c,deltas[i][j]));
				j++;
			}
			if(hasLambdas())
				sb.append(String.format("\t\t d(%s, %s): %s\n", st, "\\", deltas[i][j]));
			i++;
		}
		sb.append("\t }\n").append("}\n");
		return sb.toString();
	}
	
	public boolean hasLambdas() {
		return false;
	}
}
