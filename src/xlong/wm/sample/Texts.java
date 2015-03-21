package xlong.wm.sample;

/**
 * 
 * @author longx
 *
 */
public class Texts implements Properties {
	
	private static final long serialVersionUID = -7934491749352265020L;

	@Override
	public final Property getProperty(final String s) {
		return new Text(s);
	}

}
