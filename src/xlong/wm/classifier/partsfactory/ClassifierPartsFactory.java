package xlong.wm.classifier.partsfactory;

import xlong.wm.sample.converter.TextToSparseVectorConverter;

public interface ClassifierPartsFactory {
	public TextToSparseVectorConverter getNewConverter();
	
	public weka.classifiers.Classifier getNewClassifier();
}
