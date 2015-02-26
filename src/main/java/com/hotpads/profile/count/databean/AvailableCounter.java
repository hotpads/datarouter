package com.hotpads.profile.count.databean;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import javax.persistence.Id;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.util.core.IterableTool;
import com.hotpads.datarouter.util.core.ListTool;
import com.hotpads.datarouter.util.core.SetTool;
import com.hotpads.datarouter.util.core.XMLStringTool;
import com.hotpads.profile.count.databean.key.AvailableCounterKey;

public class AvailableCounter extends BaseDatabean<AvailableCounterKey,AvailableCounter>{

	@Id
	protected AvailableCounterKey key;
	protected Long lastUpdated;

	/**************************** columns *******************************/

	public static final String 
			KEY_NAME = "key", 
			COL_lastUpdated = "lastUpdated";


	public static class AvailableCounterFielder extends BaseDatabeanFielder<AvailableCounterKey,AvailableCounter>{
		public AvailableCounterFielder(){
		}

		@Override
		public Class<AvailableCounterKey> getKeyFielderClass(){
			return AvailableCounterKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(AvailableCounter d){
			return FieldTool.createList(
					new UInt63Field(COL_lastUpdated, d.lastUpdated));
		}
	}

	/*********************** constructor **********************************/

	AvailableCounter(){
		this(null, null, null, null, null);
	}

	public AvailableCounter(String sourceType, Long periodMs, String name, String source, Long lastUpdated){
		this.key = new AvailableCounterKey(sourceType, periodMs, name, source);
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

	/********************************* useful ********************************************/

	public String getNameHtmlEscaped(){
		return XMLStringTool.escapeXml(getName());
	}

	/********************************** static *****************************************/

	public static SortedSet<String> getAllSources(Collection<AvailableCounter> counters){
		SortedSet<String> outs = SetTool.createTreeSet();
		for(AvailableCounter counter : IterableTool.nullSafe(counters)){
			String source = counter.getSource();
			if(source == null) continue;
			outs.add(source);
		}
		return outs;
	}

	public static List<AvailableCounter> filterOutArrayServers(Collection<AvailableCounter> ins){
		List<AvailableCounter> outs = ListTool.createArrayList();
		for(AvailableCounter in : IterableTool.nullSafe(ins)){
			if(in.getSource().contains("#")){
				continue;
			}
			outs.add(in);
		}
		return outs;
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

	@Override
	public String toString(){
		return this.getName();
	}

}
