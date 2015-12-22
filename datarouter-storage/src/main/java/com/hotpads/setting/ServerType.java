package com.hotpads.setting;

import java.util.List;

import com.hotpads.util.core.web.HTMLSelectOptionBean;

public interface ServerType {

	public static final String
		ALL = "all",
		UNKNOWN = "unknown";

	List<HTMLSelectOptionBean> getHTMLSelectOptionsVarNames();

	ServerType fromPersistentString(String s);

	String getPersistentString();

}
