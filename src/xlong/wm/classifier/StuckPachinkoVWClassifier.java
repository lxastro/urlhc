package xlong.wm.classifier;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import xlong.wm.classifier.partsfactory.ClassifierPartsFactory;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;
import xlong.wm.sample.SparseVector;
import xlong.wm.sample.converter.TextToSparseVectorConverter;
import xlong.wm.vw.VWBinaryClassifier;

public class StuckPachinkoVWClassifier extends AbstractSingleLabelClassifier  {
	private Map<String, String> selecters;
	private Map<String, String> stuckers;
	private Map<String, TextToSparseVectorConverter> selectConverters;
	private Map<String, TextToSparseVectorConverter> stuckConverters;
	private Map<String, TreeSet<String>> sons;
	protected ClassifierPartsFactory factory;
	
	private static String inputDir = "result/VW/input/";
	private static String modelDir = "result/VW/model/";
	private static String cacheDir = "result/VW/cache/";
	private static int fileID = 0;
	private static String inputName = ".classifier";
	private static String modelName = ".model";
	private static String cacheName = ".cache";
	//private static final String OPTION = "-M";
	
	static {
		try {
			Files.createDirectories(Paths.get(inputDir));
			Files.createDirectories(Paths.get(modelDir));
			Files.createDirectories(Paths.get(cacheDir));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public StuckPachinkoVWClassifier(ClassifierPartsFactory factory) {
		selecters = new TreeMap<String,  String>();
		stuckers = new TreeMap<String,  String>();
		selectConverters = new TreeMap<String, TextToSparseVectorConverter>();
		stuckConverters = new TreeMap<String, TextToSparseVectorConverter>();
		sons = new TreeMap<String, TreeSet<String>>();
		this.factory = factory;
	}
	
	public StuckPachinkoVWClassifier(StuckPachinkoVWClassifier classifiers) {
		selecters = classifiers.selecters;
		stuckers = classifiers.stuckers;
		selectConverters = classifiers.selectConverters;
		stuckConverters = classifiers.stuckConverters;
		sons = classifiers.sons;
		factory = classifiers.factory;
	}
	
	private TextToSparseVectorConverter getNewConverter() {
		return factory.getNewConverter();
	}
	
	private String join(String s1, String s2) {
		return s1 + "_" + s2;
	}
	
	private String newFilePath() {
		return inputDir + String.valueOf(fileID++) + inputName;
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
			String line = "vw -d " + (inputDir + String.valueOf(i) + inputName);
			line += " --loss_function logistic --cache_file " + (cacheDir + String.valueOf(i) + cacheName); 
			//line += " --l1 1e-8 -f " + (modelDir + String.valueOf(i) + modelName);
			line += " --passes 5 --l1 1e-8 -f " + (modelDir + String.valueOf(i) + modelName);
			VWBinaryClassifier.runCommand(line);
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
			VWBinaryClassifier.createInputFile(((double)cntNeg)/(cntNeg + cntPos), labels, vectors, classifierPath);
			
			stuckers.put(label, String.valueOf(fileID - 1));
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
			VWBinaryClassifier.createInputFile(((double)cntNeg)/(cntNeg + cntPos), labels, vectors, classifierPath);
			
			selecters.put(join(label, subcomp.getLabel().getText()), String.valueOf(fileID - 1));
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
		return test("root", 1.0, sample);
	}
	
	private OutputStructure test(String label, double p, Sample sample) throws Exception {
		TreeSet<String> subLabels = sons.get(label);
		if (subLabels == null) {
			return new OutputStructure(label, p);
		}
		
		String stucker = loadModel(stuckers.get(label));
		double pstuck = 0;
		if (stucker != null) {
			TextToSparseVectorConverter converter = stuckConverters.get(label);
			Sample vecSample = converter.convert(sample);

			pstuck = VWBinaryClassifier.testVector((SparseVector)vecSample.getProperty(), stucker);
			
			int classID = (int)(pstuck + 0.5);
			if (classID == 1) {
				return new OutputStructure(label, p*pstuck);
			}
		}
		double maximum = -1;
		String maxLabel = null;
		for (String subLabel:subLabels) {
			String selecter = loadModel(selecters.get(join(label, subLabel)));
			TextToSparseVectorConverter converter = selectConverters.get(join(label, subLabel));
			Sample vecSample = converter.convert(sample);

			double pselect = VWBinaryClassifier.testVector((SparseVector)vecSample.getProperty(), selecter);

			if (pselect > maximum) {
				maximum = pselect;
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
		
		String stucker = loadModel(stuckers.get(label));
		if (stucker != null) {
			Vector<SparseVector> vectors = new Vector<SparseVector>();
			for (int i = 0; i < n; i++) {
				Sample sample = samples.get(i);
				TextToSparseVectorConverter converter = stuckConverters.get(label);
				Sample vecSample = converter.convert(sample);
				vectors.add((SparseVector)vecSample.getProperty());
			}
			Vector<Double> prs = VWBinaryClassifier.testVectors(vectors, stucker);
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
			Vector<Double> prs = VWBinaryClassifier.testVectors(vectors, selecter);
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
	
	private String loadModel(String fileID) {
		if (fileID == null) {
			return null;
		}
		return modelDir + fileID + modelName;
	}
}