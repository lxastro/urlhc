package xlong.nlp.wordsegmentation;

public class WordSegmenters {
	private static WordSegmenter uniSegmenter = null;
	private static WordSegmenter biSegmenter = null;
	private static WordSegmenter weightBiSegmenter = null;
	public static WordSegmenter getPeterNorvigUnigramSegmenter() {
		if (uniSegmenter == null) {
			uniSegmenter = new PeterNorvigUnigramSegmenter();
		}
		return uniSegmenter;
	}
	public static WordSegmenter getPeterNorvigBigramSegmenter() {
		if (biSegmenter == null) {
			biSegmenter = new PeterNorvigBigramSegmenter();
		}
		return biSegmenter;
	}
	public static WordSegmenter getWeightBigramSegmenter(double weight) {
		if (weightBiSegmenter == null) {
			weightBiSegmenter = new WeightBigramSegmenter(weight);
		}
		return weightBiSegmenter;
	}
}
