package xlong.wm.classifier;

import java.util.Collection;

import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;

public interface MultiLabelClassifier {
	public void train(Composite composite) throws Exception;
	public Collection<String> test(Sample sample) throws Exception;
}
