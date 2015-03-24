package xlong.urlclassify.main;

public class ParserAndTest {
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			throw new Exception("Error! Args should be: ontologyFile resultDir");
		}
		String ontologyFile = args[0]; //"E:\\longx\\data\\dbpedia_2014.owl";
		String resultDir = args[1]; //"result";
		String treeParsedFile = resultDir + "/treeParsed.txt";
		
		
		GetParsedData2.main(new String[] {ontologyFile, resultDir});
		StuckMulitnomialTest.main(new String[] {ontologyFile, resultDir, treeParsedFile});
	}
}
