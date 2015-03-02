package com.hotpads.job.web;

import java.util.List;

import com.hotpads.util.core.enums.StringPersistedEnum;
import com.hotpads.util.core.web.HTMLSelectOptionBean;

public interface JobCategory extends StringPersistedEnum{
	public List<HTMLSelectOptionBean> getHtmlSelectOptions();
}
