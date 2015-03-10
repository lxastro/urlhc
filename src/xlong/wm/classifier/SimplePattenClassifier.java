package xlong.wm.classifier;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;



import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;

public class SimplePattenClassifier implements SingleLabelClassifier {

	private int MINNUM;
	
	HashMap<String, Vector<Sample>> hostMap;
	
	public SimplePattenClassifier(int minnum) {
		hostMap = new HashMap<String, Vector<Sample>>();
		MINNUM = minnum;
	}
	
	@Override
	public void train(Composite composite) throws Exception {
		Collection<Sample> samples = composite.getAllSamples();
		// build hostMap
		System.out.println("Build hostMap");
		for (Sample sample:samples) {
			String host = getHost(sample.getURL());
			if (!hostMap.containsKey(host)) {
				hostMap.put(host, new Vector<Sample>());
			}
			hostMap.get(host).add(sample);
		}
		// sort
		System.out.println("Sort");
		for (Vector<Sample> vec:hostMap.values()) {
			Collections.sort(vec, new URLcmp());
		}
		System.out.println("hostMap.size: " + hostMap.size());
	}
	
	private String getHost(String url) throws Exception {
		String rest = url.split("://")[1];
		return rest.split("/")[0];
	}
	
	private String removeHost(String url) throws Exception {
		String rest = url.split("://")[1];
		int idx = rest.indexOf("/");
		if (idx == -1) {
			return "";
		}
		return rest.substring(idx+1);
	}
	
	private int cntPrefix(String s1, String s2) {
		int n1 = s1.length();
		int n2 = s2.length();
		int m = n1<n2 ? n1 : n2;
		for (int i = 0; i < m; i++) {
			if (s1.charAt(i) != s2.charAt(i)) {
				return i;
			}
		}
		return m;
	}
	

	@Override
	public OutputStructure test(Sample sample) throws Exception {
		String url = sample.getURL();
		Vector<Sample> samples = hostMap.get(getHost(url));
		if (samples == null) {
			return new OutputStructure(null, 1.0);
		}
		
		int n = samples.size();
		if (n < MINNUM) {
			return new OutputStructure(null, 1.0);
		}
		
		String res = removeHost(url);
		int[] pre = new int[n];
		int pmax = 0;
		for (int i = 0; i < n; i++) {
			Sample s = samples.get(i);
			pre[i] = cntPrefix(res, removeHost(s.getURL()));
			if (pre[i] > pre[pmax]) {
				pmax = i;
			}
		}
		
		int s = pmax - 1;
		while (s >= 0) {
			if (samples.get(s).getLabels().equals(samples.get(pmax).getLabels())) {
				s--;
			} else {
				break;
			}
		}
		
		int t = pmax + 1;
		while (t < n) {
			if (samples.get(t).getLabels().equals(samples.get(pmax).getLabels())) {
				t++;
			} else {
				break;
			}
		}
		
		// mar
		int mar = 0;
		
		if (pre[s+1] + mar > pre[t-1]) {
			if (s>=0 && pre[s+1]-pre[s]<mar){
				return new OutputStructure(null, 1.0);
			}
		}
		if (pre[s+1] < pre[t-1] + mar) {
			if (t<n && pre[t-1]-pre[t]<mar){
				return new OutputStructure(null, 1.0);
			}
		}
		
		if (t-s-1 < MINNUM) {
			return new OutputStructure(null, 1.0);
		}
	
		return new OutputStructure(new String(samples.get(pmax).getLabel().getText()), 1.0);
	}

	
	@Override
	public Vector<OutputStructure> test(Vector<Sample> samples)
			throws Exception {
		Vector<OutputStructure> vector = new Vector<OutputStructure>();
		int idx = 0;
		for (Sample sample:samples) {
			idx ++;
			if (idx % 10000 == 0) {
				//System.out.println(idx);
			}
			vector.add(test(sample));
		}
		return vector;
	}
	
	class URLcmp implements Comparator<Sample> {
		@Override
		public int compare(Sample s1, Sample s2) {
			String u1 = s1.getURL();
			String u2 = s2.getURL();
			return u1.compareTo(u2);
		}
		
	}

}
