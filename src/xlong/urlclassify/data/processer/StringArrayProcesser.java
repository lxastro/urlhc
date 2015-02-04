package xlong.urlclassify.data.processer;

/**
 * 
 * @author longx
 *
 */
public abstract class StringArrayProcesser {
	/** */
	private StringArrayProcesser father;
	
	/**
	 * @param father father
	 */
	public StringArrayProcesser(final StringArrayProcesser father) {
		this.father = father;
	}
	/**
	 * @param in process in
	 * @return process out
	 */
	public abstract String[] metaProcess(String[] in);

	/**
	 * @param in process in
	 * @return process out
	 */
	public final String[] process(final String[] in) {
		if (father == null) {
			return metaProcess(in);
		} else {
			return metaProcess(father.process(in));
		}
	}
}
