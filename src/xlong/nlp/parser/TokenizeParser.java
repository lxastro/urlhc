package xlong.nlp.parser;

import java.util.Iterator;
import java.util.List;

import xlong.nlp.tokenizer.Tokenizer;


public class TokenizeParser extends Parser {

	private String delimiter;
	private Tokenizer tokenizer;
	private Parser wordParser;
	
	public TokenizeParser(Parser father, Tokenizer tokenizer, Parser wordParser, String delimiter) {
		super(father);
		this.tokenizer = tokenizer;
		this.wordParser = wordParser;
		this.delimiter = delimiter;
	}
	
	public TokenizeParser(Parser father, Tokenizer tokenizer, Parser wordParser) {
		this(father, tokenizer, wordParser, " ");
	}
	
	public TokenizeParser(Parser father, Tokenizer tokenizer) {
		this(father, tokenizer, new NullParser(), " ");
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
