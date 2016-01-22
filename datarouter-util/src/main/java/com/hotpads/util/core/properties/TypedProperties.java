package com.hotpads.util.core.properties;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrPropertiesTool;

public class TypedProperties{
	
	private List<Properties> propertiesList;
	
	public TypedProperties(Collection<Properties> propertiesList){
		this.propertiesList = DrListTool.createArrayList(propertiesList);
	}
	
	public TypedProperties(String path){
		Properties properties = DrPropertiesTool.parse(path);
		if(properties!=null){ 
			this.propertiesList = DrListTool.wrap(properties); 
		}
	}
	
	
	/********************* defaultable ******************************/

	public String getString(String key, String def){
		String v = getString(key);
		return v==null?def:v;
	}
	
	public Boolean getBoolean(String key, boolean def){
		Boolean v = getBoolean(key);
		return v==null?def:v;
	}
	
	public Short getShort(String key, short def){
		Short v = getShort(key);
		return v==null?def:v;
	}
	
	public Integer getInteger(String key, int def){
		Integer v = getInteger(key);
		return v==null?def:v;
	}
	
	public Long getLong(String key, long def){
		Long v = getLong(key);
		return v==null?def:v;
	}
	
	public Float getFloat(String key, float def){
		Float v = getFloat(key);
		return v==null?def:v;
	}
	
	public Double getDouble(String key, double def){
		Double v = getDouble(key);
		return v==null?def:v;
	}
	
	/********************** typed ***********************************/
	
	public Boolean getBoolean(String key){
		String sVal = getString(key);
		if(sVal==null){ return null; }
		return DrBooleanTool.isTrue(sVal);
	}
	
	public Short getShort(String key){
		String sVal = getString(key);
		if(sVal==null){ return null; }
		return Short.valueOf(sVal);
	}
	
	public Integer getInteger(String key){
		String sVal = getString(key);
		if(sVal==null){ return null; }
		return Integer.valueOf(sVal);
	}
	
	public Long getLong(String key){
		String sVal = getString(key);
		if(sVal==null){ return null; }
		return Long.valueOf(sVal);
	}
	
	public Float getFloat(String key){
		String sVal = getString(key);
		if(sVal==null){ return null; }
		return Float.valueOf(sVal);
	}
	
	public Double getDouble(String key){
		String sVal = getString(key);
		if(sVal==null){ return null; }
		return Double.valueOf(sVal);
	}
	
	/***************** required **********************************/
	
	public String getRequiredString(String key){
		String s = getString(key);
		if(s==null){ throw new IllegalArgumentException("cannot find required String "+key); }
		return s;
	}
	
	/****************** basic ************************************/

	public String getString(String key){
		return DrPropertiesTool.getFirstOccurrence(propertiesList, key);
	}
	
	public List<Properties> getUnModifiablePropertiesList(){
		return Collections.unmodifiableList(propertiesList);
	}
}
