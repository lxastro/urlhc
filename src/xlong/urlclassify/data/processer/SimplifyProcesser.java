package xlong.urlclassify.data.processer;

/**
 * 
 * @author longx
 *
 */
public class SimplifyProcesser extends StringArrayProcesser {
	
	/**
	 * 
	 */
	public SimplifyProcesser() {
		super(null);
	}
	
	/**
	 * @param father father
	 */
	public SimplifyProcesser(final StringArrayProcesser father) {
		super(father);
	}

	@Override
	public final String[] metaProcess(final String[] in) {
		return new String[] {in[0].substring(28), in[1]};
	}
}
