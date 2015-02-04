/**
 * Project : Classify URLs
 */
package xlong.wm.sample.converter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.TreeMap;

import xlong.nlp.tokenizer.Tokenizer;
import xlong.util.MyWriter;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Label;
import xlong.wm.sample.Sample;
import xlong.wm.sample.SparseVector;
import xlong.wm.sample.Text;

/**
 * Class to convert string to word vectore
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public class TextToSparseVectorConverter {
	
	private Tokenizer tokenizer;
	private static HashSet<String> stopwords = new HashSet<String>();
	private boolean s_lowerCaseTokens = false;
	private int s_filterShortWords = 0;
	private double s_ignoreSmallFeature = 0;
	private boolean s_useStoplist = false;
    private boolean s_outputCounts = false;
    private boolean s_TF = false;
    private boolean s_IDF = false;
    private boolean s_detemineByDocFreq = false;
	private int s_wordsToKeep;
	private int s_minTermFreq = 1;
	
	private static final int MAXWORDSTOKEEP = 100000000;
	
	private HashMap<String, Integer> dictionary;
	private HashMap<String, Count> wordCount;
	private HashMap<String, Count> docCount;
	private int[] docFreq;
	private int totalNumber;

	private class Count implements Serializable {
		private static final long serialVersionUID = -8640817304015954550L;
		
		int count = 0;

		public Count(int x) {
			count = x;
		}

		public void addOne() {
			count++;
		}
	}
	
	public TextToSparseVectorConverter(Tokenizer tokenizer) {
		this.wordCount = new HashMap<String, Count>();
		this.docCount = new HashMap<String, Count>();
		this.tokenizer = tokenizer;
		this.totalNumber = 0;
		s_wordsToKeep = MAXWORDSTOKEEP;
	}
	
	public TextToSparseVectorConverter enableStopwords() {
		s_useStoplist = true;
		return this;
	}
	public TextToSparseVectorConverter enableTF() {
		s_TF = true;
		return this;
	}
	public TextToSparseVectorConverter enableIDF() {
		s_IDF = true;
		return this;
	}
	public TextToSparseVectorConverter enableDetemineByDocFreq() {
		s_detemineByDocFreq = true;
		return this;
	}
	public TextToSparseVectorConverter setFilterShortWords(int filterMaxLength) {
		s_filterShortWords = filterMaxLength;
		return this;
	}
	public TextToSparseVectorConverter setIgnoreSmallFeatures(double ignoreMaxValue) {
		s_ignoreSmallFeature = ignoreMaxValue;
		return this;
	}
	public TextToSparseVectorConverter setMinTermFreq(int minTermFreq) {
		s_minTermFreq = minTermFreq;
		return this;
	}
	public TextToSparseVectorConverter enableOutputCount() {
		s_outputCounts = true;
		return this;
	}
	public TextToSparseVectorConverter enableLowerCaseToken() {
		s_lowerCaseTokens = true;
		return this;
	}
	public TextToSparseVectorConverter setWordToKeep(int wordToKeep) {
		s_wordsToKeep = wordToKeep;
		return this;
	}
	public static void addStopwords(String stopWordsFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(stopWordsFile));
		String word;
		while((word = in.readLine()) != null) {
			stopwords.add(word.toLowerCase());
		}
		in.close();
	}
	
	public static void addStopwords(BufferedReader in) throws IOException {
		String word;
		while((word = in.readLine()) != null) {
			stopwords.add(word.toLowerCase());
		}
		in.close();	
	}
	
	public static void writeStopwords(String stopWordsFile) throws IOException {
		MyWriter.setFile(stopWordsFile, false);
		for (String string : stopwords) {
			MyWriter.writeln(string);
		}
		MyWriter.close();
	}

	private static void sortArray(int[] array) {
		int i, j, h, N = array.length - 1;

		for (h = 1; h <= N / 9; h = 3 * h + 1) {
			;
		}

		for (; h > 0; h /= 3) {
			for (i = h + 1; i <= N; i++) {
				int v = array[i];
				j = i;
				while (j > h && array[j - h] > v) {
					array[j] = array[j - h];
					j -= h;
				}
				array[j] = v;
			}
		}
	}

	public void determineDictionary() {
		// Figure out the minimum required word frequency
		Map<String, Count> xCount;
		if (s_detemineByDocFreq == true) {
			xCount = docCount;
		} else {
			xCount = wordCount;
		}
		int array[] = new int[xCount.size()];
		Iterator<Count> it =xCount.values().iterator();
		int pos = 0;
		int prune;
		while (it.hasNext()) {
			Count count = it.next();
			array[pos] = count.count;
			pos++;
		}
		// sort the array
		sortArray(array);
		if (array.length < s_wordsToKeep) {
			// if there aren't enough words, set the threshold to minFreq
			prune = s_minTermFreq;
		} else {
			// otherwise set it to be at least minFreq
			prune = Math.max(s_minTermFreq, array[array.length - s_wordsToKeep]);
		}

		// Add the word vector attributes
		HashMap<String, Integer> newDictionary = new HashMap<String, Integer>();
		Iterator<Entry<String, Count>> ite = xCount.entrySet().iterator();
		
		int index = 0;
		while (ite.hasNext()) {
			Entry<String, Count> en = ite.next();
			String word = en.getKey();
			Count count = en.getValue();
			if (count.count >= prune) {
				if (!newDictionary.containsKey(word)) {
					newDictionary.put(word, new Integer(index++));
				}
			}
		}
		dictionary = newDictionary;
		docFreq = new int[dictionary.size()];
		for (Entry<String, Integer> en:dictionary.entrySet()) {
			docFreq[en.getValue().intValue()] = docCount.get(en.getKey()).count;
		}
		
		// Release memory
		wordCount = null;
		docCount = null;
	}

	public void buildDictionary(String text) {	
		
		List<String> words = tokenizer.tokenize(text);
	
		HashSet<String> wordsSet = new HashSet<String>();
		
		for (String word : words) {
			if (this.s_lowerCaseTokens == true) {
				word = word.toLowerCase();
			}
			if (this.s_useStoplist == true) {
				if (stopwords.contains(word)) {
					continue;
				}
			}
			if (word.length() <= s_filterShortWords) {
				continue;
			}
			if (!(wordCount.containsKey(word))) {
				wordCount.put(word, new Count(1));
			} else {
				wordCount.get(word).addOne();
			}
			wordsSet.add(word);
		}
		
		for (String word : wordsSet) {
			if (!docCount.containsKey(word)) {
				docCount.put(word, new Count(1));
			} else {
				docCount.get(word).addOne();
			}
		}
		
		totalNumber++;
		
	}
	
	public TreeMap<Integer, Double> convert(String text){
		TreeMap<Integer, Double> vector = new TreeMap<Integer, Double>();
		List<String> words = tokenizer.tokenize(text);
		for (String word : words) {
			if (this.s_lowerCaseTokens == true) {
				word = word.toLowerCase();
			}
			Integer index = dictionary.get(word);
			if (index != null) {
				if (s_outputCounts) {
					Double count = vector.get(index);
					if (count != null) {
						vector.put(index, new Double(count.doubleValue() + 1.0));
					} else {
						vector.put(index, new Double(1));
					}
				} else {
					vector.put(index, new Double(1));
				}
			}
		}
		
	    if (s_TF == true) {
	    	Iterator<Integer> it = vector.keySet().iterator();
	        while (it.hasNext()) {
	        	Integer index = it.next();
	            double val = vector.get(index).doubleValue();
	            val = Math.log(val + 1);
	            vector.put(index, new Double(val));
	        }
	    }
	    
	    if (s_IDF == true) {
	        Iterator<Integer> it = vector.keySet().iterator();
	        while(it.hasNext()) {
	        	Integer index = it.next();
	            double val = vector.get(index).doubleValue();
	            val = val * Math.log(totalNumber / (double) docFreq[index.intValue()]);
	            vector.put(index, new Double(val));
	        }
	    }
	    
	    TreeMap<Integer, Double> newVector = new TreeMap<Integer, Double>();
	    for (Entry<Integer, Double> en:vector.entrySet()){
	    	if (en.getValue() > s_ignoreSmallFeature) {
	    		newVector.put(en.getKey() + 1, en.getValue());
	    	}
	    }
	    
		return newVector;
	}
	
	public Collection<Map<Integer, Double>> convert(Collection<String> texts) {
		ArrayList<Map<Integer, Double>> vectors = new ArrayList<Map<Integer, Double>>();
		for (String text:texts) {
			vectors.add(convert(text));
		}
		return vectors;
	}
	
	public void buildDictionary(Composite textComposite) {
		for (Sample instance:textComposite.getSamples()) {
			buildDictionary(((Text) instance.getProperty()).getText());
		}
		for (Composite composite:textComposite.getComposites()) {
			buildDictionary(composite);
		}
	}
	
	public Sample convert(Sample textSample) {
		SparseVector sv = new SparseVector(convert((((Text) textSample.getProperty()).getText())), dictionary.size());
		Set<Label> labels = textSample.getLabels(); 
		return new Sample(sv, labels);
	}
	
	public Composite convert(Composite textComposite) {
		Composite vectorComposite = new Composite(textComposite.getLabel());
		for (Sample instance:textComposite.getSamples()) {
			vectorComposite.addSample(convert(instance));
		}
		for (Composite composite:textComposite.getComposites()) {
			vectorComposite.addComposite(convert(composite));
		}
		return vectorComposite;
	}
	
	public int dictionarySize() {
		return dictionary.size();
	}
	
	public void save(String filePath) throws IOException {
		FileOutputStream fos = new FileOutputStream(filePath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(s_ignoreSmallFeature);
        oos.writeObject(s_lowerCaseTokens);
        oos.writeObject(s_outputCounts);
        oos.writeObject(s_TF);
        oos.writeObject(s_IDF);
        oos.writeObject(totalNumber);
        oos.writeObject(docFreq);
        oos.writeObject(dictionary);
        oos.close();
	}
	
	@SuppressWarnings("unchecked")
	public static TextToSparseVectorConverter load(String filePath) throws Exception {
		TextToSparseVectorConverter converter = new TextToSparseVectorConverter(null);
		FileInputStream fis = new FileInputStream(filePath);
		ObjectInputStream ois = new ObjectInputStream(fis);
		converter.s_ignoreSmallFeature = (double) ois.readObject();
		converter.s_lowerCaseTokens = (boolean) ois.readObject();
		converter.s_outputCounts = (boolean) ois.readObject();
		converter.s_TF = (boolean) ois.readObject();
		converter.s_IDF = (boolean) ois.readObject();
		converter.totalNumber = (int) ois.readObject();
		converter.docFreq = (int[]) ois.readObject();
		converter.dictionary = (HashMap<String, Integer>) ois.readObject();
		ois.close();
		return converter;
	}
	
}
