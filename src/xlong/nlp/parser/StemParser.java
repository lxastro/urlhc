package xlong.nlp.parser;

import weka.core.stemmers.Stemmer;

public abstract class StemParser extends Parser {

	public StemParser(Parser father) {
		super(father);
	}
	
	abstract protected Stemmer getStemmer();

	@Override
	public String myParse(String text) {
		return getStemmer().stem(text);
	}

}
