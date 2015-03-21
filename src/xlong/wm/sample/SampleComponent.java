package xlong.wm.sample;

import java.io.Serializable;
import java.util.Set;

/**
 * InstanceComponent.
 */
public interface SampleComponent extends Serializable {
	/**
	 * @return number of instances in this component.
	 */
	int countSample();
	
	/**
	 * @return true if this component is a leaf.
	 */
	boolean isLeaf();
	
	/**
	 * @return the labels.
	 */
	Set<Label> getLabels();
	
}
