package xlong.urlclassify.data.processer;

/**
 * 
 * @author longx
 *
 */
public class UrlNormalizeProcesser extends StringArrayProcesser {

	/**
	 * 
	 */
	public UrlNormalizeProcesser() {
		super(null);
	}
	/**
	 * 
	 * @param father father
	 */
	public UrlNormalizeProcesser(final StringArrayProcesser father) {
		super(father);
	}

	@Override
	public final String[] metaProcess(final String[] in) {
		String[] out = new String[in.length];
		for (int i = 0; i < in.length; i++) {
			out[i] = UrlNormalizer.normalize(in[i]);
		}
		return out;
	}
}
