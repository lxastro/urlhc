package xlong.urlclassify.data.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import xlong.util.MyWriter;

public class TypeMapIO {
	
	public static HashMap<String, HashSet<String>> read(String filePath) throws IOException {
		HashMap<String, HashSet<String>> typeMap = new HashMap<String, HashSet<String>>();
		File dir = new File(filePath);
		for (File f:dir.listFiles()){
			BufferedReader in = new BufferedReader(new FileReader(f));
			String type = f.getName();
			HashSet<String> urls = new HashSet<String>();
			String url;
			while ((url = in.readLine()) != null) {
				if (url.length() > 0){
					urls.add(url);
				}
			}
			typeMap.put(type, urls);
			in.close();
		}
		return typeMap;
	}
	
	public static void write(HashMap<String, HashSet<String>> typeMap, String filePath) {
		for (Entry<String, HashSet<String>> en:typeMap.entrySet()) {
			String type = en.getKey();
			HashSet<String> urls = en.getValue();
			
			MyWriter.setFile(filePath + "/" + type, false);
			for (String url:urls) {
				MyWriter.writeln(url);
			}
			MyWriter.close();
		}
	}
}
