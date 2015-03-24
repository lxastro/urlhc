package xlong.wm.classifier;

import java.io.Serializable;
import java.util.Vector;

import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;

public interface SingleLabelClassifier extends Serializable {
	public void train(Composite composite) throws Exception;
	public OutputStructure test(Sample sample) throws Exception;
	public Vector<OutputStructure> test(Vector<Sample> samples) throws Exception;
	public void save() throws Exception;
}
