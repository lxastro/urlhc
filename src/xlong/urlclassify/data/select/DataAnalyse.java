package xlong.urlclassify.data.select;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import xlong.urlclassify.data.IO.UrlMapIO;

public class DataAnalyse {
	
	private static void countURL(HashMap<String, TreeSet<String>> urlMap) {
		TreeMap<String, Integer> countMap = new TreeMap<String, Integer>();
		for (Entry<String, TreeSet<String>> en:urlMap.entrySet()) {
			for (String type:en.getValue()) {
				if (!countMap.containsKey(type)) {
					countMap.put(type, 1);
				} else {
					countMap.put(type, countMap.get(type) + 1);
				}
			}
		}
		System.out.println(countMap.size());
		for (Entry<String, Integer> en:countMap.entrySet()) {
			System.out.println(en.getKey() + ": " + en.getValue());
		}
	}
	
	public static void main(String[] args) throws Exception{
		
		if (args.length != 1) {
			throw new Exception("Error! Args should be: resultDir");
		}

		String resultDir = args[0]; //"result";
		
		String UrlMapFile = resultDir + "/UrlMap.txt";
		
		HashMap<String, TreeSet<String>> urlMap = UrlMapIO.read(UrlMapFile);
		System.out.println(urlMap.size());
		
		countURL(urlMap);
	}
}
