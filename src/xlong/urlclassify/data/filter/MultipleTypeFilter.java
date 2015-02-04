package xlong.urlclassify.data.filter;

import xlong.urlclassify.data.Entity;

public class MultipleTypeFilter extends EntityFilter {

	public MultipleTypeFilter() {
		super(null);
	}
	public MultipleTypeFilter(EntityFilter father) {
		super(father);
	}

	@Override
	public boolean metaFilter(Entity en) {
		return en.getTypes().size() > 1;
	}

}
