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
import com.hotpads.trace.TraceSpan;

@SuppressWarnings("serial")
@Embeddable
public class CountKey extends BasePrimaryKey<CountKey>{

	/****************************** fields ********************************/
	
	protected String name;
	protected Long periodMs;
	protected Long startTimeMs;
	
	
	public static final String
		COL_name = "name",
		COL_periodMs = "periodMs",
		COL_startTimeMs = "startTimeMs";
	
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(TraceSpan.KEY_key, COL_name, name),
				new UInt63Field(TraceSpan.KEY_key, COL_periodMs, periodMs),
				new UInt63Field(TraceSpan.KEY_key, COL_startTimeMs, startTimeMs));
	}
	

	/****************************** constructor ********************************/
	
	CountKey(){
		super();
	}
	

	public CountKey(String name, Long periodMs, Long startTimeMs){
		super();
		this.name = name;
		this.periodMs = periodMs;
		this.startTimeMs = startTimeMs;
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
		
}


