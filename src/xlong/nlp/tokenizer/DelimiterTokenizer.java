package xlong.nlp.tokenizer;

import java.util.LinkedList;
import java.util.List;

public class DelimiterTokenizer extends Tokenizer {

	private static final long serialVersionUID = -740116059502329171L;
	private final String delimitersReg;

	public DelimiterTokenizer(String delimitersReg) {
		super(null);
		this.delimitersReg = delimitersReg;
	}

	public DelimiterTokenizer(Tokenizer father, String delimitersReg) {
		super(father);
		this.delimitersReg = delimitersReg;
	}

	private List<String> filterOutEmptyStrings(String[] splitString) {
		LinkedList<String> clean = new LinkedList<String>();

		for (int i = 0; i < splitString.length; i++) {
			if (!splitString[i].equals("")) {
				clean.add(splitString[i]);
			}
		}
		return clean;
	}

	@Override
	public List<String> myTokenize(String text) {
		return filterOutEmptyStrings(text.split(delimitersReg));
	}

	public static void main(String[] args) {
		for (String word : new DelimiterTokenizer("://").tokenize(new String(
				"http://www.nfl.com/teams/greenbaypackers/profile?team=GB"))) {
			System.out.println(word);
		}
		
		System.out.println("! next !");
		
		for (String word : new DelimiterTokenizer(" ").tokenize(new String(
				"1_a   1_b 1_c 2_a 2_b 2_c"))) {
			System.out.println(word);
		}
	}

}
