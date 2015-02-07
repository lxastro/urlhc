package xlong.wm.evaluater;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Vector;

import xlong.wm.classifier.OutputStructure;
import xlong.wm.classifier.SingleLabelClassifier;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;

public abstract class Evaluater {
	SingleLabelClassifier singleLabelClassifier;
    int total;
	
	public Evaluater(SingleLabelClassifier singleLabelClassifier) {
		this.singleLabelClassifier = singleLabelClassifier;
	}
	
	public abstract void evaluate(Composite composite) throws Exception;
	
	public abstract double getAccuracy();
	
	public abstract int[][] getConfusionMatrix();
	
	public abstract String[] getLabelSet();
	
	public abstract void output(BufferedWriter out) throws IOException;
	
	public abstract Vector<String> getActuals();
	
	public abstract Vector<OutputStructure> getPredicts();
	
	public abstract Vector<Sample> getSamples();
	
	public int getTotal() {
		return total;
	}
}
