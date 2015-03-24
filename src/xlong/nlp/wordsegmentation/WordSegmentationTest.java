package xlong.nlp.wordsegmentation;

import java.util.Vector;

public class WordSegmentationTest {
	private static String[] testStrings = new String[] {
		"mountainwitchrpg",
		"rpg",
		"choosespain",
		"thisisatest",
		"wheninthecourseofhumaneventsitbecomesnecessary",
		"whorepresents",
		"expertsexchange",
		"speedofart",
		"nowisthetimeforallgood",
		"itisatruthuniversallyacknowledged",
		"itwasabrightcolddayinaprilandtheclockswerestrikingthirteen",
		"itwasthebestoftimesitwastheworstoftimesitwastheageofwisdomitwastheageoffoolishness",
		"asgregorsamsaawokeonemorningfromuneasydreamshefoundhimselftransformedinhisbedintoagiganticinsect",
		"inaholeinthegroundtherelivedahobbitnotanastydirtywetholefilledwiththeendsofwormsandanoozysmellnoryetadrybaresandyholewithnothinginittositdownonortoeatitwasahobbitholeandthatmeanscomfort",
		"faroutintheunchartedbackwatersoftheunfashionableendofthewesternspiralarmofthegalaxyliesasmallunregardedyellowsun",
	};
	public static void main(String[] args) {
		WordSegmenter segmenter = WordSegmenters.getPeterNorvigUnigramSegmenter();
		for (String str:testStrings) {
			Vector<String> words = segmenter.segment(str);
			for (String word:words) {
				System.out.print(word + " ");
			}
			System.out.println();
		}
		System.out.println();
		segmenter = WordSegmenters.getPeterNorvigBigramSegmenter();
		for (String str:testStrings) {
			Vector<String> words = segmenter.segment(str);
			for (String word:words) {
				System.out.print(word + " ");
			}
			System.out.println();
		}
		System.out.println();
		segmenter = WordSegmenters.getWeightBigramSegmenter(-5);
		for (String str:testStrings) {
			Vector<String> words = segmenter.segment(str);
			for (String word:words) {
				System.out.print(word + " ");
			}
			System.out.println();
		}
	}
}
