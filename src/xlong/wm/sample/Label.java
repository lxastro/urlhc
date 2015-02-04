package xlong.wm.sample;

/**
 * The interface for Label.
 */
public interface Label extends Comparable<Label> {
	/**
	 * @return text of label
	 */
	String getText();
	/**
	 * @return ID of label;
	 */
	int getID();
}
