package xlong.urlclassify.data.processer;

/**
 * 
 * @author longx
 *
 */
public class Triple2PairProcesser extends StringArrayProcesser {

	/**
	 * 
	 */
	public Triple2PairProcesser() {
		super(null);
	}
	
	/**
	 * 
	 * @param father father
	 */
	public Triple2PairProcesser(final StringArrayProcesser father) {
		super(father);
	}

	@Override
	public final String[] metaProcess(final String[] in) {
		return new String[] {in[0], in[2]};
	}

}
