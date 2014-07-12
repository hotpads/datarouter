package com.hotpads.setting;

import java.util.List;

import com.hotpads.util.core.web.HTMLSelectOptionBean;

public interface DatarouterServerType {

	String getPersistentString();
	DatarouterServerType fromPersistentStringStatic(String required);
	List<HTMLSelectOptionBean> getHTMLSelectOptionsVarNames();

	public static interface DatarouterServerTypeTool {
		String getUNKNOWNPersistentString();
		String getALLPersistentString();
	}
}
