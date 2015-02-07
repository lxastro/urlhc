package xlong.urlclassify.main;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import xlong.nlp.parser.BigramSegmentParser;
import xlong.nlp.parser.Parser;
import xlong.nlp.parser.TokenizeParser;
import xlong.nlp.parser.UnionParser;
import xlong.nlp.tokenizer.SingleWordTokenizer;
import xlong.urlclassify.data.IO.UrlMapIO;
import xlong.wm.ontology.OntologyTree;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Labels;
import xlong.wm.sample.Sample;
import xlong.wm.sample.Text;

public class GetParsedData {

	public static void main(String[] args) throws Exception{
		
		// ----------------------------Data process---------------------------------
		// Get properties.
		
		if (args.length != 2) {
			throw new Exception("Error! Args should be: ontologyFile resultDir");
		}
		
		String ontologyFile = args[0]; //"E:\\longx\\data\\dbpedia_2014.owl";
		String resultDir = args[1]; //"result";
		
		String UrlMapFile = resultDir + "/UrlMap.txt";
		String FlatParsedFile = resultDir + "/flatParsed.txt";
		String TreeParsedFile = resultDir + "/treeParsed.txt";
		
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		OntologyTree flatTree = tree.toFlatTree();
		BigramSegmentParser.setWeigth(1);
		Parser segParser = new TokenizeParser(null, new SingleWordTokenizer(), new  BigramSegmentParser(null));
		Parser simpleParser = new TokenizeParser(null, new SingleWordTokenizer());
		//Parser stemParser = new TokenizeParser(null, new SingleWordTokenizer(), new  BigramSegmentParser(null, new SnowballStemParser(null)));
		Parser parser = new UnionParser(null, segParser, simpleParser);
		Parser urlParser = parser;
		

		Composite flatComposite = new Composite(flatTree);
		
		HashMap<String, TreeSet<String>> urlMap = UrlMapIO.read(UrlMapFile);
		System.out.println(urlMap.size());
		
		
		int cnt = 0;
		for (Entry<String, TreeSet<String>> en:urlMap.entrySet()) {
			cnt ++;
			if (cnt % 100000 == 0) {
				System.out.println(cnt);
			}
			Sample sample = new Sample(en.getKey(), new Text(urlParser.parse(en.getKey())), Labels.getLabels(en.getValue()));
			flatComposite.addSample(sample);
		}
		System.out.println(flatComposite.countSample());
		flatComposite.cutBranch(1);
		System.out.println(flatComposite.getComposites().size());
		flatComposite.save(FlatParsedFile);
		
		Composite treeComposite = new Composite(tree);
		cnt = 0;
		for (Entry<String, TreeSet<String>> en:urlMap.entrySet()) {
			cnt ++;
			if (cnt % 100000 == 0) {
				System.out.println(cnt);
			}
			String label = en.getValue().first(); 
			Sample sample = new Sample(en.getKey(), new Text(urlParser.parse(en.getKey())), Labels.getLabels(tree.getPath(label)));
			treeComposite.addSample(sample);
		}
		System.out.println(treeComposite.countSample());
		treeComposite.cutBranch(1);
		System.out.println(flatComposite.getComposites().size());
		treeComposite.save(TreeParsedFile);
		
	}
}
