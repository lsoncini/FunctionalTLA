import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AFD extends AF{
	
	public AFD(final List<Character> alp, final List<String> st, final List<String> fst, final List<String>[][] table ) {
		this.alphabet = alp;
		this.states = st;
		this.finalStates = fst;
		this.delta = table;
	}
	
	public GR toGR() {
		List<String> nonTerminals = new ArrayList<>();
		for (Character c : alphabet) {
			nonTerminals.add(String.valueOf(c));
		}
		List<String> terminals = this.states;
		String initialState = states.get(0);
		Map<String, List<String>> predicates = new HashMap<String, List<String>>();
		
		for(String st : states) {
			for(Character c: alphabet) {
				String r = getDelta(st, c);
				if(r!=null) {
					List<String> to = new ArrayList<>();
					to.add(String.valueOf(c).concat(st));
					predicates.put(st,to);
					nonTerminals.add(st);
					nonTerminals.add(r);
					if(finalStates.contains(r)) {
						List<String> aux = new ArrayList<>();
						aux.add(String.valueOf(c));
						predicates.put(st,aux);
					}
				}
			}
		}
		if(finalStates.contains(initialState)) {
			List<String> aux = new ArrayList<>();
			aux.add(String.valueOf("\\"));
			predicates.put(initialState,aux);
		}		
		return new GR(nonTerminals, terminals, predicates, initialState);
	}

	public String getDelta(String state, Character character) {
		List<String> aux = getPosition(state, character);
		return aux.isEmpty()? null:aux.get(0);
	}
}
