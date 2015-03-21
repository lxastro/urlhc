package xlong.wm.sample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Instance to store property and labels.
 */
public class Sample implements SampleComponent {
	
	private static final long serialVersionUID = -2866643130656638671L;
	
	private final String url;
	/** Property of a instance. */
	private final Property property;
	/** Labels of a instance. */
	private TreeSet<Label> labels;
	/** Labels pool. */
	private static Map<String, TreeSet<Label>> labelsPool = new HashMap<String, TreeSet<Label>>();
	/**
	 * @param property the property
	 * @param labels the labels
	 */
	public Sample(final String url, final Property property, final Collection<Label> labels) {
		this.url = url;
		this.property = property;
		
		String str = Labels.labelsToString(labels);
		TreeSet<Label> labelSet = labelsPool.get(str);
		if (labelSet != null) {
			this.labels = labelSet;
		} else {
			this.labels = new TreeSet<Label>(labels);
			labelsPool.put(str, this.labels);
		}
	}
	/**
	 * @param in buffered reader
	 * @param pFactory a property factory
	 * @throws IOException IOException
	 */
	public Sample(final BufferedReader in, final Properties pFactory) throws IOException {
		url = in.readLine();
		property = pFactory.getProperty(in.readLine());
		labels = Labels.loadFromString(in.readLine());
	}
	
	/**
	 * @param property the property
	 */
	public Sample(final String url, final Property property) {
		this(url, property, new TreeSet<Label>());
	}
	@Override
	public final int countSample() {
		return 1;
	}

	@Override
	public final boolean isLeaf() {
		return true;
	}

	public final String getURL(){
		return url;
	}
	/**
	 * @return the property
	 */
	public final Property getProperty() {
		return property;
	}

	@Override
	public final Set<Label> getLabels() {
		return labels;
	}
	
	public final Label getLabel() {
		for (Label label:labels) {
			return label;
		}
		return null;
	}
	
	public final void setLabels(TreeSet<Label> labels) {
		this.labels = labels;
	}
	
	/**
	 * @param label the label
	 * @return contain or not
	 */
	public final boolean containLabel(final Label label) {
		return labels.contains(label);
	}
	
	/**
	 * @param labelSet the labels
	 * @return contain or not
	 */
	public final boolean containLabel(final Collection<Label> labelSet) {
		return labels.containsAll(labelSet);
	}
	
	/**
	 * @param out buffered writer
	 * @throws IOException IOException
	 */
	public final void save(final BufferedWriter out) throws IOException {
		out.write(toString());
	}
	
	@Override
	public String toString() {
		String s = url + "\n";
		s += property.getOneLineString() + "\n";
		s += Labels.labelsToString(labels) + "\n";
		return s;
	}

}
