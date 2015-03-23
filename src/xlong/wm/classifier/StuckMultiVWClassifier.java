package xlong.wm.classifier;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

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
	private String testType = "Pachinko";
	
	private int fileID = 0;
	private Vector<Integer> cntClass;
	
	private static String inputDir = "result/MVW/input/";
	private static String modelDir = "result/MVW/model/";
	private static String cacheDir = "result/MVW/cache/";
	private static String inputExt = ".classifier";
	private static String modelExt = ".model";
	private static String cacheExt = ".cache";
	
	static {
		try {
			Files.createDirectories(Paths.get(inputDir));
			Files.createDirectories(Paths.get(modelDir));
			Files.createDirectories(Paths.get(cacheDir));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public StuckMultiVWClassifier(ClassifierPartsFactory factory, String testType) {
		selecters = new TreeMap<String,  String>();
		stuckers = new TreeMap<String,  String>();
		selectConverters = new TreeMap<String, TextToSparseVectorConverter>();
		stuckConverters = new TreeMap<String, TextToSparseVectorConverter>();
		sons = new TreeMap<String, TreeSet<String>>();
		cntClass = new Vector<Integer>();
		this.factory = factory;
		this.testType = testType;
	}
	
	public StuckMultiVWClassifier(StuckMultiVWClassifier classifiers) {
		selecters = classifiers.selecters;
		stuckers = classifiers.stuckers;
		selectConverters = classifiers.selectConverters;
		stuckConverters = classifiers.stuckConverters;
		sons = classifiers.sons;
		factory = classifiers.factory;
		testType = classifiers.testType;
		cntClass = classifiers.cntClass;
	}
	
	private TextToSparseVectorConverter getNewConverter() {
		return factory.getNewConverter();
	}

	private String newFilePath() {
		return inputDir + String.valueOf(fileID++) + inputExt;
	}
	
	@Override
	public void train(Composite composite) throws Exception {
		getInputFile(composite);
		getModel();
		deleteDir(cacheDir);
	}
	
	private static void deleteDir(String path) throws Exception{
		Files.walkFileTree(Paths.get(path),
				new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult postVisitDirectory(Path dir,
							IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file,
							BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}
				});
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
			line += " --oaa " + (cntClass.get(i));
			line += " --passes 35 --l1 1e-8 -f " + (modelDir + String.valueOf(i) + modelExt);
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
			
			stuckers.put(label, String.valueOf(fileID - 1));
			stuckConverters.put(label, converter);
			
		}
		
		TextToSparseVectorConverter converter = getNewConverter();
		
		Vector<String> sublabels = new Vector<String>();
		//System.out.println("build selecter dictionary...");
		for (Composite subcomp:composite.getComposites()) {
			sublabels.add(subcomp.getLabel().getText());
			converter.buildDictionary(subcomp);
			//System.out.println(subcomp.getLabel().getText());
		}
		sons.put(label, new TreeSet<String>(sublabels));
		
		System.out.println("n sublabels: " + sublabels.size());
		
		if (sublabels.size() == 1) {
			return;
		}
			
		converter.determineDictionary();
		//System.out.println(converter.getDictionary().size());
		
	
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
		
		selecters.put(label , String.valueOf(fileID - 1));
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
		switch (testType) {
		case "AllPath":
			outputs = testAllPath(samples);
			break;
//		case "BeamSearch":
//			outputs = testBeamSearch(samples);
//			break;
		default:
			outputs = null;
//			outputs = testPachinko(samples);
			break;
		}
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
	
//
//	public Vector<OutputStructure> testPachinko(Vector<Sample> samples) throws Exception {
//		Vector<Double> ps = new Vector<Double>();
//		int n = samples.size();
//		for (int i = 0; i < n; i++) {
//			ps.add(1.0);
//		}
//		return testPachinko("root", ps, samples);
//	}
//	
//
//	public Vector<OutputStructure> testPachinko(String label, Vector<Double> ps, Vector<Sample> samples) throws Exception {
//		Vector<OutputStructure> results = new Vector<OutputStructure>();
//		TreeSet<String> sublabelSet= sons.get(label);
//		
//		int n = samples.size();
//		
//		if (sublabelSet == null) {
//			for (int i = 0; i < n; i++) {
//				results.add(new OutputStructure(label, ps.get(i)));
//			}
//			return results;
//		}
//		Vector<String> subLabels = new Vector<String>(sublabelSet);
//		
//		int m = subLabels.size();
//		double[] nextP = new double[n];
//		for (int i = 0 ; i < n; i++) {
//			nextP[i] = ps.get(i);
//		}
//		String[] nextLabel = new String[n];
//		double[][] probs = new double[n][m];
//		int[] cnts = new int[n];
//		
//		String stucker = loadModel(stuckers.get(label));
//		if (stucker != null) {
//			Vector<SparseVector> vectors = new Vector<SparseVector>();
//			TextToSparseVectorConverter converter = stuckConverters.get(label);
//			
//			for (int i = 0; i < n; i++) {
//				Sample sample = samples.get(i);	
//				Sample vecSample = converter.convert(sample);
//				vectors.add((SparseVector)vecSample.getProperty());
//			}
//			Vector<Vector<Double>> prs = VWUtil.multiClassTest(vectors, stucker);
//			for (int i = 0; i < n; i++) {
//				double pstuck = prs.get(i).get(1);
//				int classID = (int)(pstuck + 0.5);
//				if (classID == 1) {
//					nextP[i] = nextP[i] * pstuck;
//					nextLabel[i] = label;
//				} else {
//					nextP[i] = nextP[i] * (1.0-pstuck);
//					nextLabel[i] = null;
//				}
//			}
//		}
//		stucker = null;
//		
//		if (m == 1) {
//			
//		}
//
//		for (int j = 0; j < m; j++) {
//			String subLabel = subLabels.get(j);
//			String selecter = loadModel(selecters.get(join(label, subLabel)));
//			TextToSparseVectorConverter converter = selectConverters.get(join(label, subLabel));
//			
//			Vector<SparseVector> vectors = new Vector<SparseVector>();
//			for (int i = 0; i < n; i++) {
//				cnts[i] = 0;
//				if (nextLabel[i] == null) {
//					cnts[i] ++;
//					Sample sample = samples.get(i);
//					Sample vecSample = converter.convert(sample);
//					vectors.add((SparseVector)vecSample.getProperty());
//				}
//			}
//			Vector<Double> prs = VWUtil.testVectors(vectors, selecter);
//			int ii = 0;
//			for (int i = 0; i < n; i++) {
//				if (nextLabel[i] == null) {
//					probs[i][j] = prs.get(ii);
//					ii++;
//				}
//			}
//		}
//		
//		for (int i = 0; i < n; i++) {
//			if (nextLabel[i] == null) {
//				double maximum = -1;
//				double tot = 0;
//				for (int j = 0; j < m; j++) {
//					tot += probs[i][j];
//					if (probs[i][j] > maximum) {
//						nextLabel[i] = subLabels.get(j);
//						maximum = probs[i][j];
//					}
//				}
//				double p = 1.0/cnts[i];
//				if (tot > 1e-8) {
//					p = maximum/tot;
//				}
//				nextP[i] = nextP[i] * p;
//			}
//		}
//		
//		for (int j = 0; j < m; j++) {
//			String subLabel = subLabels.get(j);
//			Vector<Sample> partsSamples = new Vector<Sample>();
//			Vector<Double> partsP = new Vector<Double>();
//			for (int i = 0; i < n; i++) {
//				if (nextLabel[i] == subLabel) {
//					partsSamples.add(samples.get(i));
//					partsP.add(nextP[i]);
//				}
//			}
//			Vector<OutputStructure> partsRes = testPachinko(subLabel, partsP, partsSamples);
//			int idx = 0;
//			for (int i = 0; i < n; i++) {
//				if (nextLabel[i] == subLabel) {
//					nextLabel[i] = partsRes.get(idx).getLabel();
//					nextP[i] = partsRes.get(idx).getP();
//					idx++;
//				}
//			}			
//		}
//		for (int i = 0; i < n; i++) {
//			results.add(new OutputStructure(nextLabel[i], nextP[i]));
//		}
//		return results;
//	}
	
	private String loadModel(String fileID) {
		if (fileID == null) {
			return null;
		}
		return modelDir + fileID + modelExt;
	}
	
	private static String getModelName(int id) {
		return modelDir + "vw" + id + modelExt;
	}

	@Override
	public void save(int id) throws Exception {
		String fileName = getModelName(id);
		FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
	}

	public static StuckMultiVWClassifier load(int id) throws Exception {
		String fileName = getModelName(id);
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		StuckMultiVWClassifier classifier = (StuckMultiVWClassifier) ois.readObject();
		ois.close();
		return classifier;
	}
}
