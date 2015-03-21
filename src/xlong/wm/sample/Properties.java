package xlong.wm.sample;

import java.io.Serializable;

/**
 * 
 * @author longx
 *
 */
public interface Properties extends Serializable {
	/**
	 * @param s the one line string
	 * @return the property
	 */
	Property getProperty(String s);

}
