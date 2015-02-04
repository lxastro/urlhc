package xlong.wm.evaluater;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import xlong.wm.classifier.SingleLabelClassifier;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;

public class SingleLabelEvaluater extends Evaluater {
	
	class Count {
		int count;
		Count() {
			count = 0;
		}
		void addOne () {
			count++;
		}
	}
	
	protected TreeMap<String, TreeMap<String, Count>> cnt;
	protected TreeSet<String> labelSet;
	protected int total;
	
	private Vector<Sample> samples;
	private Vector<String> actuals;
	
	public SingleLabelEvaluater(SingleLabelClassifier singleLabelClassifier) {
		super(singleLabelClassifier);
		cnt = new TreeMap<String, TreeMap<String, Count>>();
		labelSet = new TreeSet<>();
		total = 0;
		samples = new Vector<>();
		actuals = new Vector<>();
	}
	
	private int getCnt(String actual, String predict) {
		if (!cnt.containsKey(actual)) {
			return 0;
		}
		if (!cnt.get(actual).containsKey(predict)) {
			return 0;
		}
		return cnt.get(actual).get(predict).count;
	}
	
	private void addCnt(String actual, String predict) {
		labelSet.add(actual);
		labelSet.add(predict);
		if (!cnt.containsKey(actual)) {
			cnt.put(actual, new TreeMap<String, Count>());
		}
		if (!cnt.get(actual).containsKey(predict)) {
			cnt.get(actual).put(predict, new Count());
		}
		cnt.get(actual).get(predict).addOne();
	}

//	private void evaluate(Sample sample, String actual) throws Exception {
//		String predict = singleLabelClassifier.test(sample);
//		addCnt(actual, predict);
//		total++;
//	}
	
	private void evaluate(Vector<Sample> samples, Vector<String> actual) throws Exception{
		Vector<String> predicts = singleLabelClassifier.test(samples);
		int n = predicts.size();
		for (int i = 0; i < n; i++) {
			addCnt(actual.get(i), predicts.get(i));
		}
		total += predicts.size();
	}
	
	private void addEvaluate(Sample sample, String actual) {
		samples.add(sample);
		actuals.add(actual);
	}
	
	private void addEvaluate(Composite composite) throws Exception {
		for (Sample sample:composite.getSamples()) {
			addEvaluate(sample, composite.getLabel().getText());
		}
		for (Composite sub:composite.getComposites()) {
			addEvaluate(sub);
		}	
	}
	
	private void evaluate() throws Exception {
		evaluate(samples, actuals);
		samples = new Vector<>();
		actuals = new Vector<>();
	}
	
	@Override
	public double getAccuracy() {
		int tp = 0;
		for (String label:labelSet) {
			tp += getCnt(label, label);
		}
		return ((double) tp) / total;
	}
	
	@Override
	public int[][] getConfusionMatrix() {
		String[] labels = getLabelSet();
		int[][] cm = new int[labels.length][labels.length];
		for (int i = 0; i < labels.length; i++) {
			for (int j = 0; j < labels.length; j++) {
				cm[i][j] = getCnt(labels[i], labels[j]);
			}
		}
		return cm;
	}

	@Override
	public String[] getLabelSet() {
		return labelSet.toArray(new String[0]);
	}
	
	@Override
	public void evaluate(Composite composite) throws Exception {
		addEvaluate(composite);
		evaluate();
	}

	@Override
	public void output(BufferedWriter out) throws IOException {
		out.write("Accuracy: " + getAccuracy() + "\n");
		out.write("Confusion Matrix: " + "\n");
		int[][] cm = getConfusionMatrix();
		String[] labels = getLabelSet();
		int[] t = new int[labels.length];
		out.write(String.format("%8s", ""));
		for (int j = 0; j < labels.length; j++) {
			out.write(String.format("%8d", j));
		}
		out.write("\n");
		for (int i = 0; i < labels.length; i++) {
			out.write(String.format("%8d", i));
			t[i] = 0;
			for (int j = 0; j < labels.length; j++) {
				out.write(String.format("%8d", cm[i][j]));
				t[i] += cm[i][j];
			}
			out.write("     total:");
			out.write(String.format("%8d", t[i]));
			out.write("\n");
		}
		for (int i = 0; i < labels.length; i++) {
			out.write(String.format("%4d", i));
			out.write(": ");
			out.write(" (total ");
			out.write(String.format("%8d", t[i]));
			out.write(") ");
			out.write(labels[i]);
			out.write("\n");
		}
	}



}
