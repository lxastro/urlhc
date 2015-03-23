package xlong.wm.classifier;

public class OutputStructure implements Comparable<OutputStructure>{
	private String label;
	private double p;
	public OutputStructure(String label, double p) {
		setLabel(label);
		setP(p);
	}
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return the p
	 */
	public double getP() {
		return p;
	}
	/**
	 * @param p the p to set
	 */
	public void setP(double p) {
		this.p = p;
	}
	@Override
	public int compareTo(OutputStructure o) {
		return -(int)Math.signum(p-o.p);
	}
	

}
