package xlong.wm.sample;

/**
 * 
 * @author longx
 *
 */
public class SparseVectors implements Properties {

	private static final long serialVersionUID = 7173267656424961385L;

	@Override
	public final Property getProperty(final String s) {
		return new SparseVector(s);
	}

}
