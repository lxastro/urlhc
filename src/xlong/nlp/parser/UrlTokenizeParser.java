package xlong.nlp.parser;

import java.util.Iterator;
import java.util.List;

import xlong.nlp.tokenizer.Tokenizer;


public class UrlTokenizeParser extends Parser {

	private String delimiter;
	private Tokenizer tokenizer;
	private Parser wordParser;
	
	public UrlTokenizeParser(Parser father, Parser wordParser, String delimiter) {
		super(father);
		this.wordParser = wordParser;
		this.delimiter = delimiter;
	}
	
	public UrlTokenizeParser(Parser father, Parser wordParser) {
		this(father, wordParser, " ");
	}
	
	public UrlTokenizeParser(Parser father) {
		this(father, new NullParser(), " ");
	}
	

	@Override
	protected String myParse(String text) {
		List<String> words = tokenizer.tokenize(text);
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
