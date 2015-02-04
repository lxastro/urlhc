package xlong.nlp.tokenizer;

import java.util.LinkedList;
import java.util.List;

public class SingleWordTokenizer extends Tokenizer {

	private static final String DELIMITERSREG = "[0-9_\\W]";

	public SingleWordTokenizer() {
		super(null);
	}

	public SingleWordTokenizer(Tokenizer father) {
		super(father);
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
		return filterOutEmptyStrings(text.split(DELIMITERSREG));
	}

	public static void main(String[] args) {
		for (String word : new SingleWordTokenizer().tokenize(new String(
				"http://www.nfl.com/teams/greenbaypackers/profile?team=GB"))) {
			System.out.println(word);
		}
	}

}
