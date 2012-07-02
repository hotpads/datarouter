/**
 * 
 */
package com.hotpads.profile.count.databean.key;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
@Embeddable
public class AvailableCounterKey extends BasePrimaryKey<AvailableCounterKey>{

	/****************************** fields ********************************/
	
	protected String sourceType;
	protected Long periodMs;
	protected String name;//eg "get Listing" or "rawSearch"
	protected String source;//eg "webhead93" or "0130221"
	
	
	public static final String
		COL_sourceType = "sourceType",
		COL_periodMs = "periodMs",
		COL_name = "name",
		COL_source = "source";
	
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(COL_sourceType, sourceType,255),
				new UInt63Field(COL_periodMs, periodMs),
				new StringField(COL_name, name,255),
				new StringField(COL_source, source,255));
	}
	

	/****************************** constructor ********************************/
	
	AvailableCounterKey(){
		super();
	}
	

	public AvailableCounterKey(String sourceType, Long periodMs, String name, String source){
		super();
		this.sourceType = sourceType;
		this.periodMs = periodMs;
		this.name = name;
		this.source = source;
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


	public String getSource(){
		return source;
	}


	public void setSource(String source){
		this.source = source;
	}


	public String getSourceType(){
		return sourceType;
	}


	public void setSourceType(String sourceType){
		this.sourceType = sourceType;
	}
		
}


