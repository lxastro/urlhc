package xlong.urlclassify.main;

public class ParserAndTest {
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			throw new Exception("Error! Args should be: ontologyFile resultDir GetParsedDataX");
		}
		String ontologyFile = args[0]; //"E:\\longx\\data\\dbpedia_2014.owl";
		String resultDir = args[1]; //"result";
		int parsedIdx = Integer.parseInt(args[2]);
		String treeParsedFile = resultDir + "/treeParsed.txt";
		
		switch (parsedIdx) {
		case 2:
			GetParsedData2.main(new String[] {ontologyFile, resultDir});
			break;
		case 3:
			GetParsedData3.main(new String[] {ontologyFile, resultDir});
			break;
		case 4:
			GetParsedData4.main(new String[] {ontologyFile, resultDir});
			break;
		case 5:
			GetParsedData5.main(new String[] {ontologyFile, resultDir});
			break;
		default:
			GetParsedData.main(new String[] {ontologyFile, resultDir});
			break;
		}
		
		CombineTest.main(new String[] {ontologyFile, resultDir, treeParsedFile});
	}
}
