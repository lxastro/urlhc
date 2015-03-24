package xlong.util;

import java.util.HashMap;
import java.util.Map;

public class OptionsUtil {

	public static Map<String, String> parseOptions(String... options) {
		if (options.length == 1) {
			options = options[0].split(" ");
		}
		String option = null;
		Map<String, String> optionMap = new HashMap<String, String>();
		
		int i = 0;
		while (i < options.length) {
			String s = options[i].trim();
			if (!s.equals("")) {
				if (option == null) {
					option = s;
				} else {
					optionMap.put(option, s);
					option = null;
				}
			}
			i++;
		}
		
		return optionMap;
		
	}
	
	public static void main(String[] args) {
		Map<String, String> om = parseOptions(" -t 1 -s 2 -z 3 -x  sdfsf ");
		for (String k: om.keySet()) {
			System.out.println(k + " : " + om.get(k));
		}
		
		System.out.println("! next !");
		om = parseOptions("");
		for (String k: om.keySet()) {
			System.out.println(k + " : " + om.get(k));
		}
		
		System.out.println("! next !");
	}
	
}
