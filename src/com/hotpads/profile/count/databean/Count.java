package com.hotpads.profile.count.databean;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.profile.count.databean.key.CountKey;
import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

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
	
	public Count(String name, String sourceType, String source, Long periodMs, Long startTimeMs, Long value){
		this.key = new CountKey(name, sourceType, source, periodMs, startTimeMs);
		this.value = value;
	}
	
	
	/************************** databean **************************************/
	
	@Override
	public Class<CountKey> getKeyClass(){
		return CountKey.class;
	}
	
	@Override
	public CountKey getKey(){
		return key;
	}

	/********************************* useful ****************************************/
	
	public String getTimeString(){
		String s = DateTool.getYYYYMMDDHHMMSSWithPunctuationNoSpaces(new Date(this.getStartTimeMs()));
		return s.replace("_", " ");
	}
	
	public double getValuePerSecond(){
		return ((double)this.value) / (this.getPeriodMs() / 1000);
	}
	
	/********************************** static ******************************************/
	
	public static long getIntervalStart(long periodMs, long timeMs){
		long msToSubtract = timeMs % periodMs;
		return timeMs - msToSubtract;
	}
	
	public static List<Count> getListWithGapsFilled(
			String otherName, String otherSourceType, String otherSource,
			Long periodMs, List<Count> ins, Long startTime, Long endTime){
		int numPoints = (int)((endTime - startTime) / periodMs);
		List<Count> outs = ListTool.createArrayList(numPoints + 1);
		long intervalStart = startTime;
		Iterator<Count> i = IterableTool.nullSafe(ins).iterator();
		Count next = IterableTool.next(i);
		while(intervalStart <= endTime){
			if(next != null && next.getStartTimeMs().equals(intervalStart)){
				outs.add(next);
				next = IterableTool.next(i);
			}else{
				Count zero = new Count(otherName, otherSourceType, otherSource, 
						periodMs, intervalStart, 0L);
				outs.add(zero);
			}
			intervalStart += periodMs;
		}
		return outs;
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


	public String getSource(){
		return key.getSource();
	}

	public void setSource(String source){
		key.setSource(source);
	}

	public String getSourceType(){
		return key.getSourceType();
	}

	public void setSourceType(String sourceType){
		key.setSourceType(sourceType);
	}

	
}
