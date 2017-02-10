package tla;

import java.util.HashSet;
import java.util.Set;

public class Test {

	public static void main(String[] args) {
		Set<String>[][] delta = new HashSet[2][5+1];
		for (int i = 0; i < delta.length; i++) {
			for (int j = 0; j < delta[i].length; j++) {
				System.out.println(delta[i][j]);
			}
		}
	}

}
