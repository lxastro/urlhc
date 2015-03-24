package xlong.wm.classifier;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import xlong.util.FileUtil;
import xlong.wm.classifier.partsfactory.SimpleClassifierPartsFactory;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;

public class CombineClassifier extends AbstractSingleLabelClassifier {
	
	private static final long serialVersionUID = -2154362118894218993L;

	private static String modelExt = ".combine";
	
	SingleLabelClassifier c1,c2;

	public CombineClassifier(SingleLabelClassifier c1, SingleLabelClassifier c2, String modelDir) {
		super(
			new SimpleClassifierPartsFactory() {
				private static final long serialVersionUID = 8824649711942306652L;
			},
			modelDir
		);
		this.c1 = c1;
		this.c2 = c2;
	}
	
	private void initDir() throws Exception {
		FileUtil.createDir(modelDir);
	}
	
	public SingleLabelClassifier getC1() {
		return c1;
	}
	
	public SingleLabelClassifier getC2() {
		return c2;
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

	public static CombineClassifier load(String modelDir) throws Exception {
		modelDir = FileUtil.addTralingSlash(modelDir);
		String fileName = getModelName(modelDir);
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		CombineClassifier classifier = (CombineClassifier) ois.readObject();
		ois.close();
		return classifier;
	}
	

	@Override
	public void train(Composite composite) throws Exception {
		initDir();
		System.out.println("train 1");
		c1.train(composite);
		System.out.println("train 2");
		c2.train(composite);
	}

	@Override
	public OutputStructure test(Sample sample) throws Exception {
		OutputStructure o1 = c1.test(sample);
		if (o1.getLabel() != null) {
			return o1;
		} else {
			return c2.test(sample);
		}
	}

	@Override
	public Vector<OutputStructure> test(Vector<Sample> samples)
			throws Exception {
		System.out.println("test 1");
		Vector<OutputStructure> vo1 = c1.test(samples);
		System.out.println("test 2");
		Vector<OutputStructure> vo2 = c2.test(samples);
		Vector<OutputStructure> vo = new Vector<OutputStructure>();
		int n = samples.size();
		for (int i = 0; i < n; i++) {
			if (vo1.get(i).getLabel() != null) {
				vo.add(vo1.get(i));
			} else {
				vo.add(vo2.get(i));
			}
		}
		return vo;
	}

}
