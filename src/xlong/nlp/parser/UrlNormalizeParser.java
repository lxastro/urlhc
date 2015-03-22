package xlong.nlp.parser;

/**
 * 
 * @author longx
 *
 */
public class UrlNormalizeParser extends Parser {

	/**
	 * 
	 * @param father father
	 */
	public UrlNormalizeParser(Parser father) {
		super(father);
	}
	
	@Override
	protected String myParse(String text) {
		return UrlNormalizer.normalize(text);
	}
}
