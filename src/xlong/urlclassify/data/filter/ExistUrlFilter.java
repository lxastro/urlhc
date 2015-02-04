package xlong.urlclassify.data.filter;

import xlong.urlclassify.data.Entity;

public class ExistUrlFilter extends EntityFilter {

	
	public ExistUrlFilter() {
		super(null);
	}
	
	public ExistUrlFilter(EntityFilter father) {
		super(father);
	}

	@Override
	public boolean metaFilter(Entity en) {
		return en.cntUrls() > 0;
	}

}
