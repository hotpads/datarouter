package com.hotpads.profile.count;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.profile.count.key.CountKey;

@SuppressWarnings("serial")
@Entity
@AccessType("field")
public class Count extends BaseDatabean<CountKey>{

	@Id
	protected CountKey key;
	protected Long value;
		
	
	/**************************** columns *******************************/
	
	public static final String
		KEY_NAME = "key",
		COL_value = "value";
	
	
	/*********************** constructor **********************************/
	
	Count(){
	}
	
	public Count(String group, String source, String name, Long periodMs, Long startTimeMs, Long value){
		this.key = new CountKey(group, source, name, periodMs, startTimeMs);
		this.value = value;
	}
	
	
	/************************** databean **************************************/
	
	@Override
	public Class<CountKey> getKeyClass() {
		return CountKey.class;
	}
	
	@Override
	public CountKey getKey() {
		return key;
	}
	
	
	/********************************* get/set ****************************************/


	public Long getValue(){
		return value;
	}

	public void setValue(Long value){
		this.value = value;
	}

	public void setKey(CountKey key){
		this.key = key;
	}

	public Long getPeriodMs(){
		return key.getPeriodMs();
	}

	public Long getStartTimeMs(){
		return key.getStartTimeMs();
	}

	public void setPeriodMs(Long periodMs){
		key.setPeriodMs(periodMs);
	}

	public void setStartTimeMs(Long startTimeMs){
		key.setStartTimeMs(startTimeMs);
	}

	public String getName(){
		return key.getName();
	}

	public void setName(String name){
		key.setName(name);
	}

	public String getGroup(){
		return key.getGroup();
	}

	public String getSource(){
		return key.getSource();
	}

	public void setGroup(String group){
		key.setGroup(group);
	}

	public void setSource(String source){
		key.setSource(source);
	}

	
}
