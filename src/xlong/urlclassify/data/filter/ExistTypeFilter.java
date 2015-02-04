package xlong.urlclassify.data.filter;

import xlong.urlclassify.data.Entity;

public class ExistTypeFilter extends EntityFilter {

	
	public ExistTypeFilter() {
		super(null);
	}
	
	public ExistTypeFilter(EntityFilter father) {
		super(father);
	}

	@Override
	public boolean metaFilter(Entity en) {
		return en.cntTypes() > 0;
	}

}
