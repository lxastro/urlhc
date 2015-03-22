package xlong.urlclassify.data.IO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Vector;
import java.util.stream.Stream;

import xlong.nlp.parser.BigramSegmentParser;
import xlong.nlp.parser.Parser;
import xlong.nlp.parser.TokenizeParser;
import xlong.nlp.parser.UnionParser;
import xlong.nlp.parser.UrlNormalizeParser;
import xlong.nlp.tokenizer.SingleWordTokenizer;
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
	
	private static Parser segParser = new TokenizeParser(null, new SingleWordTokenizer(), new  BigramSegmentParser(null));
	private static Parser simpleParser = new TokenizeParser(null, new SingleWordTokenizer());
	private static Parser parser = new UnionParser(new UrlNormalizeParser(null), segParser, simpleParser);
	
	private static Vector<Sample> loadFile(String filePath) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(filePath));
		Vector<Sample> samples = new Vector<>();
		String url;
		while ((url = in.readLine()) != null) {
			samples.add(new Sample(url, new Text(parser.parse(url))));
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
