package xlong.wm.sample;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Factory to generate Label.
 */
public final class Labels {
	/** delimiter. */
	private static final String DELIMITER = " ";
	/** delimiter in regular expression. */
	private static final String DELIMITERREG = " ";
	/** To store label texts. Associate IDs with their text. */
	private static Vector<Label> labelVector;
	/** Map from a text to corresponding label.*/
	private static Map<String, Label> labels;
	
	static {
		labelVector = new Vector<Label>();
		labels  = new HashMap<String, Label>();
	}

	/** An implements of Label. */
	private static class LabelImpl implements Label {
		/** Text of the Label. */
		private final String text;
		/** ID which is ordered by first creating time. */
		private final int id;
		
		/**
		 * Get a new LabelImpl.
		 * @param text the text
		 * @param id the ID
		 */
		LabelImpl(final String text, final int id) {
			this.text = text;
			this.id = id;
		}
		
		@Override
		public int compareTo(final Label o) {
			return getID() - o.getID();
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public int getID() {
			return id;
		}
		
		@Override
		public String toString() {
			return text + "(" + id + ")";
		}
	}
	
	/**
	 * 
	 */
	private Labels() {
		
	}
	
	/**
	 * Get a Label has given text.
	 * @param text the text of label.
	 * @return the label.
	 */
	public static Label getLabel(final String text) {
		Label label = labels.get(text);
		if (label == null) {
			label = new LabelImpl(text, labelVector.size());
			labelVector.add(label);
			labels.put(text, label);
		}
		return label;
	}
	
	/**
	 * 
	 * @param id id
	 * @return the label
	 */
	public static Label getLabel(final int id) {
		return labelVector.get(id);
	}
	/**
	 * 
	 * @param texts texts
	 * @return labels
	 */
	public static TreeSet<Label> getLabels(final Collection<String> texts) {
		TreeSet<Label> labelSet = new TreeSet<Label>();
		for (String text:texts) {
			labelSet.add(Labels.getLabel(text));
		}
		return labelSet;
	}
	
	/**
	 * Change labels to one line string by their ID in order.
	 * @param labelsPar the labels
	 * @return the one line string
	 */
	public static String labelsToString(final Collection<Label> labelsPar) {
		TreeSet<Label> labelSet = new TreeSet<Label>(labelsPar);
		StringBuffer str = new StringBuffer();
		Iterator<Label> iterator = labelSet.iterator();
		if (iterator.hasNext()) {
			str = str.append(iterator.next().getText());
		}
		while (iterator.hasNext()) {
			str = str.append(DELIMITER + iterator.next().getText());
		}
		return str.toString();
	}
	
	/**
	 * @param s the one line string
	 * @return the labels
	 */
	public static TreeSet<Label> loadFromString(final String s) {
		TreeSet<Label> labelSet = new TreeSet<Label>();
		String[] ss = s.split(DELIMITERREG);
		for (String label:ss) {
			labelSet.add(Labels.getLabel(label));
		}
		return labelSet;
	}

	/**
	 * 
	 * @param label label
	 * @return string
	 */
	public static String labelsToString(final Label label) {
		return label.getText();
	}
	
	/**
	 * @return number of labels
	 */
	public static int cntLabel() {
		return labels.size();
	}
}
