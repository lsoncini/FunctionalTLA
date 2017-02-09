package tla;
public class ER {
	
	private String regex;
	private ER next = null;
	private boolean repeatable;
	
	public ER(final String s, final boolean repeatable){
		regex = s;
		this.repeatable = repeatable;
	}

	public String getRegex() {
		return regex + getNext() != null ? next.getRegex() : "";
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public ER getNext() {
		return next;
	}

	public void setNext(ER next) {
		this.next = next;
	}

	public boolean isRepeatable() {
		return repeatable;
	}

	public void setRepeatable(boolean repeatable) {
		this.repeatable = repeatable;
	}
	
	

}
