package xlong.nlp.parser;

public class AddPrefixParser extends Parser {

	private String prefix;
	
	public AddPrefixParser(Parser father, String prefix) {
		super(father);
		this.prefix = prefix;
	}

	@Override
	protected String myParse(String text) {
		return prefix + text;
	}

}
