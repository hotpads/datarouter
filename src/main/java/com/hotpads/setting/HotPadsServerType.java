package com.hotpads.setting;

import java.util.List;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.enums.EnumTool;
import com.hotpads.util.core.enums.HpVarEnum;
import com.hotpads.util.core.web.HTMLSelectOptionBean;

public enum HotPadsServerType implements HpVarEnum, ServerType<HotPadsServerType>{
	UNKNOWN(-1, "Unknown", ServerType.UNKNOWN, false),
	ALL(0, "All", ServerType.ALL, true),
	WEB(1, "Web", "web", true),
	INDEX(2, "Index", "index", true),
	JOB(3, "Job", "job", true),
	JOB_MASTER(4, "Job Master", "job_master", true),
	JOBLET(5, "Joblet", "joblet", true),
	TEXT_INDEX(6, "Text Index", "text", true),
	DEV(7, "Dev", "dev", false),
	JOBLET2(8, "Joblet2", "joblet2", true),
	JOB2(9, "Job2", "job2", true),
	WEBTEST(10, "Webtest", "web_test", true);
	
	public static HotPadsServerType fromInteger(Integer value){
		return EnumTool.getEnumFromInteger(values(), value, UNKNOWN);
	}
	
	public static List<HTMLSelectOptionBean> getHTMLSelectOptions(){
		return EnumTool.getHTMLSelectOptions(values());
	}
	
	public static List<HTMLSelectOptionBean> getHTMLSelectOptionsVarNames(){
		return EnumTool.getHTMLSelectOptionsVarNames(values());
	}

	private Integer value;
	private String display;
	private String varName;
	private boolean live;
	
	private HotPadsServerType(Integer value, String display, String varName, boolean live){
		this.value = value;
		this.display = display;
		this.varName = varName;
		this.live = live;
	}
	
	public Integer getInteger(){
		return value;
	}
	public String getDisplay() {
		return display;
	}
	public String getVarName(){
		return varName;
	}
	
	@Override
	public String getPersistentString() {
		return varName;
	}
	
	public static HotPadsServerType fromPersistentStringStatic(String s){
		return DataRouterEnumTool.getEnumFromString(values(), s, null);
	}
	
	@Override
	public HotPadsServerType fromPersistentString(String s) {
		return fromPersistentStringStatic(s);
	}
	
	public boolean isLive(){
		return live;
	}
	
	public static List<String> getCSVListValues(){
		List<String> servers = ListTool.create();

		for(int index = 0; index < HotPadsServerType.values().length; index++){
			servers.add(HotPadsServerType.values()[index].toString());
		}
		return servers;
	}

}
