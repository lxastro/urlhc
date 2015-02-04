package xlong.nlp.parser;

public class UnionParser extends Parser {

	private Parser[] parsers;
	private String delimiter;
	
	public UnionParser(Parser father, String delimiter, Parser... parsers) {
		super(father);
		this.parsers = parsers;
		this.delimiter = delimiter;
	}
	
	public UnionParser(Parser father, Parser... parsers){
		this(father, " ", parsers);
	}

	@Override
	protected String myParse(String text) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < parsers.length; i++) {
			if (i > 0) {
				s.append(delimiter);
			}
			s.append(parsers[i].parse(text));
		}
		return s.toString();
	}

}
