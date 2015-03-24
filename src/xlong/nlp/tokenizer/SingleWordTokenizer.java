package xlong.nlp.tokenizer;

public class SingleWordTokenizer extends DelimiterTokenizer {

	private static final long serialVersionUID = -740116059502329171L;
	private static final String DELIMITERSREG = "[0-9_\\W]";

	public SingleWordTokenizer() {
		super(DELIMITERSREG);
	}

	public SingleWordTokenizer(Tokenizer father) {
		super(father, DELIMITERSREG);
	}

	public static void main(String[] args) {
		for (String word : new SingleWordTokenizer().tokenize(new String(
				"http://www.nfl.com/teams/greenbaypackers/profile?team=GB"))) {
			System.out.println(word);
		}
		
		System.out.println("! next !");
		
		for (String word : new SingleWordTokenizer().tokenize(new String(
				"1_a 1_b 1_c 2_a 2_b 2_c"))) {
			System.out.println(word);
		}
	}

}
