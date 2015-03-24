package xlong.wm.classifier;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import weka.core.Instances;
import xlong.util.FileUtil;
import xlong.util.OptionsUtil;
import xlong.wm.classifier.adapter.SparseVectorSampleToWekaInstanceAdapter;
import xlong.wm.classifier.partsfactory.ClassifierPartsFactory;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;
import xlong.wm.sample.converter.TextToSparseVectorConverter;

public class StuckTopDownMultiBaseClassifier extends AbstractSingleLabelClassifier  {

	private static final long serialVersionUID = -2871445498610738891L;
	protected Map<String, weka.classifiers.Classifier> selecters;
	protected Map<String, weka.classifiers.Classifier> stuckers;
	protected Map<String, TextToSparseVectorConverter> selectConverters;
	protected Map<String, SparseVectorSampleToWekaInstanceAdapter> selectAdapters;
	protected Map<String, TextToSparseVectorConverter> stuckConverters;
	protected Map<String, SparseVectorSampleToWekaInstanceAdapter> stuckAdapters;
	protected Map<String, TreeSet<String>> sons;
	protected ClassifierPartsFactory factory;
	
	protected String testType;
	protected int beamWidth;
	
	private static String modelExt = ".model";
	
	public StuckTopDownMultiBaseClassifier(ClassifierPartsFactory factory, String modelDir) {
		super(factory, modelDir);
		selecters = new TreeMap<String, weka.classifiers.Classifier>();
		stuckers = new TreeMap<String, weka.classifiers.Classifier>();
		selectConverters = new TreeMap<String, TextToSparseVectorConverter>();
		selectAdapters = new TreeMap<String, SparseVectorSampleToWekaInstanceAdapter>();
		stuckConverters = new TreeMap<String, TextToSparseVectorConverter>();
		stuckAdapters = new TreeMap<String, SparseVectorSampleToWekaInstanceAdapter>();	
		sons = new TreeMap<String, TreeSet<String>>();
		this.factory = factory;
	}
	
	private void getTestOptions() {
		Map<String, String> options = OptionsUtil.parseOptions(testArgs);
		testType = options.get("-testMethod");
		if (testType.startsWith("BeamSearch")) {
			beamWidth = Integer.parseInt(testType.substring(10).trim());
			testType = "BeamSearch";
		}
	}
	
	private void initDir() throws Exception {
		FileUtil.createDir(modelDir);
	}
	
	private static String getModelName(String modelDir) {
		return modelDir + "root" + modelExt;
	}

	@Override
	public void save() throws Exception {
		String fileName = getModelName(modelDir);
		FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
	}

	public static StuckTopDownMultiBaseClassifier load(String modelDir) throws Exception {
		modelDir = FileUtil.addTralingSlash(modelDir);
		String fileName = getModelName(modelDir);
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		StuckTopDownMultiBaseClassifier classifier = (StuckTopDownMultiBaseClassifier) ois.readObject();
		ois.close();
		return classifier;
	}
	
	@Override
	public void train(Composite composite) throws Exception {
		initDir();
		train(composite.getLabel().getText(), composite);
		for (Composite subcomp:composite.getComposites()) {
			train(subcomp);
		}
	}
	
	private TextToSparseVectorConverter getNewConverter() {
		return factory.getNewConverter();
	}
	
	private weka.classifiers.Classifier getNewClassifier() {
		return factory.getNewWekaClassifier();
	}
	
	private void train(String label, Composite composite) throws Exception {
		if (composite.getComposites().size() == 0) {
			sons.put(label, null);
			return;
		}
		System.out.println("train: " + label);
		if (composite.getSamples().size() == 0) {
			stuckers.put(label, null);
		} else {
			weka.classifiers.Classifier stucker = getNewClassifier();
			
			TextToSparseVectorConverter converter = getNewConverter();
			//System.out.println("build stucker dictionary...");
			converter.buildDictionary(composite);
			//System.out.println("determine stucker dictionary...");
			converter.determineDictionary();
			//System.out.println(converter.getDictionary().size());
			//System.out.println("convert stucker...");
			Composite vecComposite = converter.convert(composite);
			
			int numOfAtts = converter.dictionarySize();
			Vector<String> labels = new Vector<String>();
			labels.add("neg"); labels.add("pos");
			SparseVectorSampleToWekaInstanceAdapter adapter = new SparseVectorSampleToWekaInstanceAdapter(numOfAtts, labels);
			Instances instances = new Instances(adapter.getDataSet());
			for (Sample sample:vecComposite.getSamples()) {
				instances.add(adapter.adaptSample(sample, "pos"));
			}
			for (Composite subcomp:vecComposite.getComposites()) {
				addAll(instances, adapter, subcomp, "neg");
			}
			
			//System.out.println("train stucker...");
			stucker.buildClassifier(instances);
			
			stuckers.put(label, stucker);
			stuckAdapters.put(label, adapter);
			stuckConverters.put(label, converter);
		}
		
		weka.classifiers.Classifier selecter = getNewClassifier();
		
		TextToSparseVectorConverter converter = getNewConverter();
		
		Vector<String> labels = new Vector<String>();
		//System.out.println("build selecter dictionary...");
		for (Composite subcomp:composite.getComposites()) {
			labels.add(subcomp.getLabel().getText());
			converter.buildDictionary(subcomp);
			//System.out.println(subcomp.getLabel().getText());
		}
		sons.put(label, new TreeSet<String>(labels));
		if (labels.size() == 1) {
			return;
		}
		//System.out.println(labels.size());
		//System.out.println("determine selecter dictionary...");
		converter.determineDictionary();
		//System.out.println(converter.getDictionary().size());
		
		//System.out.println("convert selecter...");
		int numOfAtts = converter.dictionarySize();
		SparseVectorSampleToWekaInstanceAdapter adapter = new SparseVectorSampleToWekaInstanceAdapter(numOfAtts, labels);
		//System.out.println(labels.size());
		
		Instances instances = new Instances(adapter.getDataSet());
		for (Composite subcomp:composite.getComposites()) {
			addAll(instances, adapter, converter.convert(subcomp), subcomp.getLabel().getText());
		}
		
		//System.out.println("train selecter...");
		selecter.buildClassifier(instances);
		
		selecters.put(label, selecter);
		selectAdapters.put(label, adapter);
		selectConverters.put(label, converter);		
	}

	private void addAll(Instances instances, SparseVectorSampleToWekaInstanceAdapter adapter, Composite composite, String label) {
		for (Sample sample:composite.getSamples()) {
//			if (adapter.adaptSample(sample,label).classValue() > 3) {
//				System.out.println(adapter.adaptSample(sample,label).classValue());
//				System.out.println(sample.getProperty().getOneLineString());
//				System.out.println(label);
//			}
			instances.add(adapter.adaptSample(sample, label));
		}
		for (Composite subcomp:composite.getComposites()) {
			addAll(instances, adapter, subcomp, label);
		}
	}
	
	class Pair{
		String label;
		double prob;
		public Pair(String label, double prob) {
			this.label = label;
			this.prob = prob;
		}
	}
	
	class PairComp implements Comparator<Pair> {

		@Override
		public int compare(Pair o1, Pair o2) {
			return -(int)Math.signum(o1.prob-o2.prob);
		}
		
	}
	
	@Override
	public Vector<OutputStructure> test(Vector<Sample> samples) throws Exception {
		Vector<OutputStructure> results = new Vector<OutputStructure>();
		int cnt = 0;
		for (Sample sample:samples) {
			cnt ++;
			if (cnt % 50000 == 0) {
				System.out.println(cnt);
			}
			results.add(test(sample));
		}
		return results;
	}
	
	
	@Override
	public OutputStructure test(Sample sample) throws Exception {
		getTestOptions();
		Pair pair;
		switch (testType) {
		case "AllPath":
			pair = testAllPath("root", sample);
			break;
		case "BeamSearch":
			pair = testBeamSearch("root", sample).peek();
			break;
		default:
			pair = testPachinko("root", sample);
			break;
		}
		return new OutputStructure(pair.label,pair.prob);
	}
	
	private Pair testPachinko(String label, Sample sample) throws Exception {
		
		if (sons.get(label) == null) {
			return new Pair(label, 1.0);
		}
		weka.classifiers.Classifier stucker = stuckers.get(label);
		double unstuckProb = 1.0;
		if (stucker != null) {
			TextToSparseVectorConverter converter = stuckConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = stuckAdapters.get(label);
			
			double[] probs = stucker.distributionForInstance(adapter.adaptSample(vecSample));
			int classID = (int)(probs[1] + 0.5);
			String str = adapter.getDataSet().classAttribute().value(0);
			int posID;
			if (str.equals("pos")) {
				posID = 0;
			} else {
				posID = 1;			
			}
			if (classID == posID) {
				return new Pair(label, probs[classID]);
			} else {
				unstuckProb = probs[1-classID];
			}
		}
		if (sons.get(label).size() == 1) {
			Pair subPair = testPachinko(sons.get(label).first(), sample);
			return new Pair(subPair.label, unstuckProb * 1.0 * subPair.prob);
		} else {
			weka.classifiers.Classifier selecter = selecters.get(label);
			TextToSparseVectorConverter converter = selectConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = selectAdapters.get(label);
			
			double[] probs = selecter.distributionForInstance(adapter.adaptSample(vecSample));
			int classID = argmax(probs);
			String subLabel = adapter.getDataSet().classAttribute().value(classID);
			
			Pair subPair = testAllPath(subLabel, sample);
			return new Pair(subPair.label, unstuckProb * probs[classID] * subPair.prob);	
		}
	}
	
	private static int argmax(double[] xs) {
		int idx = 0;
		for (int i = 1; i < xs.length; i++) {
			if (xs[i] > xs[idx]) {
				idx = i;
			}
		}
		return idx;
	}
	
	private PriorityQueue<Pair> testBeamSearch(String label, Sample sample) throws Exception {
		PriorityQueue<Pair> pairs = new PriorityQueue<Pair>(new PairComp());
		if (sons.get(label) == null) {
			pairs.add(new Pair(label, 1.0));
			return pairs;
		}
		weka.classifiers.Classifier stucker = stuckers.get(label);
		double unstuckProb = 1.0;
		if (stucker != null) {
			TextToSparseVectorConverter converter = stuckConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = stuckAdapters.get(label);
			double[] probs = stucker.distributionForInstance(adapter.adaptSample(vecSample));
			String str = adapter.getDataSet().classAttribute().value(0);
			if (str.equals("pos")) {
				pairs.add(new Pair(label, probs[0]));
				unstuckProb = probs[1];
			} else {
				pairs.add(new Pair(label, probs[1]));
				unstuckProb = probs[0];				
			}
		}
		if (sons.get(label).size() == 1) {
			PriorityQueue<Pair> subPairs = testBeamSearch(sons.get(label).first(), sample);
			for (Pair pair:subPairs) {
				pairs.add(new Pair(pair.label, unstuckProb * 1.0 * pair.prob));
			}
		} else {
			weka.classifiers.Classifier selecter = selecters.get(label);
			TextToSparseVectorConverter converter = selectConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = selectAdapters.get(label);
			double[] probs = selecter.distributionForInstance(adapter.adaptSample(vecSample));
			int n = probs.length;
			PriorityQueue<Pair> nowPairs = new PriorityQueue<Pair>(new PairComp());
			for (int i = 0; i < n; i++) {
				String subLabel = adapter.getDataSet().classAttribute().value(i);
				nowPairs.add(new Pair(subLabel, probs[i]));
			}
			for (int i = 0; i < beamWidth; i++) {
				if (!nowPairs.isEmpty()) {
					Pair exPair = nowPairs.poll();
					PriorityQueue<Pair> subPairs = testBeamSearch(exPair.label, sample);
					for (Pair pair:subPairs) {
						pairs.add(new Pair(pair.label, unstuckProb * exPair.prob * pair.prob));
					}
				} else {
					break;
				}
			}
		}
		if (pairs.size() <= beamWidth) {
			return pairs;
		}
		PriorityQueue<Pair> newPairs = new PriorityQueue<Pair>(new PairComp());
		for (int i = 0; i < beamWidth; i++) {
			if (!pairs.isEmpty()) {
				newPairs.add(pairs.poll());
			} else {
				break;
			}
		}
		return newPairs;
	}
	
	private Pair testAllPath(String label, Sample sample) throws Exception {
		
		PriorityQueue<Pair> pairs = new PriorityQueue<Pair>(new PairComp());
		if (sons.get(label) == null) {
			return new Pair(label, 1.0);
		}
		weka.classifiers.Classifier stucker = stuckers.get(label);
		double unstuckProb = 1.0;
		if (stucker != null) {
			TextToSparseVectorConverter converter = stuckConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = stuckAdapters.get(label);
			double[] probs = stucker.distributionForInstance(adapter.adaptSample(vecSample));
			String str = adapter.getDataSet().classAttribute().value(0);
			if (str.equals("pos")) {
				pairs.add(new Pair(label, probs[0]));
				unstuckProb = probs[1];
			} else {
				pairs.add(new Pair(label, probs[1]));
				unstuckProb = probs[0];				
			}
		}
		if (sons.get(label).size() == 1) {
			Pair subPair = testAllPath(sons.get(label).first(), sample);
			pairs.add(new Pair(subPair.label, unstuckProb * 1.0 * subPair.prob));
		} else {
			weka.classifiers.Classifier selecter = selecters.get(label);
			TextToSparseVectorConverter converter = selectConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = selectAdapters.get(label);
			double[] probs = selecter.distributionForInstance(adapter.adaptSample(vecSample));
			int n = probs.length;
			for (int i = 0; i < n; i++) {
				String subLabel = adapter.getDataSet().classAttribute().value(i);
				Pair subPair = testAllPath(subLabel, sample);
				pairs.add(new Pair(subPair.label, unstuckProb * probs[i] * subPair.prob));
			}
		}
		
		return pairs.poll();		
	}
}
