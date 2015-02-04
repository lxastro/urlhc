package xlong.nlp.parser;

import xlong.nlp.tokenizer.SingleWordTokenizer;

class ParserTest {
	private static String[] testStrings = new String[] {
		"http://choosespain/thisisatest/sitdown/itwasabrightcolddayinaprilandtheclockswerestrikingthirteen",
	};
	public static void main(String[] args) {
		Parser urlParser;
		urlParser = new TokenizeParser(null, new SingleWordTokenizer(), new UnigramSegmentParser(null, new SnowballStemParser(null)), "/");
		for (String str:testStrings) {
			String after = urlParser.parse(str);
			System.out.println(after);
		}
		System.out.println();
		urlParser = new TokenizeParser(null, new SingleWordTokenizer(), new UnigramSegmentParser(null, new PTStemParser(null), ","), "/");
		for (String str:testStrings) {
			String after = urlParser.parse(str);
			System.out.println(after);
		}
		System.out.println();
		urlParser = new TokenizeParser(null, new SingleWordTokenizer(), new BigramSegmentParser(null, new SnowballStemParser(null)));
		for (String str:testStrings) {
			String after = urlParser.parse(str);
			System.out.println(after);
		}
		System.out.println();
		urlParser = new TokenizeParser(null, new SingleWordTokenizer(), new BigramSegmentParser(null, new PTStemParser(null)));
		for (String str:testStrings) {
			String after = urlParser.parse(str);
			System.out.println(after);
		}
		System.out.println();
		urlParser = new TokenizeParser(null, new SingleWordTokenizer(), new BigramSegmentParser(null));
		for (String str:testStrings) {
			String after = urlParser.parse(str);
			System.out.println(after);
		}
	}

}
