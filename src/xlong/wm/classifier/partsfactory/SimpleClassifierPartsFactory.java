package xlong.wm.classifier.partsfactory;

import xlong.wm.sample.converter.TextToSparseVectorConverter;

public abstract class SimpleClassifierPartsFactory implements ClassifierPartsFactory{

	private static final long serialVersionUID = 5141188704903237533L;

	public TextToSparseVectorConverter getNewConverter() {
		return null;
	}
	
	public weka.classifiers.Classifier getNewWekaClassifier() {
		return null;
	}
}
