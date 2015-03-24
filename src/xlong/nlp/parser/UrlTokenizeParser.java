package xlong.nlp.parser;

import java.util.Iterator;
import java.util.List;

import xlong.nlp.tokenizer.DelimiterTokenizer;
import xlong.nlp.tokenizer.Tokenizer;


public class UrlTokenizeParser extends Parser {

	private String delimiter;
	private Tokenizer protocolTokenizer;
	private Tokenizer slashTokenizer;
	private Parser wordParser;
	
	public UrlTokenizeParser(Parser father, Parser wordParser, String delimiter) {
		super(father);
		this.wordParser = wordParser;
		this.delimiter = delimiter;
		this.protocolTokenizer = new DelimiterTokenizer("://");
		this.slashTokenizer = new DelimiterTokenizer("[/?&]");
	}
	
	public UrlTokenizeParser(Parser father, Parser wordParser) {
		this(father, wordParser, " ");
	}
	
	public UrlTokenizeParser(Parser father) {
		this(father, new NullParser(), " ");
	}
	
	private String replaceSpace(String word) {
		return word.replace(' ', '_');
	}

	@Override
	protected String myParse(String text) {
		List<String> parts;
		parts = protocolTokenizer.tokenize(text);
		if (parts.size() == 2) {
			text = parts.get(1);
		} else {
			text = parts.get(0);
		}
		
		List<String> words = slashTokenizer.tokenize(text);
		if (words.size() == 0) {
			return "";
		}
		Iterator<String> iterator = words.iterator();
		StringBuilder s = new StringBuilder();
		while (true) {
			s.append(wordParser.parse(replaceSpace(iterator.next())));
			if (iterator.hasNext()) {
				s.append(delimiter);
			} else {
				break;
			}
		}
		return s.toString();
	}
	
	public static void main(String[] args) {
		UrlTokenizeParser parser = new UrlTokenizeParser(null);
		String url = "http://cn.bing.com/search?q=九九乘法表入英后变脸&qs=APN&sk=HS1&pq=中印对边界问题动真格&sc=8-10&sp=2&cvid=4496935bb65e44fe8943b1a029c24ec9&FORM=QBRE";
		System.out.println(parser.parse(url));
	}
}
