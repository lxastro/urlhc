package xlong.wm.sample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import xlong.wm.ontology.OntologyTree;

/**
 * 
 */
public class Composite implements SampleComponent {
	/** */
	private final Vector<Sample> samples;
	/** */
	private final Vector<Composite> composites;
	/** */
	private final Label label;
	
	/**
	 * @param label the label
	 */
	public Composite(final Label label) {
		samples = new Vector<Sample>();
		composites = new Vector<Composite>();
		this.label = label;
	}
	
	/**
	 * 
	 * @param labelString the label string
	 */
	public Composite(final String labelString) {
		this(Labels.getLabel(labelString));
	}
	
	/**
	 * 
	 */
	public Composite() {
		this("root");
	}
	
	/**
	 * 
	 * @param tree ontologyTree
	 */
	public Composite(final OntologyTree tree) {
		this(tree.getTypeName());
		for (OntologyTree son:tree.getSons()) {
			this.addComposite(new Composite(son));
		}
	}
	
	/**
	 * @param in reader 
	 * @param pFactory property factory
	 * @throws IOException IOException
	 */
	public Composite(final BufferedReader in, final Properties pFactory) throws IOException {
		samples = new Vector<Sample>();
		composites = new Vector<Composite>();
		in.readLine();
		this.label = Labels.loadFromString(in.readLine()).first();
		
		in.mark(10);
		while (in.read() == '{') {
			in.reset();
			addComposite(new Composite(in, pFactory));
			in.mark(10);
		}
		in.reset();
		
		in.mark(10);
		while (!(in.read() == '}')) {
			in.reset();
			addSample(new Sample(in, pFactory));
			in.mark(10);
		}
		in.readLine();
	}
	
	/**
	 * @param filePath filePath 
	 * @param pFactory property factory
	 * @throws IOException IOException
	 */
	public Composite(final String filePath, final Properties pFactory) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filePath));
		samples = new Vector<Sample>();
		composites = new Vector<Composite>();
		in.readLine();
		this.label = Labels.loadFromString(in.readLine()).first();
		
		in.mark(10);
		while (in.read() == '{') {
			in.reset();
			addComposite(new Composite(in, pFactory));
			in.mark(10);
		}
		in.reset();
		
		in.mark(10);
		while (!(in.read() == '}')) {
			in.reset();
			addSample(new Sample(in, pFactory));
			in.mark(10);
		}
		in.readLine();
		in.close();
	}
	
	@Override
	public final int countSample() {
		int size = 0;
		for (SampleComponent component:samples) {
			size += component.countSample();
		}
		for (SampleComponent component:composites) {
			size += component.countSample();
		}
		return size;
	}

	@Override
	public final boolean isLeaf() {
		return false;
	}
	
	@Override
	public final Set<Label> getLabels() {
		TreeSet<Label> labels = new TreeSet<Label>();
		labels.add(label);
		return labels;
	}
	
	/**
	 * 
	 * @return the label
	 */
	public final Label getLabel() {
		return label;
	}
	
	/**
	 * 
	 * @return samples
	 */
	public final Vector<Sample> getSamples() {
		return samples;
	}
	
	/**
	 * 
	 * @return composites
	 */
	public final Vector<Composite> getComposites() {
		return composites;
	}
	
	/**
	 * 
	 * @param sample sample to add
	 */
	public final void addSample(final Sample sample) {
		boolean flag = true;
		for (Composite composite:composites) {
			if (sample.containLabel(composite.getLabel())) {
				composite.addSample(sample);
				flag = false;
			}
		}
		if (flag) {
			samples.add(sample);
		}
	}
	
	/**
	 * @param composite composite to add
	 * @return success or not
	 */
	public final boolean addComposite(final Composite composite) {
		return composites.add(composite);
	}

	/**
	 * @param out BufferedWriter
	 * @throws IOException IOException
	 */
	public final void save(final BufferedWriter out) throws IOException {
		out.write("{\n");
		out.write(Labels.labelsToString(label) + "\n");
		for (Composite composite:composites) {
			composite.save(out);
		}
		for (Sample sample:samples) {
			sample.save(out);
		}
		out.write("}\n");
	}
	
	/**
	 * 
	 * @param filePath the filePath
	 * @throws IOException IOException
	 */
	public final void save(final String filePath) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
		save(out);
		out.close();
	}
	
	/**
	 * @param percent percent
	 * @return count
	 */
	private int[] percentToCount(final int[] percent) {
		int npart = percent.length;
		int n = samples.size();
		int[] ns = new int[npart];
		int s = 0;
		int sp = 0;
		for (int i = 0; i < npart; i++) {
			ns[i] = (int) Math.round(((double) n) * percent[i] / 100);
			s += ns[i];
			sp += percent[i];
		}
		if (sp > 100) {
			return null;
		}
		if (s != (int) Math.round(((double) n) * sp / 100)) {
			int maxid = 0;
			for (int i = 1; i < npart; i++) {
				if (percent[i] > percent[maxid]) {
					maxid = i;
				}
			}
			ns[maxid] -= (s - (int) Math.round(((double) n) * sp / 100));
		}
		return ns;
	}
	
	/**
	 * @param percent split percents
	 * @param rand random
	 * @return split composites
	 */
	public final Vector<Composite> split(final int[] percent, final Random rand) {
		int npart = percent.length;
		int[] cnt = percentToCount(percent);
		Collections.shuffle(samples, rand);
		Vector<Composite> parts = new Vector<Composite>();
		int s = 0;
		for (int i = 0; i < npart; i++) {
			Composite part = new  Composite(label);
			for (int j = s; j < s + cnt[i]; j++) {
				part.addSample(samples.get(j));
			}
			parts.add(part);
			s += cnt[i];
		}		
		for (Composite composite:composites) {
			Vector<Composite> subParts = composite.split(percent, rand);
			for (int i = 0; i < npart; i++) {
				parts.get(i).addComposite(subParts.get(i));
			}
		}
		return parts;
	}
	
	public final void riseBranch(int minSamples) {
		Iterator<Composite> iterator = composites.iterator();
		while (iterator.hasNext()) {
			Composite composite = iterator.next();
			if (composite.countSample() < minSamples) {
				samples.addAll(composite.getAllSamples());
				iterator.remove();
			} else {
				composite.riseBranch(minSamples);
			}
		}		
	}
	
	public final void inner2outer() {
		if (samples.size() > 0 && composites.size() > 0) {
			Composite outerComposite = new Composite(label.getText() + "_other");
			outerComposite.samples.addAll(samples);
			samples.clear();
			addComposite(outerComposite);
		}
		for (Composite composite:composites) {
			composite.inner2outer();
		}
	}
	
	public final void cutBranch(int minSamples) {
		Iterator<Composite> iterator = composites.iterator();
		while (iterator.hasNext()) {
			Composite composite = iterator.next();
			composite.cutBranch(minSamples);
			if (composite.countSample() < minSamples) {
				iterator.remove();
			}
		}
	}
	
	public final Collection<Sample> getAllSamples() {
		HashSet<Sample> allSamples = new HashSet<Sample>();
		allSamples.addAll(samples);
		for (Composite composite:composites) {
			allSamples.addAll(composite.getAllSamples());
		}
		return allSamples;
	}
	
	public void flatComposite(int level) {
		if (level == 0) {
			Collection<Sample> allSamples = getAllSamples();
			samples.clear();
			samples.addAll(allSamples);
			composites.clear();
		} else {
			for (Composite subcomp:composites) {
				subcomp.flatComposite(level - 1);
			}
		}
	}
}
