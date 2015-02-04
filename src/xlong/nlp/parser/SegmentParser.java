package xlong.nlp.parser;

import java.util.Iterator;
import java.util.Vector;

import xlong.nlp.wordsegmentation.WordSegmenter;

public abstract class SegmentParser extends Parser {

	private String delimiter;
	private Parser wordParser;
	
	public SegmentParser(Parser father, Parser wordParser, String delimiter) {
		super(father);
		this.wordParser = wordParser;
		this.delimiter = delimiter;
	}
	
	public SegmentParser(Parser father, Parser wordParser) {
		this(father, wordParser, " ");
	}
	
	public SegmentParser(Parser father) {
		this(father, new NullParser(), " ");
	}
	
	abstract protected WordSegmenter getSegmenter();

	@Override
	protected String myParse(String text) {
		Vector<String> words = getSegmenter().segment(text);
		if (words.size() == 0) {
			return "";
		}
		Iterator<String> iterator = words.iterator();
		StringBuilder s = new StringBuilder();
		while (true) {
			s.append(wordParser.parse(iterator.next()));
			if (iterator.hasNext()) {
				s.append(delimiter);
			} else {
				break;
			}
		}
		return s.toString();
	}

	
}
