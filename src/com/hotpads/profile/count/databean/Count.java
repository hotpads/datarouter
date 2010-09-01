package com.hotpads.profile.count.databean;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.log4j.Logger;
import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.profile.count.databean.key.CountKey;
import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.XMLStringTool;

@SuppressWarnings("serial")
@Entity
@AccessType("field")
public class Count extends BaseDatabean<CountKey>{
	static Logger logger = Logger.getLogger(Count.class);

	@Id
	protected CountKey key;
	protected Long value;
		
	
	/**************************** columns *******************************/
	
	public static final String
		KEY_NAME = "key",
		COL_value = "value";

	@Override
	public List<Field<?>> getNonKeyFields(){
		return FieldTool.createList(
				new UInt63Field(COL_value, this.value));
	}
	
	@Override
	public boolean isFieldAware(){
		return true;
	}
	
	
	/*********************** constructor **********************************/
	
	Count(){
		this(null, null, null, null, null, null, null);
	}
	
	public Count(String name, String sourceType, String source, 
			Long periodMs, Long startTimeMs, Long flushTimeMs, Long value){
		this.key = new CountKey(name, sourceType, source, periodMs, startTimeMs, flushTimeMs);
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
	
	public double getValuePer(String frequency){
		if("second".equals(frequency)){
			return getValuePerSecond();
		}else if("minute".equals(frequency)){
			return getValuePerMinute();
		}else if("hour".equals(frequency)){
			return getValuePerHour();
		}
		throw new IllegalArgumentException("unknown frequency: "+frequency);
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
	
	public String getNameHtmlEscaped(){
		return XMLStringTool.escapeXml(getName());
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
	
	public static List<Count> getListWithGapsFilled(
			String otherName, String otherSourceType, String otherSource,
			Long periodMs, List<Count> ins, Long startTime, Long endTime){
		int numPoints = (int)((endTime - startTime) / periodMs);
		List<Count> outs = ListTool.createArrayList(numPoints + 1);
		long intervalStart = startTime;
		Iterator<Count> i = IterableTool.nullSafe(ins).iterator();
		Count next = IterableTool.next(i);
		int numMatches=0, numNull=0, numOutOfRange=0;
		while(intervalStart <= endTime){
			if(next != null && next.getStartTimeMs().equals(intervalStart)){
//				logger.warn("match:"+new Date(intervalStart)+" "+new Date(next.getStartTimeMs()));
				if(outs.size()>0){
					Count last = outs.get(outs.size()-1);
					if(last.getStartTimeMs().equals(next.getStartTimeMs())){
						last.increment(next.value);
					}else{
						outs.add(next);
					}
				}else{
					outs.add(next);
				}
				next = IterableTool.next(i);
				++numMatches;
			}else{
//				logger.warn("miss:"+new Date(intervalStart));
				if(next==null){ ++numNull; }
				else{ ++numOutOfRange; }
				Count zero = new Count(otherName, otherSourceType, otherSource, 
						periodMs, intervalStart, System.currentTimeMillis(), 0L);
				outs.add(zero);
			}
			intervalStart += periodMs;
		}
//		logger.warn("numMatches="+numMatches);
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

	public Long getFlushTimeMs(){
		return key.getFlushTimeMs();
	}

	public void setFlushTimeMs(Long flushTimeMs){
		key.setFlushTimeMs(flushTimeMs);
	}

	
}
