package xlong.wm.classifier.partsfactory;

import xlong.wm.sample.converter.TextToSparseVectorConverter;

public abstract class SimpleClassifierPartsFactory implements ClassifierPartsFactory{

	private static final long serialVersionUID = 5141188704903237533L;

	@Override
	public TextToSparseVectorConverter getNewConverter() {
		return null;
	}
	
	@Override
	public weka.classifiers.Classifier getNewWekaClassifier() {
		return null;
	}
	
	@Override
	public String getTestArgs() {
		return "";
	}

	@Override
	public String getTrainArgs() {
		return "";
	}

	@Override
	public String getTempDir() {
		return "temp/";
	}
	
	
}
