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

public class StuckBinaryVWClassifier extends AbstractSingleLabelClassifier  {

	private static final long serialVersionUID = -1579838531910064576L;
	private Map<String, String> selecters;
	private Map<String, String> stuckers;
	private Map<String, TextToSparseVectorConverter> selectConverters;
	private Map<String, TextToSparseVectorConverter> stuckConverters;
	private Map<String, TreeSet<String>> sons;
	protected ClassifierPartsFactory factory;
	
	private String testType;
	
	private int npasses;
	
	private int fileID = 0;
	private String inputDir;
	private String cacheDir;

	private static String subInputDir = "input/";
	private static String subCacheDir = "cache/";
	private static String inputExt = ".classifier";
	private static String modelExt = ".bvw";
	private static String cacheExt = ".cache";

	public StuckBinaryVWClassifier(ClassifierPartsFactory factory, String modelDir) {
		super(factory, modelDir);
		selecters = new TreeMap<String,  String>();
		stuckers = new TreeMap<String,  String>();
		selectConverters = new TreeMap<String, TextToSparseVectorConverter>();
		stuckConverters = new TreeMap<String, TextToSparseVectorConverter>();
		sons = new TreeMap<String, TreeSet<String>>();
		this.factory = factory;
		getTrainOptions();
	}
	
	private void getTrainOptions() {
		Map<String, String> options = OptionsUtil.parseOptions(trainArgs);
		npasses = Integer.parseInt(options.get("-passes"));
	}
	
	private void getTestOptions() {
		Map<String, String> options = OptionsUtil.parseOptions(testArgs);
		testType = options.get("-testMethod");
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

	public static StuckBinaryVWClassifier load(String modelDir) throws Exception {
		modelDir = FileUtil.addTralingSlash(modelDir);
		String fileName = getModelName(modelDir);
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		StuckBinaryVWClassifier classifier = (StuckBinaryVWClassifier) ois.readObject();
		ois.close();
		return classifier;
	}
	
	private String join(String s1, String s2) {
		return s1 + "_" + s2;
	}
	
	@Override
	public void train(Composite composite) throws Exception {
		initDir();
		getInputFile(composite);
		getModel();
		finalizeDir();
	}
	
	private void getInputFile(Composite composite) throws Exception {
		getInputFile(composite.getLabel().getText(), composite);
		for (Composite subcomp:composite.getComposites()) {
			getInputFile(subcomp);
		}
	}
	
	private void getModel() throws Exception {
		for (int i = 0; i < fileID; i++) {
			String line = "vw -d " + (inputDir + String.valueOf(i) + inputExt);
			line += " --loss_function logistic --cache_file " + (cacheDir + String.valueOf(i) + cacheExt); 
			//line += " --l1 1e-8 -f " + (modelDir + String.valueOf(i) + modelName);
			line += " --passes " + npasses + " --l1 1e-8 -f " + (modelDir + String.valueOf(i) + modelExt);
			VWUtil.runCommand(line);
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
			//System.out.println("build stucker dictionary...");
			converter.buildDictionary(composite);
			//System.out.println("determine stucker dictionary...");
			converter.determineDictionary();
			//System.out.println(converter.getDictionary().size());
			//System.out.println("convert stucker...");
			Composite vecComposite = converter.convert(composite);
			
			Vector<String> labels = new Vector<String>();
			Vector<SparseVector> vectors = new Vector<SparseVector>();
			
			int cntPos = vecComposite.getSamples().size();
			int cntNeg = 0;
			for (Sample sample:vecComposite.getSamples()) {
				labels.add("+1");
				vectors.add((SparseVector) sample.getProperty());
			}
			for (Composite subcomp:vecComposite.getComposites()) {
				addAll(labels, vectors, subcomp, "-1");
				cntNeg += subcomp.countSample();
			}
			
			System.out.println("get input file stucker... instances: " + (cntNeg + cntPos) + " pos: " + cntPos);
			
			String classifierPath = newFilePath();
			VWUtil.createInputFile(((double)cntNeg)/(cntNeg + cntPos), labels, vectors, classifierPath);
			
			stuckers.put(label, lastFileID());
			stuckConverters.put(label, converter);
			
		}
		TreeSet<String> sublabels = new TreeSet<String>();
		
		TextToSparseVectorConverter converter = getNewConverter();
		//System.out.println("build selecter dictionary...");
		for (Composite subcompAll:composite.getComposites()) {
			converter.buildDictionary(subcompAll);
		}
		//System.out.println("determine selecter dictionary...");
		converter.determineDictionary();
		//System.out.println(converter.getDictionary().size());
		
		for (Composite subcomp:composite.getComposites()) {
			sublabels.add(subcomp.getLabel().getText());
			System.out.println("subcomp: " + join(label, subcomp.getLabel().getText()));
					
			Vector<String> labels = new Vector<String>();
			Vector<SparseVector> vectors = new Vector<SparseVector>();
			
			int cntNeg = 0;
			int cntPos = 0;
			for (Composite subcompOther:composite.getComposites()) {
				if (subcompOther.getLabel().compareTo(subcomp.getLabel()) != 0) {
					addAll(labels, vectors, converter.convert(subcompOther), "-1");
					cntNeg += subcompOther.countSample();
				} else {
					addAll(labels, vectors, converter.convert(subcompOther), "+1");
					cntPos += subcompOther.countSample();
				}
			}
			System.out.println("get input file selecter... instances: " + (cntNeg + cntPos) + " pos: " + cntPos);
			
			String classifierPath = newFilePath();
			VWUtil.createInputFile(((double)cntNeg)/(cntNeg + cntPos), labels, vectors, classifierPath);
			
			selecters.put(join(label, subcomp.getLabel().getText()), lastFileID());
			selectConverters.put(join(label, subcomp.getLabel().getText()), converter);	

		}
		sons.put(label, sublabels);
	}

	private void addAll(Vector<String> labels, Vector<SparseVector> vectors, Composite composite, String label) {
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
		getTestOptions();
		Vector<OutputStructure> outputs;
		switch (testType) {
		case "AllPath":
			outputs = testAllPath(samples);
			break;
		default:
			outputs = testPachinko(samples);
			break;
		}
		return outputs;
	}
	
	public Vector<OutputStructure> testAllPath(Vector<Sample> samples) throws Exception {
		Vector<OutputStructure> outputs = new Vector<OutputStructure>();
		Map<String, Vector<Double>> probs = new HashMap<String, Vector<Double>>();
		calProbs("root", samples, probs);
		int n = samples.size();
		for (int i = 0; i < n; i++) {
			outputs.add(testAllPath("root", i, probs));
		}
		return outputs;
	}
	
	private OutputStructure testAllPath(String label, int idx, Map<String, Vector<Double>> probs) {
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
			double pr = getProbs(stucker, idx, probs);
			outs.add(new OutputStructure(label, pr));
			unstuckProb = 1.0 - pr;
		}
		
		Double[] subprobs = new Double[m];
		Double tot = 0.0;
		
		for (int j = 0; j < m; j++) {
			String subLabel = subLabels.get(j);
			String selecter = loadModel(selecters.get(join(label, subLabel)));
			subprobs[j] = getProbs(selecter, idx, probs);
			tot += subprobs[j];
		}
		
		if (tot < 1e-8) {
			for (int j = 0; j < m; j++) {
				subprobs[j] = 1.0/m;
			}
		} else {
			for (int j = 0; j < m; j++) {
				subprobs[j] = subprobs[j]/tot;
			}
		}
		
		for (int j = 0; j < m; j++) {
			String subLabel = subLabels.get(j);
			OutputStructure subout = testAllPath(subLabel, idx, probs);
			outs.add(new OutputStructure(subout.getLabel(), unstuckProb * subprobs[j] * subout.getP()));
		}
		
		return outs.peek();
	}
	
	private Double getProbs(String modelName, int idx, Map<String, Vector<Double>> probs) {
		return probs.get(modelName).get(idx);
	}
	
	private void calProbs(String label, Vector<Sample> samples, Map<String, Vector<Double>> probs) throws Exception {
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
			Vector<Double> prs = VWUtil.testVectors(vectors, stucker);
			probs.put(stucker, prs);
		}

		for (int j = 0; j < m; j++) {
			String subLabel = subLabels.get(j);
			String selecter = loadModel(selecters.get(join(label, subLabel)));
			TextToSparseVectorConverter converter = selectConverters.get(join(label, subLabel));
			
			
			Vector<SparseVector> vectors = new Vector<SparseVector>();
			for (int i = 0; i < n; i++) {
				Sample sample = samples.get(i);
				Sample vecSample = converter.convert(sample);
				vectors.add((SparseVector)vecSample.getProperty());
			}
			Vector<Double> prs = VWUtil.testVectors(vectors, selecter);
			probs.put(selecter,prs);
			
			calProbs(subLabel, samples, probs);
		}
		
	}
	

	public Vector<OutputStructure> testPachinko(Vector<Sample> samples) throws Exception {
		Vector<Double> ps = new Vector<Double>();
		int n = samples.size();
		for (int i = 0; i < n; i++) {
			ps.add(1.0);
		}
		return testPachinko("root", ps, samples);
	}
	

	public Vector<OutputStructure> testPachinko(String label, Vector<Double> ps, Vector<Sample> samples) throws Exception {
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
		
		String stucker = loadModel(stuckers.get(label));
		if (stucker != null) {
			Vector<SparseVector> vectors = new Vector<SparseVector>();
			TextToSparseVectorConverter converter = stuckConverters.get(label);
			
			for (int i = 0; i < n; i++) {
				Sample sample = samples.get(i);	
				Sample vecSample = converter.convert(sample);
				vectors.add((SparseVector)vecSample.getProperty());
			}
			Vector<Double> prs = VWUtil.testVectors(vectors, stucker);
			for (int i = 0; i < n; i++) {
				double pstuck = prs.get(i);
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
			String selecter = loadModel(selecters.get(join(label, subLabel)));
			TextToSparseVectorConverter converter = selectConverters.get(join(label, subLabel));
			
			Vector<SparseVector> vectors = new Vector<SparseVector>();
			for (int i = 0; i < n; i++) {
				cnts[i] = 0;
				if (nextLabel[i] == null) {
					cnts[i] ++;
					Sample sample = samples.get(i);
					Sample vecSample = converter.convert(sample);
					vectors.add((SparseVector)vecSample.getProperty());
				}
			}
			Vector<Double> prs = VWUtil.testVectors(vectors, selecter);
			int ii = 0;
			for (int i = 0; i < n; i++) {
				if (nextLabel[i] == null) {
					probs[i][j] = prs.get(ii);
					ii++;
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
			Vector<OutputStructure> partsRes = testPachinko(subLabel, partsP, partsSamples);
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
