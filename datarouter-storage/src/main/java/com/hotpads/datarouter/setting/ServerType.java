package com.hotpads.datarouter.setting;

import java.util.List;

import com.hotpads.util.core.web.HtmlSelectOptionBean;

public interface ServerType {

	public static final String
			ALL = "all",
			UNKNOWN = "unknown",
			DEV = "dev";

	List<HtmlSelectOptionBean> getHTMLSelectOptionsVarNames();

	ServerType fromPersistentString(String str);

	String getPersistentString();

}
