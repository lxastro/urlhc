package xlong.nlp.parser;

import weka.core.stemmers.Stemmer;

public class SnowballStemParser extends StemParser {

	public static Stemmer stemmer =  new weka.core.stemmers.SnowballStemmer();
	
	public SnowballStemParser(Parser father) {
		super(father);
	}

	@Override
	protected Stemmer getStemmer() {
		return stemmer;
	}

}
