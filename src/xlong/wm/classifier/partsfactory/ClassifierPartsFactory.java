package xlong.wm.classifier.partsfactory;

import java.io.Serializable;

import xlong.wm.sample.converter.TextToSparseVectorConverter;

public interface ClassifierPartsFactory extends Serializable{
	public TextToSparseVectorConverter getNewConverter();
	
	public weka.classifiers.Classifier getNewWekaClassifier();
}
