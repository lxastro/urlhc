package xlong.wm.evaluater;

import java.io.BufferedWriter;
import java.io.IOException;

import xlong.wm.classifier.SingleLabelClassifier;
import xlong.wm.sample.Composite;

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
	
	public int getTotal() {
		return total;
	}
}
