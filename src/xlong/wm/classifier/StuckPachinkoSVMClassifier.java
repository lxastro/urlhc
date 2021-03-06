package xlong.wm.classifier;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import weka.core.Instances;
import xlong.util.FileUtil;
import xlong.wm.classifier.adapter.SparseVectorSampleToWekaInstanceAdapter;
import xlong.wm.classifier.partsfactory.ClassifierPartsFactory;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;
import xlong.wm.sample.converter.TextToSparseVectorConverter;

public class StuckPachinkoSVMClassifier extends AbstractSingleLabelClassifier  {

	private static final long serialVersionUID = -5882370481198868075L;
	private Map<String, String> selecters;
	private Map<String, String> stuckers;
	private Map<String, TextToSparseVectorConverter> selectConverters;
	private Map<String, SparseVectorSampleToWekaInstanceAdapter> selectAdapters;
	private Map<String, TextToSparseVectorConverter> stuckConverters;
	private Map<String, SparseVectorSampleToWekaInstanceAdapter> stuckAdapters;
	private Map<String, TreeSet<String>> sons;
	protected ClassifierPartsFactory factory;
	
	private int fileID = 0;
	
	private static String modelExt = ".svm";


	public StuckPachinkoSVMClassifier(ClassifierPartsFactory factory, String modelDir) {
		super(factory, modelDir);
		selecters = new TreeMap<String,  String>();
		stuckers = new TreeMap<String,  String>();
		selectConverters = new TreeMap<String, TextToSparseVectorConverter>();
		selectAdapters = new TreeMap<String, SparseVectorSampleToWekaInstanceAdapter>();
		stuckConverters = new TreeMap<String, TextToSparseVectorConverter>();
		stuckAdapters = new TreeMap<String, SparseVectorSampleToWekaInstanceAdapter>();	
		sons = new TreeMap<String, TreeSet<String>>();
		this.factory = factory;
	}
	
	private void initDir() throws Exception {
		FileUtil.createDir(modelDir);
	}
	
	private TextToSparseVectorConverter getNewConverter() {
		return factory.getNewConverter();
	}
	
	private weka.classifiers.Classifier getNewClassifier() {
		return factory.getNewWekaClassifier();
	}
	
	
	private String join(String s1, String s2) {
		return s1 + "_" + s2;
	}
	
	private String newFilePath() {
		return modelDir + String.valueOf(fileID++) + modelExt;
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

	public static StuckPachinkoSVMClassifier load(String modelDir) throws Exception {
		modelDir = FileUtil.addTralingSlash(modelDir);
		String fileName = getModelName(modelDir);
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		StuckPachinkoSVMClassifier classifier = (StuckPachinkoSVMClassifier) ois.readObject();
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
	public OutputStructure test(Sample sample) throws Exception {
		return test("root", 1.0, sample);
	}
	
	private OutputStructure test(String label, double p, Sample sample) throws Exception {
		TreeSet<String> subLabels = sons.get(label);
		if (subLabels == null) {
			return new OutputStructure(label, p);
		}
		weka.classifiers.Classifier stucker = loadClassifier(stuckers.get(label));
		double pstuck = 0;
		if (stucker != null) {
			TextToSparseVectorConverter converter = stuckConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = stuckAdapters.get(label);
			pstuck = stucker.classifyInstance(adapter.adaptSample(vecSample));
			int classID = (int)(pstuck + 0.5);
			if (classID == 1) {
				return new OutputStructure(label, p*pstuck);
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
		return test(maxLabel, p*(1-pstuck)*maximum, sample);
	}

	@Override
	public Vector<OutputStructure> test(Vector<Sample> samples) throws Exception {
		Vector<Double> ps = new Vector<Double>();
		int n = samples.size();
		for (int i = 0; i < n; i++) {
			ps.add(1.0);
		}
		return test("root", ps, samples);
	}
	

	public Vector<OutputStructure> test(String label, Vector<Double> ps, Vector<Sample> samples) throws Exception {
		Vector<OutputStructure> results = new Vector<OutputStructure>();
		TreeSet<String> sublabelSet= sons.get(label);
		
		int n = samples.size();
		
		if (sublabelSet == null) {
			for (int i = 0; i < n; i++) {
				results.add(new OutputStructure(label, ps.get(i)));
			}
			return results;
		}
		Vector<String> subLabels = new Vector<String>(sublabelSet);
		
		int m = subLabels.size();
		double[] nextP = new double[n];
		for (int i = 0 ; i < n; i++) {
			nextP[i] = ps.get(i);
		}
		String[] nextLabel = new String[n];
		double[][] probs = new double[n][m];
		int[] cnts = new int[n];
		
		weka.classifiers.Classifier stucker = loadClassifier(stuckers.get(label));
		if (stucker != null) {
			for (int i = 0; i < n; i++) {
				Sample sample = samples.get(i);
				TextToSparseVectorConverter converter = stuckConverters.get(label);
				Sample vecSample = converter.convert(sample);
				SparseVectorSampleToWekaInstanceAdapter adapter = stuckAdapters.get(label);
				double pstuck = stucker.distributionForInstance(adapter.adaptSample(vecSample))[1];
				int classID = (int)(pstuck + 0.5);
				if (classID == 1) {
					nextP[i] = nextP[i] * pstuck;
					nextLabel[i] = label;
				} else {
					nextP[i] = nextP[i] * (1.0-pstuck);
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
				cnts[i] = 0;
				if (nextLabel[i] == null) {
					cnts[i] ++;
					Sample sample = samples.get(i);
					Sample vecSample = converter.convert(sample);
					probs[i][j] = selecter.distributionForInstance(adapter.adaptSample(vecSample))[1];
				}
			}
		}
		
		for (int i = 0; i < n; i++) {
			if (nextLabel[i] == null) {
				double maximum = -1;
				double tot = 0;
				for (int j = 0; j < m; j++) {
					tot += probs[i][j];
					if (probs[i][j] > maximum) {
						nextLabel[i] = subLabels.get(j);
						maximum = probs[i][j];
					}
				}
				double p = 1.0/cnts[i];
				if (tot > 1e-8) {
					p = maximum/tot;
				}
				nextP[i] = nextP[i] * p;
			}
		}
		
		for (int j = 0; j < m; j++) {
			String subLabel = subLabels.get(j);
			Vector<Sample> partsSamples = new Vector<Sample>();
			Vector<Double> partsP = new Vector<Double>();
			for (int i = 0; i < n; i++) {
				if (nextLabel[i] == subLabel) {
					partsSamples.add(samples.get(i));
					partsP.add(nextP[i]);
				}
			}
			Vector<OutputStructure> partsRes = test(subLabel, partsP, partsSamples);
			int idx = 0;
			for (int i = 0; i < n; i++) {
				if (nextLabel[i] == subLabel) {
					nextLabel[i] = partsRes.get(idx).getLabel();
					nextP[i] = partsRes.get(idx).getP();
					idx++;
				}
			}			
		}
		for (int i = 0; i < n; i++) {
			results.add(new OutputStructure(nextLabel[i], nextP[i]));
		}
		return results;
	}
}
