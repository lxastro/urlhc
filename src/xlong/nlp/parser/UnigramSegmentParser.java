package xlong.nlp.parser;

import xlong.nlp.wordsegmentation.WordSegmenter;
import xlong.nlp.wordsegmentation.WordSegmenters;

public class UnigramSegmentParser extends SegmentParser {

	public static WordSegmenter wordSegmenter = WordSegmenters.getPeterNorvigUnigramSegmenter();
	
	public UnigramSegmentParser(Parser father, Parser wordParser, String delimiter) {
		super(father, wordParser, delimiter);
	}

	public UnigramSegmentParser(Parser father, Parser wordParser) {
		super(father, wordParser);
	}
	
	public UnigramSegmentParser(Parser father) {
		super(father);
	}

	@Override
	protected WordSegmenter getSegmenter() {
		return wordSegmenter;
	}

}
