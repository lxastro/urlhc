package xlong.wm.sample;

import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * 
 * @author longx
 *
 */
public class SparseVector implements Property {

	/** */
	private int[] indexs;
	/** */
	private double[] values;
	private int totalLength;
	/**
	 * 
	 * @param vector vector
	 */
	public SparseVector(final TreeMap<Integer, Double> vector, int totalLength) {
		int size = vector.size();
		indexs = new int[size];
		values = new double[size];
		int i = 0;
		for (Entry<Integer, Double> en:vector.entrySet()) {
			indexs[i] = en.getKey();
			values[i] = en.getValue();
			i++;
		}
		this.totalLength = totalLength;
	}
	
	/**
	 * @param line one line string
	 */
	public SparseVector(final String line) {
		String[] parts = line.split(" ");
		totalLength = Integer.parseInt(parts[0]);
		int size = (parts.length - 1) / 2;
		indexs = new int[size];
		values = new double[size];
		for (int i = 0; i < size; i++) {
			indexs[i] = Integer.parseInt(parts[2 * i + 1]);
			values[i] = Double.parseDouble(parts[2 * i + 2]);
		}
	}
	
	public final int size() {
		return totalLength;
	}
	
	/**
	 * @return indexs
	 */
	public final int[] getIndexs() {
		return indexs;
	}
	
	/**
	 * @return values
	 */
	public final double[] getValues() {
		return values;
	}
	
	@Override
	public final String toString() {
		String str = indexs[0] + ":" + values[0];
		for (int i = 1; i < indexs.length; i++) {
			str += " " + indexs[i] + ":" + values[i];
		}
		return str;
	}
	
	@Override
	public final String getOneLineString() {
		String str = "" + totalLength;
		for (int i = 0; i < indexs.length; i++) {
			str += " " + indexs[i] + " " + values[i];
		}
		return str;	
	}

}
