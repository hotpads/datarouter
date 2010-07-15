/**
 * 
 */
package com.hotpads.profile.count.key;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.profile.count.Count;

@SuppressWarnings("serial")
@Embeddable
public class CountKey extends BasePrimaryKey<CountKey>{

	/****************************** fields ********************************/

	protected String name;//eg "get Listing" or "rawSearch"
	protected String group;//eg, "server" or "indexBranch"
	protected String source;//eg "webhead93" or "0130221"
	protected Long periodMs;
	protected Long startTimeMs;
	
	
	public static final String
	COL_name = "name",
		COL_group = "group",
		COL_source = "source",
		COL_periodMs = "periodMs",
		COL_startTimeMs = "startTimeMs";
	
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(Count.KEY_NAME, COL_name, name),
				new StringField(Count.KEY_NAME, COL_group, group),
				new StringField(Count.KEY_NAME, COL_source, source),
				new UInt63Field(Count.KEY_NAME, COL_periodMs, periodMs),
				new UInt63Field(Count.KEY_NAME, COL_startTimeMs, startTimeMs));
	}
	

	/****************************** constructor ********************************/
	
	CountKey(){
		super();
	}
	

	public CountKey(String name, String group, String source, Long periodMs, Long startTimeMs){
		super();
		this.group = group;
		this.source = source;
		this.periodMs = periodMs;
		this.startTimeMs = startTimeMs;
		this.name = name;
	}


	/****************************** get/set ********************************/
	
	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public Long getPeriodMs(){
		return periodMs;
	}

	public void setPeriodMs(Long periodMs){
		this.periodMs = periodMs;
	}

	public Long getStartTimeMs(){
		return startTimeMs;
	}

	public void setStartTimeMs(Long startTimeMs){
		this.startTimeMs = startTimeMs;
	}


	public String getGroup(){
		return group;
	}


	public void setGroup(String group){
		this.group = group;
	}


	public String getSource(){
		return source;
	}


	public void setSource(String source){
		this.source = source;
	}
		
}


