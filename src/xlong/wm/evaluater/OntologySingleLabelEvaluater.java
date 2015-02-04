package xlong.wm.evaluater;

import java.util.TreeSet;

import xlong.wm.classifier.SingleLabelClassifier;
import xlong.wm.ontology.OntologyTree;

public class OntologySingleLabelEvaluater extends SingleLabelEvaluater {

	OntologyTree tree;
	boolean cal;
	double hammingLoss;
	double precision;
	double recall;
	double f1;
	
	public OntologySingleLabelEvaluater(SingleLabelClassifier singleLabelClassifier, OntologyTree tree) {
		super(singleLabelClassifier);
		this.tree = tree;
		cal = false;
	}
	
	private void calculate() {
		cal = true;
		String[] labels = getLabelSet();
		int[][] cm = getConfusionMatrix();
		double ham = 0;
		double pre = 0;
		double rec = 0;
		double f1s = 0;
		for (int i = 0; i < labels.length; i++) {
			for (int j = 0; j < labels.length; j++) {
				if (cm[i][j] == 0) {
					continue;
				}
				TreeSet<String> actualSet = tree.getPath(labels[i]);
				TreeSet<String> predictSet = tree.getPath(labels[j]);
				int nt = actualSet.size() - 1;
				int np = predictSet.size() - 1;
				actualSet.retainAll(predictSet);
				int ns = actualSet.size() - 1;
				if (ns != 0) {
					ham += cm[i][j] * ((double)ns) / (nt + np - ns);
					pre += cm[i][j] * ((double)ns) / (np);
					rec += cm[i][j] * ((double)ns) / (nt);
					f1s += cm[i][j] * ((double)ns * 2) / (nt + np);
				}
				
			}
		}
		hammingLoss = ham/total;
		precision = pre/total;
		recall = rec/total;
		f1 = f1s/total;
	}
	
	public double getAverHammingLoss() {
		if (!cal) calculate();
		return hammingLoss;
	}
	
	public double getAverPrecision() {
		if (!cal) calculate();
		return precision;
	}
	
	public double getAverRecall() {
		if (!cal) calculate();
		return recall;
	}
	
	public double getAverF1() {
		if (!cal) calculate();
		return f1;
	}
	

}
