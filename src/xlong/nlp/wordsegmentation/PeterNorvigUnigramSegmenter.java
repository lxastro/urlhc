package xlong.nlp.wordsegmentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

public class PeterNorvigUnigramSegmenter implements WordSegmenter {

	private static final int MAXWORDLENGTH = 20;
	private PDist pDist;
	private HashMap<String, Vector<String>> segmentMemo;
	
	PeterNorvigUnigramSegmenter() {
		pDist = new PDist();
		segmentMemo = new HashMap<String, Vector<String>>();
	}

	@Override
	public Vector<String> segment(String text) {
		return segment(text, true);
	}
	
	private Vector<String> segment(String text, boolean delete) {
		if (delete) {
			segmentMemo = new HashMap<String, Vector<String>>();
		}
		if (text == null) {
			return null;
		}
		if (segmentMemo.containsKey(text)) {
			return segmentMemo.get(text);
		}
		double maxProb = -Double.MAX_VALUE;
		Vector<String> words = new Vector<String>();
		for (int i = 1; i <= Math.min(MAXWORDLENGTH, text.length()); i++) {
			Vector<String> candidate = new Vector<String>();
			candidate.add(text.substring(0, i));
			candidate.addAll(segment(text.substring(i), false));
			double prob = pwords(candidate);
			if (prob > maxProb) {
				maxProb = prob;
				words = candidate;
			}
		}
		segmentMemo.put(text, words);
		return words;
		
	}
	
	private double pwords(Vector<String> words) {
		double prob = 0.0;
		for (String word:words) {
			prob += Math.log10(pDist.pword(word));
		}
		return prob;
	}

	class PDist {
		private HashMap<String, Long> gramMap;
		private final String unigramResouse = "/data/count_1w.txt";
		private final long n = 1024908267229l;

		PDist(){
			gramMap = new HashMap<String, Long>();
			read(unigramResouse);
		}
		private void read(String fileResourse) {
			BufferedReader in;
			try {
				in = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(fileResourse)));
				String line;
				while ((line = in.readLine()) != null) {
					String[] strs = line.split("\t");
					long cnt = Long.parseLong(strs[1]);
					gramMap.put(strs[0], cnt);
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		double pword(String word) {
			if (gramMap.containsKey(word)) {
				return ((double)gramMap.get(word))/n;
			} else {
				return missingPWord(word);
			}
		}
		double missingPWord(String word) {
			return 10.0/(n * Math.pow(10.0, word.length()));
		}
	}
}
