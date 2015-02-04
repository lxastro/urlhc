package xlong.nlp.parser;

import weka.core.stemmers.Stemmer;

public class PTStemParser extends StemParser {

	public static Stemmer stemmer =  new weka.core.stemmers.PTStemmer();
	
	public PTStemParser(Parser father) {
		super(father);
	}
	
	@Override
	protected Stemmer getStemmer() {
		return stemmer;
	}

}
