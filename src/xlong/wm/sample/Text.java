package xlong.wm.sample;

/**
 * 
 * @author longx
 *
 */
public class Text implements Property {

	private static final long serialVersionUID = -7270111198341101987L;
	/** */
	private final String text;
	
	/**
	 * @param s the text
	 */
	public Text(final String s) {
		this.text = s;
	}
	
	/**
	 * @return text;
	 */
	public final String getText() {
		return text;
	}
	
	@Override
	public final String toString() {
		return text;
	}

	@Override
	public final String getOneLineString() {
		return text;
	}
	
}
