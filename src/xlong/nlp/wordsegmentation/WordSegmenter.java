package xlong.nlp.wordsegmentation;

import java.util.Vector;

public interface WordSegmenter {
	public Vector<String> segment(String text);
}
