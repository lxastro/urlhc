/**
 * Project : Classify URLs
 */
package xlong.urlclassify.data.IO;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import xlong.urlclassify.data.processer.StringArrayProcesser;
import xlong.util.MyWriter;

/**
 * Class for reading Ntriples.
 * 
 * @author Xiang Long (longx13@mails.tinghua.edu.cn)
 */
public class NTripleReader {
	private static final String mySpliter = " |-| ";
	/** The NxParser used to parse file */
	protected NxParser nxp;
	/** Counts of triples */
	protected int cnt;
	/** Output logs or not. */
	protected static boolean outputLogs = true;
	
	private StringArrayProcesser stringArrayProcesser;
	
	private static final int MAXLINE = 100000000;
	
	/**
	 * Constructor
	 * 
	 * @param filePath
	 *            the path of the file to read.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public NTripleReader(String filePath, StringArrayProcesser stringArrayProcesser) throws IOException {
		nxp = new NxParser(new FileInputStream(filePath), false);
		cnt = 0;
		this.stringArrayProcesser = stringArrayProcesser;
	}
	
	public NTripleReader(String filePath) throws IOException {
		this(filePath, null);
	}	

	/**
	 * Set output logs or not.
	 * 
	 * @param outputLogs
	 */
	public static void setOutputLogs(boolean outputLogsPar) {
		outputLogs = outputLogsPar;
	}

	/**
	 * Get next triple
	 * 
	 * @return next triple
	 */
	protected String[] getNextTriple() {
		while (nxp.hasNext()) {
			Node[] ns = nxp.next();
			if (ns.length == 3) {
				cnt++;
				return stringArrayProcesser.process(Nodes2Strings(ns));
			}
		}
		return null;
	}
	
	private String[] Nodes2Strings(Node[] ns) {
		int l = ns.length;
		String[] ss = new String[l];
		for (int i = 0; i < l; i++) {
			ss[i] = ns[i].toString();
		}
		return ss;
	}

	/**
	 * Read Ntriples and write result into a file.
	 * 
	 * @param outFile
	 *            the file output reading result.
	 * @param maxNum
	 *            the max number of triples to read.
	 */
	public void readAll(String outFile, int maxNum) {
		if (!MyWriter.setFile(outFile, false)) {
			System.err.println("MyWriter setFile fail.");
			System.exit(0);
		}
		int cnt = 0;
		String[] ns;
		while ((ns = getNextTriple()) != null && cnt <= maxNum) {
			MyWriter.writeln(ns[0] + mySpliter + ns[1]);
			cnt ++;
			if (cnt % 1000000 == 0) System.out.println(cnt);
		}
		if (outputLogs) {
			System.out.println("Read lines: " + Math.min(cnt, maxNum));
		}

		MyWriter.close();
	}
	
	public ArrayList<String[]> readAll(int maxNum) {
		int cnt = 0;
		String[] ns;
		ArrayList<String[]> triples = new ArrayList<String[]>();
		while ((ns = getNextTriple()) != null && cnt <= maxNum) {
			triples.add(ns);
			cnt ++;
			if (cnt % 1000000 == 0) System.out.println(cnt);
		}
		if (outputLogs) {
			System.out.println("Read lines: " + Math.min(cnt, maxNum));
		}
		return triples;
	}
	
	public void readAll(String outFile){
		readAll(outFile, MAXLINE);
	}
	
	public ArrayList<String[]> readAll() {
		return readAll(MAXLINE);
	}

}
