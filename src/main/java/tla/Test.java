package tla;

import java.util.SortedSet;
import java.util.TreeSet;

public class Test {

	public static void main(String[] args) {
		SortedSet<String> set = new TreeSet<>();
		set.add("chau");
		set.add("iubdiub");
		set.add("cyebyehau");
		set.add("hola");
		System.out.println(set.headSet("hola").size());
	}

}
