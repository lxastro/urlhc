package xlong.nlp.parser;

import xlong.nlp.wordsegmentation.WordSegmenter;
import xlong.nlp.wordsegmentation.WordSegmenters;

public class BigramSegmentParser extends SegmentParser {

	public static WordSegmenter wordSegmenter = WordSegmenters.getWeightBigramSegmenter(1.0);
	
	public static void setWeigth(double weight) {
		wordSegmenter = WordSegmenters.getWeightBigramSegmenter(1.0);
	}
	
	public BigramSegmentParser(Parser father, Parser wordParser, String delimiter) {
		super(father, wordParser, delimiter);
	}
	
	public BigramSegmentParser(Parser father, Parser wordParser) {
		super(father, wordParser);
	}
	
	public BigramSegmentParser(Parser father) {
		super(father);
	}

	@Override
	protected WordSegmenter getSegmenter() {
		return wordSegmenter;
	}

}
