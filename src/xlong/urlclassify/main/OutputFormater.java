package xlong.urlclassify.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.TreeMap;

import xlong.util.MyWriter;

public class OutputFormater {
	
	static class Structure implements Comparable<Structure>{
		public String url;
		public String predict;
		public String actual;
		public double p;
		public Structure(String url, String predict, String actual, double p) {
			this.actual = actual;
			this.url = url;
			this.predict = predict;
			this.p = p;
		}
		
		@Override
		public int compareTo(Structure s2) {
			if (this.predict.equals(this.actual)) {
				if (!s2.predict.equals(s2.actual)) {
					return -1;
				}
			} else {
				if (s2.predict.equals(s2.actual)) {
					return 1;
				}
			}
			
			if (new Double(this.p).compareTo(new Double(s2.p)) == 0) {
				return this.url.compareTo(s2.url);
			} else {
				return - new Double(this.p).compareTo(new Double(s2.p));
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
		if (args.length != 1) {
			throw new Exception("Error! Args should be: resultDir");
		}
		
		String resultDir = args[0]; //"result";
		Map<String, TreeSet<Structure>> map = new TreeMap<String, TreeSet<Structure>>();
		BufferedReader in = new BufferedReader(new FileReader(resultDir + "/output.txt"));
		String line;
		String line2;
		while ((line = in.readLine()) != null) {
			line2 = in.readLine();
			String url = line;
			String[] ss = line2.split(" ");
			String predict = ss[0];
			String actual = ss[1];
			double p = Double.parseDouble(ss[2]);
			if (!map.containsKey(predict)) {
				map.put(predict, new TreeSet<Structure>());
			}
			map.get(predict).add(new Structure(url, predict, actual, p));
		}
		in.close();
		
		MyWriter.setFile(resultDir + "/outputformate.txt", false);
		for (Entry<String, TreeSet<Structure>> en:map.entrySet()) {
			String predict = en.getKey();
			TreeSet<Structure> stuctures = en.getValue();
			if (stuctures.size()<50) {
				continue;
			}
			int base = stuctures.size()/50;
			
			System.out.println(stuctures.size());
			int cnt1 = 0;
			int cnt2 = 0;
			for (Structure stucture:stuctures) {
				if (predict.equals(stucture.actual)) {
					cnt1++;
				} else {
					cnt2++;
				}
				if ((cnt1 + cnt2)%base == 0) {
					MyWriter.writeln(predict + " " + stucture.actual + " " + stucture.url);
				}
			}
			MyWriter.writeln("Number: " + (cnt1+cnt2) + " Accuracy: " + 1.0*cnt1/(cnt1+cnt2) + "\n");
		}
		MyWriter.close();
		

	}
}
