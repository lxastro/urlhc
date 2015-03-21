package xlong.urlclassify.main;

import java.util.Random;
import java.util.Vector;

import xlong.util.MyWriter;
import xlong.wm.evaluater.OntologySingleLabelEvaluater;
import xlong.wm.ontology.OntologyTree;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;
import xlong.wm.sample.Texts;
import xlong.wm.classifier.OutputStructure;
import xlong.wm.classifier.SimplePattenClassifier;
import xlong.wm.classifier.SingleLabelClassifier;

public class SimplePattenTest {

	public static void main(String[] args) throws Exception {
		
		if (args.length != 3) {
			throw new Exception("Error! Args should be: ontologyFile resultDir parsedFile");
		}
		String ontologyFile = args[0]; //"E:\\longx\\data\\dbpedia_2014.owl";
		String resultDir = args[1]; //"result";
		String parsedFile = args[2]; 
		
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		
		Composite treeComposite, train, test;
		treeComposite = new Composite(parsedFile, new Texts());
		
		System.out.println(treeComposite.countSample());
		System.out.println(treeComposite.getComposites().size());
		Vector<Composite> composites;
		
		//composites = treeComposite.split(new int[] {70, 30}, new Random(123));
		composites = treeComposite.split(new int[] {7, 3}, new Random(123));
		train = composites.get(0);
		train.cutBranch(1);
		System.out.println(train.countSample());
		train.save(resultDir + "/trainText");	
		test = composites.get(1);
		System.out.println(test.countSample());
		test.save(resultDir + "/testText");
		
		train = new Composite(resultDir + "/trainText", new Texts());
		test = new Composite(resultDir + "/testText", new Texts());
		
		train.relabel();
		for (int minnum = 5; minnum <= 5; minnum*=2) {
			SingleLabelClassifier singleLabelClassifier = new SimplePattenClassifier(minnum);
			System.out.println("train " + minnum);
			singleLabelClassifier.train(train);
			singleLabelClassifier.save(1);
			singleLabelClassifier = SimplePattenClassifier.load(1);
			
			OntologySingleLabelEvaluater evaluater = new OntologySingleLabelEvaluater(singleLabelClassifier, tree);
			System.out.println("test");
			evaluater.evaluate(test);	
	
			MyWriter.setFile(resultDir + "/evaluate.txt", false);
			MyWriter.writeln("accuracy: " + evaluater.getAccuracy());
			MyWriter.writeln("hamming loss: " + evaluater.getAverHammingLoss());
			MyWriter.writeln("precision: " + evaluater.getAverPrecision());
			MyWriter.writeln("recall: " + evaluater.getAverRecall());
			MyWriter.writeln("f1: " + evaluater.getAverF1());
			MyWriter.close();
			
			Vector<String> actuals = evaluater.getActuals();
			Vector<OutputStructure> predicts = evaluater.getPredicts();
			Vector<Sample> samples = evaluater.getSamples();
			int n = actuals.size();
			MyWriter.setFile(resultDir + "/output.txt", false);
			for (int i = 0; i < n; i++) {
				MyWriter.writeln(samples.get(i).getURL());
				MyWriter.writeln(predicts.get(i).getLabel() + " " + actuals.get(i) + " " + predicts.get(i).getP());
			}
			MyWriter.close();
	
			MyWriter.writeln("MINNUM: " + minnum);
			MyWriter.writeln("ignore rate: " + evaluater.getIgnoreRate());
			MyWriter.writeln("accuracy: " + evaluater.getAccuracy());
			MyWriter.writeln("hamming loss: " + evaluater.getAverHammingLoss());
			MyWriter.writeln("precision: " + evaluater.getAverPrecision());
			MyWriter.writeln("recall: " + evaluater.getAverRecall());
			MyWriter.writeln("f1: " + evaluater.getAverF1());
		}
	}

}
