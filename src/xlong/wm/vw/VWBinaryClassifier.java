package xlong.wm.vw;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Scanner;
import java.util.Vector;

import xlong.util.MyWriter;
import xlong.wm.sample.SparseVector;

public class VWBinaryClassifier {

	public static void createInputFile(double posWeight, Vector<String> labels, Vector<SparseVector> vectors, String fileName) {
		double negWeight = 1.0-posWeight;
		MyWriter.setFile(fileName, false);
		int n = labels.size();
		Vector<Integer> idxs = new Vector<>();
		for (int i = 0; i < n; i++) {
			idxs.add(i);
		}
		Collections.shuffle(idxs);
		for (int i = 0; i < n; i++) {
			if (labels.get(i) == "+1") {
				MyWriter.write(labels.get(idxs.get(i)) + " " + posWeight + " | ");
			} else {
				MyWriter.write(labels.get(idxs.get(i)) + " " + negWeight + " | ");
			}
			
			MyWriter.writeln(vectors.get(idxs.get(i)).toString());
		}
		MyWriter.close();
	}
	
	public static void runCommand(String command) throws Exception {
		System.out.println("Executing command: " + command);
		
        ProcessBuilder builder = new ProcessBuilder(command.split(" "));
        builder.redirectErrorStream(true);
        final Process p = builder.start();
        
        final InputStream inStream = p.getInputStream();       
        Thread readerTread = new Thread(new Runnable() {
           public void run() {
              InputStreamReader reader = new InputStreamReader(inStream);
              Scanner scan = new Scanner(reader);
              while (scan.hasNextLine()) {
                 System.out.println("-- " + scan.nextLine());
              }
              scan.close();
           }
        });
        readerTread.start();
        
        OutputStream outStream = p.getOutputStream();
        PrintWriter pWriter = new PrintWriter(outStream);
        pWriter.close();
        
        p.waitFor();
        readerTread.join();
        System.out.println("done");
	}
	
	public static void trainVectors(Vector<String> labels, Vector<SparseVector> vectors, String modelName) throws Exception {
		System.out.println("train " + modelName);
		String trainCommand = "vw --loss_function logistic -c --passes 10 -f " + modelName;
		System.out.println("command: " + trainCommand);
		
        ProcessBuilder builder = new ProcessBuilder(trainCommand.split(" "));
        builder.redirectErrorStream(true);
        final Process p = builder.start();
        
        final InputStream inStream = p.getInputStream();       
        Thread readerTread = new Thread(new Runnable() {
           public void run() {
              InputStreamReader reader = new InputStreamReader(inStream);
              Scanner scan = new Scanner(reader);
              while (scan.hasNextLine()) {
                 System.out.println("-- " + scan.nextLine());
              }
              scan.close();
           }
        });
        readerTread.start();
        
        OutputStream outStream = p.getOutputStream();
        PrintWriter pWriter = new PrintWriter(outStream);
		int n = labels.size();
		for (int i = 0; i < n; i++) {
			pWriter.print(labels.get(i) + " | ");
			pWriter.println(vectors.get(i).toString());
		}
        pWriter.close();
        
        p.waitFor();
        readerTread.join();
        System.out.println("done");	
	}
	
	private static double sigma(double x) {
		return 1.0/(1.0 + Math.exp(-x));
	}
	
	public static double testVector(SparseVector vector, String modelName) throws Exception {
		Vector<SparseVector> vectors = new Vector<>();
		vectors.add(vector);
		return testVectors(vectors, modelName).get(0);
	}
	
	public static Vector<Double> testVectors(Vector<SparseVector> vectors, String modelName) throws Exception {
		System.out.println("test " + modelName);
		String testCommand = "vw -i " + modelName + " -t -p /dev/stdout --quiet";
		System.out.println("command: " + testCommand);
		
        ProcessBuilder builder = new ProcessBuilder(testCommand.split(" "));
        builder.redirectErrorStream(true);
        final Process p = builder.start();
        
        final Vector<Double> result = new Vector<Double>();
        final InputStream inStream = p.getInputStream();       
        Thread readerTread = new Thread(new Runnable() {
           public void run() {
              InputStreamReader reader = new InputStreamReader(inStream);
              Scanner scan = new Scanner(reader);
              while (scan.hasNextLine()) {
            	 String line = scan.nextLine();
            	 //System.out.println("-- " + line);
                 result.add(sigma(Double.parseDouble(line)));
              }
              scan.close();
           }
        });
        readerTread.start();
        
        OutputStream outStream = p.getOutputStream();
        PrintWriter pWriter = new PrintWriter(outStream);
        for (SparseVector vector:vectors) {
        	pWriter.print("0 | ");
        	pWriter.println(vector.toString());
        }
        pWriter.close();
        
        p.waitFor();
        readerTread.join();
        System.out.println("done");	
        return result;
	}
	
	public static void main(String[] args) throws Exception {
		Vector<String> labels = new Vector<>();
		Vector<SparseVector> vectors = new Vector<>();
		labels.add("+1");
		vectors.add(new SparseVector("1 1 2"));
		labels.add("+1");
		vectors.add(new SparseVector("1 0 2"));
		labels.add("+1");
		vectors.add(new SparseVector("2 1 1 0 1"));
		labels.add("-1");
		vectors.add(new SparseVector("1 3 2"));
		labels.add("-1");
		vectors.add(new SparseVector("1 2 2"));
		labels.add("-1");
		vectors.add(new SparseVector("2 2 1 3 1"));
		
		trainVectors(labels, vectors, "test1.model");
		Vector<Double> result = testVectors(vectors, "test1.model");
		for (Double p:result) {
			System.out.println(p);
		}
		
	}
}
