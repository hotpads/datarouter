package com.hotpads.profile.count.databean;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrXMLStringTool;
import com.hotpads.profile.count.databean.key.CountKey;

@Entity
@AccessType("field")
public class Count extends BaseDatabean<CountKey,Count>{
	static Logger logger = LoggerFactory.getLogger(Count.class);

	@Id
	protected CountKey key;
	protected Long value;
		
	
	/**************************** columns *******************************/
	
	public static final String
		KEY_NAME = "key",
		COL_value = "value";
	
	public static class CountFielder extends BaseDatabeanFielder<CountKey,Count>{
		public CountFielder(){}
		@Override
		public Class<CountKey> getKeyFielderClass(){
			return CountKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(Count d){
			return FieldTool.createList(
					new UInt63Field(COL_value, d.value));
		}
	}
	
	
	/*********************** constructor **********************************/
	
	Count(){
		this(null, null, null, null, null, null, null);
	}
	
	public Count(String name, String sourceType, Long periodMs, 
			Long startTimeMs, String source, Long created, Long value){
		this.key = new CountKey(name, sourceType, periodMs, startTimeMs, source, created);
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
		String s = DrDateTool.getYYYYMMDDHHMMSSWithPunctuationNoSpaces(new Date(this.getStartTimeMs()));
		return s.replace("_", " ");
	}
	
	public double getValuePer(String frequency){
		if("period".equals(frequency)){
			return value;
		}else if("second".equals(frequency)){
			return getValuePerSecond();
		}else if("minute".equals(frequency)){
			return getValuePerMinute();
		}else if("hour".equals(frequency)){ return getValuePerHour(); }
		throw new IllegalArgumentException("unknown frequency: " + frequency);
	}
	
	public double getValuePerSecond(){
		return ((double)value) * 1000 / getPeriodMs();
	}
	
	public double getValuePerMinute(){
		return ((double)value) * 60000 / getPeriodMs();
	}
	
	public double getValuePerHour(){
		return ((double)value) * 3600000 / getPeriodMs();
	}
	
	public static double getValuePer(double value, Long periodMs, String frequencyString){
		if("period".equals(frequencyString)){			
			return value;
		}else if("second".equals(frequencyString)){
			return getValuePerSecond(value, periodMs);
		}else if("minute".equals(frequencyString)){
			return getValuePerMinute(value, periodMs);
		}else if("hour".equals(frequencyString)){ 
			return getValuePerHour(value, periodMs); 
		}else{
			Long frequencyInMs = DrNumberTool.getLongNullSafe(frequencyString, null);
			if(frequencyInMs == null || frequencyInMs < 1L){ 
				throw new IllegalArgumentException("unknown frequency or bad frequency: " + frequencyString); 
			}
			return value * frequencyInMs / periodMs;
		}
	}

	public static double getValuePerSecond(double value, Long periodMs){
		return (value) * 1000 / periodMs;
	}

	public static double getValuePerMinute(double value, Long periodMs){
		return (value) * 60000 / periodMs;
	}

	public static double getValuePerHour(double value, Long periodMs){
		return (value) * 3600000 / periodMs;
	}
	
	public String getNameHtmlEscaped(){
		return DrXMLStringTool.escapeXml(getName());
	}
	
	public Long increment(Long value){
		if(value==null){ return value; }
		this.value += value;
		return value;
	}
	
	/********************************** static ******************************************/
	
	public static long getIntervalStart(long periodMs, long timeMs){
		long msToSubtract = timeMs % periodMs;
		return timeMs - msToSubtract;
	}
	
	public static List<Count> filterForSource(Collection<Count> ins, String source){
		List<Count> outs = DrListTool.createArrayList();
		for(Count in : DrIterableTool.nullSafe(ins)){
			if(DrObjectTool.equals(source, in.getSource())){ outs.add(in); }
		}
		return outs;
	}
	
	public static List<Count> getListWithGapsFilled(String otherName, String otherSourceType, String otherSource,
			Long periodMs, Iterable<Count> ins, Long startTime, Long endTime){
		int numPoints = (int)((endTime - startTime) / periodMs);
		List<Count> outs = DrListTool.createArrayList(numPoints + 1);
		long intervalStart = startTime;
		Iterator<Count> iterator = DrIterableTool.nullSafe(ins).iterator();
		Count next = DrIterableTool.next(iterator);
		// int numMatches=0, numNull=0, numOutOfRange=0;
		while(intervalStart <= endTime){
			if(next != null && next.getStartTimeMs().equals(intervalStart)){
				if(DrCollectionTool.notEmpty(outs)){
					Count last = DrCollectionTool.getLast(outs);
					if(last.getStartTimeMs().equals(next.getStartTimeMs())){
						last.increment(next.value);
					}else{
						outs.add(next);
					}
				}else{
					outs.add(next);
				}
				next = DrIterableTool.next(iterator);
				// ++numMatches;
			}else{
				// logger.warn("miss:"+new Date(intervalStart));
				if(next == null){
					// ++numNull; }

				}else{
					// ++numOutOfRange;
				}
				Count zero = new Count(otherName, otherSourceType, periodMs, intervalStart, otherSource, System
						.currentTimeMillis(), 0L);
				outs.add(zero);
			}
			if(next == null || next.getStartTimeMs() > intervalStart){
				intervalStart += periodMs;
			}
		}
		// logger.warn("numMatches="+numMatches);
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

	public Long getCreated(){
		return key.getCreated();
	}

	public void setCreated(Long created){
		key.setCreated(created);
	}

	@Override
	public String toString(){
		return getStartTimeMs() + ", " + getValue()+"\n";
		//return  getValue()+",";
	}

}
