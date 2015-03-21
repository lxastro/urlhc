package xlong.wm.classifier;

import java.util.Vector;

import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;

public class CombineClassifier implements SingleLabelClassifier {
	SingleLabelClassifier c1,c2;

	public CombineClassifier(SingleLabelClassifier c1, SingleLabelClassifier c2) {
		this.c1 = c1;
		this.c2 = c2;
	}

	@Override
	public void train(Composite composite) throws Exception {
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