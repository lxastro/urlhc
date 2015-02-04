package xlong.urlclassify.data.IO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import xlong.util.MyWriter;

public class UrlMapIO {
	private static final String mySpliter = " |-| ";
	private static final String mySpliterReg = " \\|-\\| ";
	
	public static HashMap<String, TreeSet<String>> read(String filePath) throws IOException {
		HashMap<String, TreeSet<String>> urlMap = new HashMap<String, TreeSet<String>>();

		BufferedReader in = new BufferedReader(new FileReader(filePath));
		String line;
		while ((line = in.readLine()) != null) {
			if (line.length() > 0){
				String[] ss = line.split(mySpliterReg);
				TreeSet<String> types = new TreeSet<String>();
				for (int i = 1; i < ss.length; i++){
					types.add(ss[i]);
				}
				urlMap.put(ss[0],types);
			}
		}
		in.close();
		return urlMap;
	}
	
	public static void write(HashMap<String, TreeSet<String>> urlMap, String filePath) {
		MyWriter.setFile(filePath, false);
		for (Entry<String, TreeSet<String>> en:urlMap.entrySet()) {
			String url = en.getKey();
			TreeSet<String> types = en.getValue();
			MyWriter.write(url);
			for (String type:types) {
				MyWriter.write(mySpliter + type);
			}
			MyWriter.writeln("");
		}
		MyWriter.close();
	}
}
