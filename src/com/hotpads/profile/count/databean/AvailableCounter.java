package com.hotpads.profile.count.databean;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.profile.count.databean.key.AvailableCounterKey;

@SuppressWarnings("serial")
@Entity
@AccessType("field")
public class AvailableCounter extends BaseDatabean<AvailableCounterKey>{

	@Id
	protected AvailableCounterKey key;
	protected Long lastUpdated;
		
	
	/**************************** columns *******************************/
	
	public static final String
		KEY_NAME = "key",
		COL_lastUpdated = "lastUpdated";
	
	
	/*********************** constructor **********************************/
	
	AvailableCounter(){
	}
	
	public AvailableCounter(String name, String sourceType, String source, Long periodMs, Long lastUpdated){
		this.key = new AvailableCounterKey(sourceType, source, periodMs, name);
		this.lastUpdated = lastUpdated;
	}
	
	
	/************************** databean **************************************/
	
	@Override
	public Class<AvailableCounterKey> getKeyClass(){
		return AvailableCounterKey.class;
	}
	
	@Override
	public AvailableCounterKey getKey(){
		return key;
	}
	
	
	/********************************* get/set ****************************************/

	public void setKey(AvailableCounterKey key){
		this.key = key;
	}

	public Long getPeriodMs(){
		return key.getPeriodMs();
	}

	public void setPeriodMs(Long periodMs){
		key.setPeriodMs(periodMs);
	}


	public String getName(){
		return key.getName();
	}

	public void setName(String name){
		key.setName(name);
	}


	public String getSource(){
		return key.getSource();
	}

	public void setSource(String source){
		key.setSource(source);
	}

	public Long getLastUpdated(){
		return lastUpdated;
	}

	public void setLastUpdated(Long lastUpdated){
		this.lastUpdated = lastUpdated;
	}

	public String getSourceType(){
		return key.getSourceType();
	}

	public void setSourceType(String sourceType){
		key.setSourceType(sourceType);
	}

	
}
