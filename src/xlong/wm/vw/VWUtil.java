package xlong.wm.vw;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Vector;

import xlong.util.MyWriter;
import xlong.wm.sample.SparseVector;

public class VWUtil {
	
	private static Vector<Double> getWeight(Vector<Integer> labels) {
		int n = labels.size();
		Map<Integer, Double> cnts = new HashMap<Integer, Double>();
		for (int i = 0; i < n; i++) {
			int label = labels.get(i);
			if (!cnts.containsKey(label)) {
				cnts.put(label, 0.0);
			}
			cnts.put(label, cnts.get(label) + 1);
		}
		
		Map<Integer, Double> weightMap = new HashMap<Integer, Double>();
		for (Entry<Integer, Double> en:cnts.entrySet()) {
			weightMap.put(en.getKey(), n/en.getValue());
		}
		
		Vector<Double> weights = new Vector<Double>();
		for (Integer label:labels) {
			weights.add(weightMap.get(label));
		}
		return weights;
	}
	
	private static boolean weightLimit = false;
	private static double weightLimitTimes = 10.0;
	
	public static void createMultiClassInputFile(Vector<Integer> labels, Vector<SparseVector> vectors, String fileName) {
		Vector<Double> weights = getWeight(labels);
		int n;
		
		if (weightLimit) {
			double min = Double.MAX_VALUE;
			for (Double weight:weights) {
				if (weight < min) {
					min = weight;
				}
			}
			double limit = min*weightLimitTimes;
			
			n = labels.size();
			for (int i = 0; i < n; i++) {
				double w = weights.get(i);
				if (w > limit) {
					int ndiv = (int)(w/limit) + 1;
					weights.set(i, w/ndiv);
					for (int j = 0; j < ndiv-1; j++) {
						weights.add(w/ndiv);
						labels.add(labels.get(i));
						vectors.add(vectors.get(i));
					}
				}
			}
		}
		
		MyWriter.setFile(fileName, false);
		n = labels.size();
		Vector<Integer> idxs = new Vector<>();
		for (int i = 0; i < n; i++) {
			idxs.add(i);
		}
		Collections.shuffle(idxs);
		for (int i = 0; i < n; i++) {
			MyWriter.write("" + (labels.get(idxs.get(i)) + 1) + " " + weights.get(idxs.get(i)) + " | ");
			MyWriter.writeln(vectors.get(idxs.get(i)).toString());
		}
		MyWriter.close();
	}
	

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
        	pWriter.print(" | ");
        	pWriter.println(vector.toString());
        }
        pWriter.close();
        
        p.waitFor();
        readerTread.join();
        System.out.println("done");	
        return result;
	}
	
	public static Vector<Vector<Double>> multiClassTest(Vector<SparseVector> vectors, String modelName) throws Exception {
		System.out.println("test " + modelName);
		String testCommand = "vw -i " + modelName + " -t -r /dev/stdout --quiet";
		System.out.println("command: " + testCommand);
		
        ProcessBuilder builder = new ProcessBuilder(testCommand.split(" "));
        builder.redirectErrorStream(true);
        final Process p = builder.start();
        
        final Vector<Vector<Double>> result = new Vector<Vector<Double>>();
        final InputStream inStream = p.getInputStream();       
        Thread readerTread = new Thread(new Runnable() {
           public void run() {
              InputStreamReader reader = new InputStreamReader(inStream);
              Scanner scan = new Scanner(reader);
              while (scan.hasNextLine()) {
            	 String line = scan.nextLine();
            	 //System.out.println("-- " + line);
                 result.add(parseLine(line));
              }
              scan.close();
           }
        });
        readerTread.start();
        
        OutputStream outStream = p.getOutputStream();
        PrintWriter pWriter = new PrintWriter(outStream);
        for (SparseVector vector:vectors) {
        	pWriter.print(" | ");
        	pWriter.println(vector.toString());
        }
        pWriter.close();
        
        p.waitFor();
        readerTread.join();
        System.out.println("done");	
        return result;
	}
	
	private static Vector<Double> parseLine(String line) {
		String[] parts = line.split(" ");
		int n = parts.length;
		double[] scores = new double[n+1];
		for (int i = 0; i < n; i++) {
			String[] pairs = parts[i].split(":");
			int idx = Integer.parseInt(pairs[0]);
			scores[idx] = Double.parseDouble(pairs[1]);
		}
		double tot = 0.0;
		for (int i = 1; i <= n; i++) {
			scores[i] = sigma(scores[i]);
			tot += scores[i];
		}
		Vector<Double> probs = new Vector<Double>();
		for (int i = 1; i <= n; i++) {
			probs.add(scores[i]/tot);
		}
		return probs;
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
