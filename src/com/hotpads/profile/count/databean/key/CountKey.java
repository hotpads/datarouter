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
import com.hotpads.profile.count.databean.Count;
import com.hotpads.util.core.DateTool;

@SuppressWarnings("serial")
@Embeddable
public class CountKey extends BasePrimaryKey<CountKey>{

	/****************************** fields ********************************/
	
	protected String name;//eg "get Listing" or "rawSearch"
	protected String sourceType;
	protected String source;//eg "webhead93" or "0130221"
	protected Long periodMs;
	protected Long startTimeMs;
	protected Long flushTimeMs;
	
	
	public static final String
		COL_name = "name",
		COL_sourceType = "sourceType",
		COL_source = "source",
		COL_periodMs = "periodMs",
		COL_startTimeMs = "startTimeMs",
		COL_flushTimeMs = "flushTimeMs";
	
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(Count.KEY_NAME, COL_name, name),
				new StringField(Count.KEY_NAME, COL_sourceType, sourceType),
				new StringField(Count.KEY_NAME, COL_source, source),
				new UInt63Field(Count.KEY_NAME, COL_periodMs, periodMs),
				new UInt63Field(Count.KEY_NAME, COL_startTimeMs, startTimeMs),
				new UInt63Field(Count.KEY_NAME, COL_flushTimeMs, flushTimeMs));
	}
	

	/****************************** constructor ********************************/
	
	CountKey(){
		super();
	}
	

	public CountKey(String name, String sourceType, String source, 
			Long periodMs, Long startTimeMs, Long flushTimeMs){
		super();
		this.name = name;
		this.sourceType = sourceType;
		this.source = source;
		this.periodMs = periodMs;
		this.startTimeMs = startTimeMs;
		this.flushTimeMs = flushTimeMs;
	}
	
	/****************************** standard ********************************/

	@Override
	public String toString(){
		String time = startTimeMs==null?"":DateTool.getYYYYMMDDHHMMSSWithPunctuationNoSpaces(startTimeMs);
		String flushDelaySeconds = (flushTimeMs - startTimeMs) / 1000 + "";
		return super.toString()+"["+time+"+"+flushDelaySeconds+"s]";
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


	public Long getFlushTimeMs(){
		return flushTimeMs;
	}


	public void setFlushTimeMs(Long flushTimeMs){
		this.flushTimeMs = flushTimeMs;
	}
		
}


