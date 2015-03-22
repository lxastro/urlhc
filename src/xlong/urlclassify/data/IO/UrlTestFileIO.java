package xlong.urlclassify.data.IO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Vector;
import java.util.stream.Stream;

import xlong.urlclassify.data.processer.UrlNormalizer;
import xlong.wm.sample.Sample;
import xlong.wm.sample.Text;

public class UrlTestFileIO {
	
	public static Vector<Sample> load(String filePath) throws Exception {
		if (Files.isDirectory(Paths.get(filePath))) {
			return loadDir(filePath);
		}
		else {
			return loadFile(filePath);
		}
	}
		
	private static Vector<Sample> loadDir(String filePath) throws Exception {
		Vector<Sample> samples = new Vector<>();
		Stream<Path> sp = Files.list(Paths.get(filePath));
		Iterator<Path> it = sp.iterator();
		while (it.hasNext()) {
			samples.addAll(loadFile(it.next().toString()));
		}
		sp.close();
		return samples;
	}
	private static Vector<Sample> loadFile(String filePath) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(filePath));
		Vector<Sample> samples = new Vector<>();
		String url;
		while ((url = in.readLine()) != null) {
			url = UrlNormalizer.normalize(url);
			samples.add(new Sample(url, new Text(url)));
		}
		in.close();
		return samples;
	}
	
	public static void main(String[] args) throws Exception {
		String filePath = "E:/longx/data/URLs/URLs_10.txt";
		Vector<Sample> samples = load(filePath);
		System.out.println(samples.size());
	}
}
