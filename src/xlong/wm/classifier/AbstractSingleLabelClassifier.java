package xlong.wm.classifier;

import xlong.util.FileUtil;
import xlong.wm.classifier.partsfactory.ClassifierPartsFactory;

public abstract class AbstractSingleLabelClassifier implements SingleLabelClassifier{

	private static final long serialVersionUID = 2791716689358989425L;
	
	protected String modelDir;
	protected String tempDir;
	protected String trainArgs;
	protected String testArgs;
	
	public AbstractSingleLabelClassifier(ClassifierPartsFactory factory, String modelDir) {
		this.modelDir = FileUtil.addTralingSlash(modelDir);
		this.tempDir = FileUtil.addTralingSlash(factory.getTempDir());

		this.trainArgs = factory.getTrainArgs();
		this.testArgs = factory.getTestArgs();
	}
	
	public void setTestArgs(String testArgs) {
		this.testArgs = testArgs;
	}

}
