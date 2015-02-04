package xlong.urlclassify.data.filter;

import xlong.urlclassify.data.Entity;

public class SingleTypeFilter extends EntityFilter {

	public SingleTypeFilter() {
		super(null);
	}
	public SingleTypeFilter(EntityFilter father) {
		super(father);
	}

	@Override
	public boolean metaFilter(Entity en) {
		return en.getTypes().size() == 1;
	}

}
