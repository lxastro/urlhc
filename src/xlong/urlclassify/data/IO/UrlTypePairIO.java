package xlong.urlclassify.data.IO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import xlong.util.MyWriter;

public class UrlTypePairIO {
	private static final String mySpliter = " |-| ";
	private static final String mySpliterReg = " \\|-\\| ";
	private BufferedReader in;
	
	public UrlTypePairIO(String filePath) throws IOException{
		in = new BufferedReader(new FileReader(filePath));
	}
	
	public void close() throws IOException {
		in.close();
	}
	
	public String[] next() throws IOException {
		String line;
		while ((line = in.readLine()) != null){
			String[] pair = line.split(mySpliterReg);
			if (pair.length == 2) {
				return pair;
			}		
		}
		return null;
	}
	
	public static ArrayList<String[]> read(String filePath) throws IOException {
		ArrayList<String[]> pairs = new ArrayList<String[]>();
		BufferedReader in = new BufferedReader(new FileReader(filePath));
		String line;
		while ((line = in.readLine()) != null) {
			String[] pair = line.split(mySpliterReg);
			if (pair.length == 2) {
				pairs.add(pair);
			}
		}
		in.close();
		return pairs;
	}
	
	public static void write(ArrayList<String[]> pairs, String filePath) {
		MyWriter.setFile(filePath, false);
		for (String[] pair:pairs) {
			MyWriter.writeln(pair[0] + mySpliter +pair[1]);
		}
		MyWriter.close();
	}
}
