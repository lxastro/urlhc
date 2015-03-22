package xlong.urlclassify.main;

import java.util.Vector;

import xlong.urlclassify.data.IO.UrlTestFileIO;
import xlong.util.MyWriter;
import xlong.wm.sample.Composite;
import xlong.wm.sample.Sample;
import xlong.wm.sample.Texts;
import xlong.wm.classifier.OutputStructure;
import xlong.wm.classifier.SimplePattenClassifier;
import xlong.wm.classifier.SingleLabelClassifier;

public class SimplePattenClassify {

	public static void main(String[] args) throws Exception {
		
		if (args.length != 3) {
			throw new Exception("Error! Args should be: inputDir resultDir parsedFile");
		}
		String parsedFile = args[2]; 
		String resultDir = args[1];
		String inputDir = args[0];
		
		
		Composite treeComposite, train;
		treeComposite = new Composite(parsedFile, new Texts());
		
		System.out.println(treeComposite.countSample());
		System.out.println(treeComposite.getComposites().size());
		train = treeComposite;
		train.cutBranch(1);
		
		train.relabel();
		for (int minnum = 5; minnum <= 5; minnum*=2) {
			SingleLabelClassifier singleLabelClassifier = new SimplePattenClassifier(minnum);
			System.out.println("train " + minnum);
			singleLabelClassifier.train(train);
			singleLabelClassifier.save(1);
			singleLabelClassifier = SimplePattenClassifier.load(1);
			
			System.out.println("load");
			Vector<Sample> testSamples = UrlTestFileIO.load(inputDir);
			
			System.out.println("classify");
			Vector<OutputStructure> results = singleLabelClassifier.test(testSamples);
			
			System.out.println("check");
			MyWriter.setFile(resultDir + "/result", false);
			int n = results.size();
			int pass = 0;
			for (int i = 0; i < n; i++) {
				OutputStructure result = results.get(i);
				if (result.getLabel() == null) {
					pass ++;
				} else {
					MyWriter.write(testSamples.get(i).getURL());
					MyWriter.writeln("    " + result.getLabel());
				}
			}
			System.out.println("total: " + n);
			System.out.println("pass: " + pass);
			System.out.println("classify: " + (n-pass));
			
		}
	}

}
