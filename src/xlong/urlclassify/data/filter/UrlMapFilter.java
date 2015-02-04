package xlong.urlclassify.data.filter;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

public class UrlMapFilter {
	public static HashMap<String, TreeSet<String>> filterUrlMap(HashMap<String, TreeSet<String>> urlMap) {
		HashMap<String, TreeSet<String>> newMap = new HashMap<String, TreeSet<String>>();
		for (Entry<String, TreeSet<String>> en:urlMap.entrySet()) {
			if (en.getValue().size() == 1) {
				newMap.put(en.getKey(), en.getValue());
			}
		}
		return newMap;
	}
}
