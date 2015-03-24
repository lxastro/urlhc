package xlong.nlp.wordsegmentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

public class WeightBigramSegmenter implements WordSegmenter {

	private static final int MAXWORDLENGTH = 20;
	private PDist pDist;
	private HashMap<String, Pair> segment2Memo;
	private double weight;
	
	WeightBigramSegmenter(double weight) {
		this.weight = weight;
		pDist = new PDist();
		segment2Memo = new HashMap<String, Pair>();
	}

	@Override
	public Vector<String> segment(String text) {
		if (text == null) {
			return null;
		}
		segment2Memo = new HashMap<String, Pair>();
		return segment2(text, "<S>").words;
	}
	
	class Pair implements Comparable<Pair>{
		Pair(double prob, Vector<String> words) {
			this.prob = prob;
			this.words = words;
		}
		double prob;
		Vector<String> words;
		@Override
		public int compareTo(Pair o) {
			if (this.prob < o.prob) {
				return -1;
			} else if (this.prob > o.prob) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	public Pair segment2(String text, String prev) {
		if (text.length() == 0) {
			return new Pair(0.0, new Vector<String>());
		}
		String args = prev + " " + text;
		if (segment2Memo.containsKey(args)) {
			return segment2Memo.get(args);
		}
		Vector<Pair> candidates = new Vector<Pair>();
		for (int i = 1; i <= Math.min(MAXWORDLENGTH, text.length()); i++) {
			String first = text.substring(0, i);
			String rem = text.substring(i);
			Pair subPair = segment2(rem, first);
			double prob = Math.log10(pDist.cpword(first, prev)) + subPair.prob;
			Vector<String> words = new Vector<String>();
			words.add(text.substring(0, i));
			words.addAll(subPair.words);
			candidates.add(new Pair(prob,  words));
		}
		Pair best = Collections.max(candidates);
		segment2Memo.put(args, best);
		return best;
	}

	class PDist {
		private HashMap<String, Long> gramMap;
		private final String unigramResouse = "/data/count_1w.txt";
		private final String bigramResouse = "/data/count_2w.txt";
		private final long n = 1024908267229l;

		PDist(){
			gramMap = new HashMap<String, Long>();
			read(unigramResouse);
			read(bigramResouse);
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
		//Conditional probability of word
		double cpword(String word, String prev) {
			String args = prev + " " + word;
			if (gramMap.containsKey(args)) {
				return ((double)gramMap.get(args))/n/pword(prev);
			} else {
				return pword(word);
			}
		}
		double missingPWord(String word) {
			return Math.pow(10.0, weight)/(n * Math.pow(10.0, word.length()));
		}
	}
}
