/**
 * 
 */
package com.hotpads.profile.count.databean.key;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.datarouter.util.core.DateTool;

@SuppressWarnings("serial")
@Embeddable
public class CountKey extends BasePrimaryKey<CountKey>{

	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;
	
	
	/****************************** fields ********************************/
	
	/*
	 * note: this field ordering is geared towards scanning through all the counts of a given sourceType.
	 *   To look at a single source, you will still have to scan through all sources in that source type which is wasteful for
	 *   a large number of sources.  You can investigate a single source via it's memory counters, 
	 *   or if you need persistent counts, then a separate table with a different CountKey could be created.
	 */
	
	protected String name;//eg "get Listing" or "rawSearch"
	protected String sourceType;//eg "site" or "modelIndex"
	protected Long periodMs;
	protected Long startTimeMs;
	protected String source;//usually a machine name, eg "webhead93"
	protected Long created;//needed to distinguish between separate counts in the same period
	
	public static final String
		COL_name = "name",
		COL_sourceType = "sourceType",
		COL_periodMs = "periodMs",
		COL_startTimeMs = "startTimeMs",
		COL_source = "source",
		COL_created = "created";
	
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new StringField(COL_name, name, DEFAULT_STRING_LENGTH),
				new StringField(COL_sourceType, sourceType, DEFAULT_STRING_LENGTH),
				new UInt63Field(COL_periodMs, periodMs),
				new UInt63Field(COL_startTimeMs, startTimeMs),
				new StringField(COL_source, source, DEFAULT_STRING_LENGTH),
				new UInt63Field(COL_created, created));
	}
	

	/****************************** constructor ********************************/
	
	CountKey(){
		super();
	}
	

	public CountKey(String name, String sourceType, Long periodMs, 
			Long startTimeMs, String source, Long created){
		super();
		this.name = name;
		this.sourceType = sourceType;
		this.periodMs = periodMs;
		this.startTimeMs = startTimeMs;
		this.source = source;
		this.created = created;
	}
	
	/****************************** standard ********************************/

	@Override
	public String toString(){
		String time = startTimeMs==null?"":DateTool.getYYYYMMDDHHMMSSWithPunctuationNoSpaces(startTimeMs);
		String flushDelaySeconds = startTimeMs==null||created==null?"":(created - startTimeMs) / 1000 + "";
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


	public Long getCreated(){
		return created;
	}


	public void setCreated(Long created){
		this.created = created;
	}


		
}


