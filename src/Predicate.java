import java.util.List;

public class Predicate {
	
	protected String from;
	protected List<String> to;
	
	public Predicate(final String f, final List<String> to) {
		this.from = f;
		this.to=to;
	}

	/*
	 * 
	 * Getters & Setters.
	 * 
	 * */
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<String> getTo() {
		return to;
	}

	public void setTo(List<String> to) {
		this.to = to;
	}
}
