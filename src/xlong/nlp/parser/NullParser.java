package xlong.nlp.parser;

public class NullParser extends Parser {

	public NullParser() {
		super(null);
	}

	@Override
	protected String myParse(String text) {
		return text;
	}

}
