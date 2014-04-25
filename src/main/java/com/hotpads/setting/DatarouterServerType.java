package com.hotpads.setting;

import java.util.List;

import com.hotpads.util.core.web.HTMLSelectOptionBean;

public interface DatarouterServerType {

	String getPersistentString();

	String getUNKNOWNPersistentString();

	String getALLPersistentString();

	DatarouterServerType fromPersistentStringStatic(String required);

	List<HTMLSelectOptionBean> getHTMLSelectOptionsVarNames();

}
