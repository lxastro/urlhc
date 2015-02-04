package xlong.wm.classifier;

import java.util.Vector;

import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;

public interface SingleLabelClassifier {
	public void train(Composite composite) throws Exception;
	public String test(Sample sample) throws Exception;
	public Vector<String> test(Vector<Sample> samples) throws Exception;
}
