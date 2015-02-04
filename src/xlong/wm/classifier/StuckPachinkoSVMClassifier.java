package xlong.wm.classifier;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import weka.core.Instances;
import xlong.wm.classifier.adapter.SparseVectorSampleToWekaInstanceAdapter;
import xlong.wm.classifier.partsfactory.ClassifierPartsFactory;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;
import xlong.wm.sample.converter.TextToSparseVectorConverter;

public class StuckPachinkoSVMClassifier extends AbstractSingleLabelClassifier  {
	private Map<String, String> selecters;
	private Map<String, String> stuckers;
	private Map<String, TextToSparseVectorConverter> selectConverters;
	private Map<String, SparseVectorSampleToWekaInstanceAdapter> selectAdapters;
	private Map<String, TextToSparseVectorConverter> stuckConverters;
	private Map<String, SparseVectorSampleToWekaInstanceAdapter> stuckAdapters;
	private Map<String, TreeSet<String>> sons;
	protected ClassifierPartsFactory factory;
	
	private static String fileDir = "result/classifiers/";
	private static int fileID = 0;
	private static String extName = ".classifier";
	//private static final String OPTION = "-M";
	
	static {
		try {
			Files.createDirectories(Paths.get(fileDir));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public StuckPachinkoSVMClassifier(ClassifierPartsFactory factory) {
		selecters = new TreeMap<String,  String>();
		stuckers = new TreeMap<String,  String>();
		selectConverters = new TreeMap<String, TextToSparseVectorConverter>();
		selectAdapters = new TreeMap<String, SparseVectorSampleToWekaInstanceAdapter>();
		stuckConverters = new TreeMap<String, TextToSparseVectorConverter>();
		stuckAdapters = new TreeMap<String, SparseVectorSampleToWekaInstanceAdapter>();	
		sons = new TreeMap<String, TreeSet<String>>();
		this.factory = factory;
	}
	
	public StuckPachinkoSVMClassifier(StuckPachinkoSVMClassifier classifiers) {
		selecters = classifiers.selecters;
		stuckers = classifiers.stuckers;
		selectConverters = classifiers.selectConverters;
		stuckConverters = classifiers.stuckConverters;
		selectAdapters = classifiers.selectAdapters;
		stuckAdapters = classifiers.stuckAdapters;
		sons = classifiers.sons;
		factory = classifiers.factory;
	}
	
	private TextToSparseVectorConverter getNewConverter() {
		return factory.getNewConverter();
	}
	
	private weka.classifiers.Classifier getNewClassifier() {
		return factory.getNewClassifier();
	}
	
	
	private String join(String s1, String s2) {
		return s1 + "_" + s2;
	}
	
	private String newFilePath() {
		return fileDir + String.valueOf(fileID++) + extName;
	}
	
	private String saveClassifier(weka.classifiers.Classifier classifier) throws Exception {
		if (classifier == null) {
			return null;
		}
		String fileName = newFilePath();
		FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(classifier);
        oos.close();
		return fileName;
	}
	
	private weka.classifiers.Classifier loadClassifier(String fileName) throws Exception {
		if (fileName == null) {
			return null;
		}
		System.out.println("load and test... " + fileName);
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		weka.classifiers.Classifier classifier = (weka.classifiers.Classifier) ois.readObject();
		ois.close();
		return classifier;
	}
	
	@Override
	public void train(Composite composite) throws Exception {
		train(composite.getLabel().getText(), composite);
		for (Composite subcomp:composite.getComposites()) {
			train(subcomp);
		}
	}
	
	private void train(String label, Composite composite) throws Exception {
		if (composite.getComposites().size() == 0) {
			sons.put(label, null);
			return;
		}
		System.out.println(label);
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
			Vector<String> tags = new Vector<String>();
			tags.add("neg"); tags.add("pos");
			SparseVectorSampleToWekaInstanceAdapter adapter = new SparseVectorSampleToWekaInstanceAdapter(numOfAtts, tags);
			Instances instances = new Instances(adapter.getDataSet());
			for (Sample sample:vecComposite.getSamples()) {
				instances.add(adapter.adaptSample(sample, "pos"));
			}
			for (Composite subcomp:vecComposite.getComposites()) {
				addAll(instances, adapter, subcomp, "neg");
			}
			
			System.out.println("train stucker...");
			stucker.buildClassifier(instances);
			
			stuckers.put(label, saveClassifier(stucker));
			stuckAdapters.put(label, adapter);
			stuckConverters.put(label, converter);
			
			stucker = null;
			System.gc();
		}
		TreeSet<String> sublabels = new TreeSet<String>();
		
		Vector<String> tags = new Vector<String>();
		tags.add("neg"); tags.add("pos");
		
		TextToSparseVectorConverter converter = getNewConverter();
		//System.out.println("build selecter dictionary...");
		for (Composite subcompAll:composite.getComposites()) {
			converter.buildDictionary(subcompAll);
		}
		//System.out.println("determine selecter dictionary...");
		converter.determineDictionary();
		//System.out.println(converter.getDictionary().size());
		
		int numOfAtts = converter.dictionarySize();
		SparseVectorSampleToWekaInstanceAdapter adapter = new SparseVectorSampleToWekaInstanceAdapter(numOfAtts, tags);
		
		for (Composite subcomp:composite.getComposites()) {
			sublabels.add(subcomp.getLabel().getText());
			System.out.println("subcomp: " + join(label, subcomp.getLabel().getText()));
			
			weka.classifiers.Classifier selecter = getNewClassifier();
					
			//System.out.println("convert selecter...");
			Instances instances = new Instances(adapter.getDataSet());
			int cntPos = 0;
			for (Composite subcompOther:composite.getComposites()) {
				if (subcompOther.getLabel().compareTo(subcomp.getLabel()) != 0) {
					addAll(instances, adapter, converter.convert(subcompOther), "neg");
				} else {
					addAll(instances, adapter, converter.convert(subcompOther), "pos");
					cntPos += subcompOther.countSample();
				}
			}
			System.out.println("train selecter... instances: " + instances.numInstances() + " pos: " + cntPos);
			//System.out.println(instances.classAttribute().numValues());
			selecter.buildClassifier(instances);
			
			selecters.put(join(label, subcomp.getLabel().getText()), saveClassifier(selecter));
			selectAdapters.put(join(label, subcomp.getLabel().getText()), adapter);
			selectConverters.put(join(label, subcomp.getLabel().getText()), converter);	
			
			instances = null;
			selecter = null;
			System.gc();
		}
		sons.put(label, sublabels);
	}

	private void addAll(Instances instances, SparseVectorSampleToWekaInstanceAdapter adapter, Composite composite, String label) {
		for (Sample sample:composite.getSamples()) {
			instances.add(adapter.adaptSample(sample, label));
		}
		for (Composite subcomp:composite.getComposites()) {
			addAll(instances, adapter, subcomp, label);
		}
	}
	
	@Override
	public String test(Sample sample) throws Exception {
		return test("root", sample);
	}
	
	private String test(String label, Sample sample) throws Exception {
		TreeSet<String> subLabels = sons.get(label);
		if (subLabels == null) {
			return label;
		}
		weka.classifiers.Classifier stucker = loadClassifier(stuckers.get(label));
		if (stucker != null) {
			TextToSparseVectorConverter converter = stuckConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = stuckAdapters.get(label);
			int classID = (int)(stucker.classifyInstance(adapter.adaptSample(vecSample)) + 0.5);
			if (classID == 1) {
				return label;
			}
		}
		double maximum = -1;
		String maxLabel = null;
		for (String subLabel:subLabels) {
			weka.classifiers.Classifier selecter = loadClassifier(selecters.get(join(label, subLabel)));
			TextToSparseVectorConverter converter = selectConverters.get(join(label, subLabel));
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = selectAdapters.get(join(label, subLabel));
			double[] probs = selecter.distributionForInstance(adapter.adaptSample(vecSample));
			if (probs[1] > maximum) {
				maximum = probs[1];
				maxLabel = subLabel;
			}
		}
		return test(maxLabel, sample);
	}

	@Override
	public Vector<String> test(Vector<Sample> samples) throws Exception {
		return test("root", samples);
	}
	

	public Vector<String> test(String label, Vector<Sample> samples) throws Exception {
		Vector<String> results = new Vector<String>();
		TreeSet<String> sublabelSet= sons.get(label);
		
		int n = samples.size();
		
		if (sublabelSet == null) {
			for (int i = 0; i < n; i++) {
				results.add(label);
			}
			return results;
		}
		Vector<String> subLabels = new Vector<String>(sublabelSet);
		
		int m = subLabels.size();
		String[] nextLabel = new String[n];
		double[][] probs = new double[n][m];
		
		weka.classifiers.Classifier stucker = loadClassifier(stuckers.get(label));
		if (stucker != null) {
			for (int i = 0; i < n; i++) {
				Sample sample = samples.get(i);
				TextToSparseVectorConverter converter = stuckConverters.get(label);
				Sample vecSample = converter.convert(sample);
				SparseVectorSampleToWekaInstanceAdapter adapter = stuckAdapters.get(label);
				int classID = (int)(stucker.classifyInstance(adapter.adaptSample(vecSample)) + 0.5);
				if (classID == 1) {
					nextLabel[i] = label ;
				} else {
					nextLabel[i] = null;
				}
			}
		}
		stucker = null;

		for (int j = 0; j < m; j++) {
			String subLabel = subLabels.get(j);
			weka.classifiers.Classifier selecter = loadClassifier(selecters.get(join(label, subLabel)));
			TextToSparseVectorConverter converter = selectConverters.get(join(label, subLabel));
			SparseVectorSampleToWekaInstanceAdapter adapter = selectAdapters.get(join(label, subLabel));
			for (int i = 0; i < n; i++) {
				if (nextLabel[i] == null) {
					Sample sample = samples.get(i);
					Sample vecSample = converter.convert(sample);
					probs[i][j] = selecter.distributionForInstance(adapter.adaptSample(vecSample))[1];
				}
			}
		}
		
		for (int i = 0; i < n; i++) {
			if (nextLabel[i] == null) {
				double maximum = -1;
				for (int j = 0; j < m; j++) {
					if (probs[i][j] > maximum) {
						nextLabel[i] = subLabels.get(j);
						maximum = probs[i][j];
					}
				}
			}
		}
		
		for (int j = 0; j < m; j++) {
			String subLabel = subLabels.get(j);
			Vector<Sample> partsSamples = new Vector<Sample>();
			for (int i = 0; i < n; i++) {
				if (nextLabel[i] == subLabel) {
					partsSamples.add(samples.get(i));
				}
			}
			Vector<String> partsRes = test(subLabel, partsSamples);
			int idx = 0;
			for (int i = 0; i < n; i++) {
				if (nextLabel[i] == subLabel) {
					nextLabel[i] = partsRes.get(idx);
					idx++;
				}
			}			
		}
		for (int i = 0; i < n; i++) {
			results.add(nextLabel[i]);
		}
		return results;
	}
	
}
