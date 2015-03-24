package xlong.wm.classifier;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import xlong.util.FileUtil;
import xlong.util.OptionsUtil;
import xlong.wm.classifier.partsfactory.ClassifierPartsFactory;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;
import xlong.wm.sample.SparseVector;
import xlong.wm.sample.converter.TextToSparseVectorConverter;
import xlong.wm.vw.VWUtil;

public class StuckMultiVWClassifier extends AbstractSingleLabelClassifier  {

	private static final long serialVersionUID = -1579838531910064576L;
	private Map<String, String> selecters;
	private Map<String, String> stuckers;
	private Map<String, TextToSparseVectorConverter> selectConverters;
	private Map<String, TextToSparseVectorConverter> stuckConverters;
	private Map<String, TreeSet<String>> sons;
	protected ClassifierPartsFactory factory;
	
	private int npasses;
	
	private int fileID = 0;
	private Vector<Integer> cntClass;
	
	private String inputDir;
	private String cacheDir;

	private static String subInputDir = "input/";
	private static String subCacheDir = "cache/";
	private static String inputExt = ".classifier";
	private static String modelExt = ".mvw";
	private static String cacheExt = ".cache";

	public StuckMultiVWClassifier(ClassifierPartsFactory factory, String modelDir) {
		super(factory, modelDir);
		selecters = new TreeMap<String,  String>();
		stuckers = new TreeMap<String,  String>();
		selectConverters = new TreeMap<String, TextToSparseVectorConverter>();
		stuckConverters = new TreeMap<String, TextToSparseVectorConverter>();
		sons = new TreeMap<String, TreeSet<String>>();
		cntClass = new Vector<Integer>();
		this.factory = factory;
		getTrainOptions();
	}
	
	private void getTrainOptions() {
		Map<String, String> options = OptionsUtil.parseOptions(trainArgs);
		npasses = Integer.parseInt(options.get("-passes"));
	}
	
	private void initDir() throws Exception {
		inputDir = tempDir + subInputDir;
		cacheDir = tempDir + subCacheDir;
		FileUtil.createDir(inputDir);
		FileUtil.createDir(modelDir);
		FileUtil.createDir(cacheDir);
	}
	
	private void finalizeDir() throws Exception {
		FileUtil.deleteDir(cacheDir);
		FileUtil.deleteDir(inputDir);
	}
	
	private TextToSparseVectorConverter getNewConverter() {
		return factory.getNewConverter();
	}

	private String newFilePath() {
		return inputDir + String.valueOf(fileID++) + inputExt;
	}
	
	private String lastFileID() {
		return String.valueOf(fileID - 1);
	}
	
	private String loadModel(String fileID) {
		if (fileID == null) {
			return null;
		}
		return modelDir + fileID + modelExt;
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

	public static StuckMultiVWClassifier load(String modelDir) throws Exception {
		modelDir = FileUtil.addTralingSlash(modelDir);
		String fileName = getModelName(modelDir);
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		StuckMultiVWClassifier classifier = (StuckMultiVWClassifier) ois.readObject();
		ois.close();
		return classifier;
	}
	
	@Override
	public void train(Composite composite) throws Exception {
		initDir();
		getInputFile(composite);
		getModel();
		finalizeDir();
	}
	
	private void getModel() throws Exception {
		for (int i = 0; i < fileID; i++) {
			String line = "vw -d " + (inputDir + String.valueOf(i) + inputExt);
			line += " --loss_function logistic --cache_file " + (cacheDir + String.valueOf(i) + cacheExt); 
			line += " --oaa " + (cntClass.get(i));
			line += " --passes " + npasses + " --l1 1e-8 -f " + (modelDir + String.valueOf(i) + modelExt);
			VWUtil.runCommand(line);
		}
	}
	
	private void getInputFile(Composite composite) throws Exception {
		getInputFile(composite.getLabel().getText(), composite);
		for (Composite subcomp:composite.getComposites()) {
			getInputFile(subcomp);
		}
	}
	
	private void getInputFile(String label, Composite composite) throws Exception {
		if (composite.getComposites().size() == 0) {
			sons.put(label, null);
			return;
		}
		System.out.println(label);
		if (composite.getSamples().size() == 0) {
			stuckers.put(label, null);
		} else {
			TextToSparseVectorConverter converter = getNewConverter();
			converter.buildDictionary(composite);
			converter.determineDictionary();
			Composite vecComposite = converter.convert(composite);
			
			Vector<Integer> labels = new Vector<Integer>();
			Vector<SparseVector> vectors = new Vector<SparseVector>();
			
			int cntPos = vecComposite.getSamples().size();
			int cntNeg = 0;
			for (Sample sample:vecComposite.getSamples()) {
				labels.add(1);
				vectors.add((SparseVector) sample.getProperty());
			}
			for (Composite subcomp:vecComposite.getComposites()) {
				addAll(labels, vectors, subcomp, 0);
				cntNeg += subcomp.countSample();
			}
			
			System.out.println("get input file stucker... instances: " + (cntNeg + cntPos) + " pos: " + cntPos);
			
			String classifierPath = newFilePath();
			cntClass.add(2);
			VWUtil.createMultiClassInputFile(labels, vectors, classifierPath);
			
			stuckers.put(label, lastFileID());
			stuckConverters.put(label, converter);
			
		}
		
		TextToSparseVectorConverter converter = getNewConverter();
		
		Vector<String> sublabels = new Vector<String>();
		for (Composite subcomp:composite.getComposites()) {
			sublabels.add(subcomp.getLabel().getText());
			converter.buildDictionary(subcomp);
		}
		sons.put(label, new TreeSet<String>(sublabels));
		
		System.out.println("n sublabels: " + sublabels.size());
		
		if (sublabels.size() == 1) {
			return;
		}
			
		converter.determineDictionary();
		
	
		Vector<Integer> labels = new Vector<Integer>();
		Vector<SparseVector> vectors = new Vector<SparseVector>();
		
		int cnt = 0;
		int classID = 0;
		for (Composite subcompOther:composite.getComposites()) {
			addAll(labels, vectors, converter.convert(subcompOther), classID);
			cnt += subcompOther.countSample();
			classID ++;
		}
		System.out.println("get input file selecter... instances: " + cnt + " classes: " + classID);
		
		String classifierPath = newFilePath();
		cntClass.add(classID);
		VWUtil.createMultiClassInputFile(labels, vectors, classifierPath);
		
		selecters.put(label , lastFileID());
		selectConverters.put(label, converter);	

	}

	private void addAll(Vector<Integer> labels, Vector<SparseVector> vectors, Composite composite, int label) {
		for (Sample sample:composite.getSamples()) {
			labels.add(label);
			vectors.add((SparseVector) sample.getProperty());
		}
		for (Composite subcomp:composite.getComposites()) {
			addAll(labels, vectors, subcomp, label);
		}
	}

	@Override
	public OutputStructure test(Sample sample) throws Exception {
		Vector<Sample> samples = new Vector<>();
		samples.add(sample);
		return test(samples).firstElement();
	}
	
	@Override
	public Vector<OutputStructure> test(Vector<Sample> samples) throws Exception {
		Vector<OutputStructure> outputs;
		outputs = testAllPath(samples);
		return outputs;
	}
	
	public Vector<OutputStructure> testAllPath(Vector<Sample> samples) throws Exception {
		Vector<OutputStructure> outputs = new Vector<OutputStructure>();
		Map<String, Vector<Vector<Double>>> probs = new HashMap<String, Vector<Vector<Double>>>();
		calProbs("root", samples, probs);
		int n = samples.size();
		for (int i = 0; i < n; i++) {
			outputs.add(testAllPath("root", i, probs));
		}
		return outputs;
	}
	
	private OutputStructure testAllPath(String label, int idx, Map<String, Vector<Vector<Double>>> probs) {
		PriorityQueue<OutputStructure> outs = new PriorityQueue<OutputStructure>();
		
		TreeSet<String> sublabelSet= sons.get(label);
		if (sublabelSet == null) {
			return new OutputStructure(label, 1.0);
		}
		
		Vector<String> subLabels = new Vector<String>(sublabelSet);
		int m = subLabels.size();
		
		String stucker = loadModel(stuckers.get(label));
		double unstuckProb = 1.0;
		
		if (stucker != null) {
			double pr = getProbs(stucker, idx, probs).get(1);
			outs.add(new OutputStructure(label, pr));
			unstuckProb = 1.0 - pr;
		}
		
		if (sons.get(label).size() == 1) {
			OutputStructure subout = testAllPath(sons.get(label).first(), idx, probs);
			outs.add(new OutputStructure(subout.getLabel(), unstuckProb * 1.0 * subout.getP()));
		} else {	
			String selecter = loadModel(selecters.get(label));
			Vector<Double> subprobs = getProbs(selecter, idx, probs);
			
			for (int j = 0; j < m; j++) {
				String subLabel = subLabels.get(j);
				OutputStructure subout = testAllPath(subLabel, idx, probs);
				outs.add(new OutputStructure(subout.getLabel(), unstuckProb * subprobs.get(j) * subout.getP()));
			}
		}
	
		return outs.peek();
	}
	
	private Vector<Double> getProbs(String modelName, int idx, Map<String, Vector<Vector<Double>>> probs) {
		return probs.get(modelName).get(idx);
	}
	
	private void calProbs(String label, Vector<Sample> samples, Map<String, Vector<Vector<Double>>> probs) throws Exception {
		TreeSet<String> sublabelSet= sons.get(label);
		if (sublabelSet == null) {
			return;
		}
		int n = samples.size();
		Vector<String> subLabels = new Vector<String>(sublabelSet);
		int m = subLabels.size();
		
		String stucker = loadModel(stuckers.get(label));
		if (stucker != null) {
			Vector<SparseVector> vectors = new Vector<SparseVector>();
			TextToSparseVectorConverter converter = stuckConverters.get(label);
			for (int i = 0; i < n; i++) {
				Sample sample = samples.get(i);
				Sample vecSample = converter.convert(sample);
				vectors.add((SparseVector)vecSample.getProperty());
			}
			Vector<Vector<Double>> prs = VWUtil.multiClassTest(vectors, stucker);
			probs.put(stucker, prs);
		}
		
		if (sons.get(label).size() == 1) {
			calProbs(sons.get(label).first(), samples, probs);;
		} else {

			String selecter = loadModel(selecters.get(label));
			TextToSparseVectorConverter converter = selectConverters.get(label);
			
			Vector<SparseVector> vectors = new Vector<SparseVector>();
			for (int i = 0; i < n; i++) {
				Sample sample = samples.get(i);
				Sample vecSample = converter.convert(sample);
				vectors.add((SparseVector)vecSample.getProperty());
			}
			Vector<Vector<Double>> prs = VWUtil.multiClassTest(vectors, selecter);
			probs.put(selecter, prs);
	
			for (int j = 0; j < m; j++) {
				String subLabel = subLabels.get(j);
				calProbs(subLabel, samples, probs);
			}
		}
	}
}
