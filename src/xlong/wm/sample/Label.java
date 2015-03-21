package xlong.wm.sample;

import java.io.Serializable;

/**
 * The interface for Label.
 */
public interface Label extends Comparable<Label>, Serializable {
	/**
	 * @return text of label
	 */
	String getText();
}
