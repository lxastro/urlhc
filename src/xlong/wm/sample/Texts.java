package xlong.wm.sample;

/**
 * 
 * @author longx
 *
 */
public class Texts implements Properties {

	@Override
	public final Property getProperty(final String s) {
		return new Text(s);
	}

}
