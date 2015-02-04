package xlong.wm.classifier;

import java.util.PriorityQueue;

import xlong.wm.classifier.adapter.SparseVectorSampleToWekaInstanceAdapter;
import xlong.wm.classifier.partsfactory.ClassifierPartsFactory;
import xlong.wm.sample.Sample;
import xlong.wm.sample.converter.TextToSparseVectorConverter;

public class StuckBeamSearchMultiBaseClassifier extends StuckTopDownMultiBaseClassifier  {
	private final int beamWidth;
	
	public StuckBeamSearchMultiBaseClassifier(ClassifierPartsFactory factory, int beamWidth) {
		super(factory);
		this.beamWidth = beamWidth;
	}
	
	public  StuckBeamSearchMultiBaseClassifier(StuckTopDownMultiBaseClassifier classifier, int beamWidth) {
		super(classifier);
		this.beamWidth = beamWidth;
	}
	
	@Override
	public String test(Sample sample) throws Exception {
		return test("root", sample).peek().label;
	}
	
	private PriorityQueue<Pair> test(String label, Sample sample) throws Exception {
		PriorityQueue<Pair> pairs = new PriorityQueue<Pair>(new PairComp());
		if (sons.get(label) == null) {
			pairs.add(new Pair(label, 1.0));
			return pairs;
		}
		weka.classifiers.Classifier stucker = stuckers.get(label);
		double unstuckProb = 1.0;
		if (stucker != null) {
			TextToSparseVectorConverter converter = stuckConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = stuckAdapters.get(label);
			double[] probs = stucker.distributionForInstance(adapter.adaptSample(vecSample));
			String str = adapter.getDataSet().classAttribute().value(0);
			if (str.equals("pos")) {
				pairs.add(new Pair(label, probs[0]));
				unstuckProb = probs[1];
			} else {
				pairs.add(new Pair(label, probs[1]));
				unstuckProb = probs[0];				
			}
		}
		if (sons.get(label).size() == 1) {
			PriorityQueue<Pair> subPairs = test(sons.get(label).first(), sample);
			for (Pair pair:subPairs) {
				pairs.add(new Pair(pair.label, unstuckProb * 1.0 * pair.prob));
			}
		} else {
			weka.classifiers.Classifier selecter = selecters.get(label);
			TextToSparseVectorConverter converter = selectConverters.get(label);
			Sample vecSample = converter.convert(sample);
			SparseVectorSampleToWekaInstanceAdapter adapter = selectAdapters.get(label);
			double[] probs = selecter.distributionForInstance(adapter.adaptSample(vecSample));
			int n = probs.length;
			PriorityQueue<Pair> nowPairs = new PriorityQueue<Pair>(new PairComp());
			for (int i = 0; i < n; i++) {
				String subLabel = adapter.getDataSet().classAttribute().value(i);
				nowPairs.add(new Pair(subLabel, probs[i]));
			}
			for (int i = 0; i < beamWidth; i++) {
				if (!nowPairs.isEmpty()) {
					Pair exPair = nowPairs.poll();
					PriorityQueue<Pair> subPairs = test(exPair.label, sample);
					for (Pair pair:subPairs) {
						pairs.add(new Pair(pair.label, unstuckProb * exPair.prob * pair.prob));
					}
				} else {
					break;
				}
			}
		}
		if (pairs.size() <= beamWidth) {
			return pairs;
		}
		PriorityQueue<Pair> newPairs = new PriorityQueue<Pair>(new PairComp());
		for (int i = 0; i < beamWidth; i++) {
			if (!pairs.isEmpty()) {
				newPairs.add(pairs.poll());
			} else {
				break;
			}
		}
		return newPairs;
	}
	
}
