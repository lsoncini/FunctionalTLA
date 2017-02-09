package tla;
import java.util.ArrayList;
import java.util.List;

public class ArdenSolver {

	class Ecuation{
		String leftTerm;
		List<EcuationTerm> rightTerms;
		EcuationTerm ardenTerm;
		
		class EcuationTerm{
			public List<String> states;
			public List<Character> terminals;
			
			public EcuationTerm(){
				states = new ArrayList<String>();
				terminals = new ArrayList<Character>();
			}
			
			public EcuationTerm addState(String state){
				states.add(state);
				return this;
			}
			
			public EcuationTerm addTerminal(Character terminal){
				terminals.add(terminal);
				return this;
			}
		}
		
		public Ecuation(String left){
			leftTerm = left;
			rightTerms = new ArrayList<>();
			ardenTerm = new EcuationTerm();
		}
		
		public Ecuation addTerm(EcuationTerm term){
			if(term.states.contains(leftTerm)){
				for(String state : term.states){
					if(!state.equals(leftTerm))
						ardenTerm.addState(state);
				}
				//faltan cosas
			}
			return null;
		}
	}
}
