package com.hotpads.job.trigger.noop;

import java.util.Collections;
import java.util.List;

import com.hotpads.job.web.JobCategory;
import com.hotpads.util.core.web.HTMLSelectOptionBean;

public enum EmptyJobCategory implements JobCategory{
	NONE;

	@Override
	public String getDisplay(){
		return null;
	}

	@Override
	public String getPersistentString(){
		return null;
	}

	@Override
	public List<HTMLSelectOptionBean> getHtmlSelectOptions(){
		return Collections.emptyList();
	}

}
