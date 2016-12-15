package com.hotpads.util.core.properties;

import java.util.ArrayList;
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
		this.propertiesList = new ArrayList<>(propertiesList);
	}

	public TypedProperties(String path){
		Properties properties = DrPropertiesTool.parse(path);
		if(properties != null){
			this.propertiesList = DrListTool.wrap(properties);
		}
	}


	/********************* defaultable ******************************/

	public String getString(String key, String def){
		String val = getString(key);
		return val == null ? def : val;
	}

	public String getString(String key){
		return DrPropertiesTool.getFirstOccurrence(propertiesList, key);
	}

	public Boolean getBoolean(String key, boolean def){
		Boolean val = getBoolean(key);
		return val == null ? def : val;
	}

	public Boolean getBoolean(String key){
		String val = getString(key);
		if(val == null){
			return null;
		}
		return DrBooleanTool.isTrue(val);
	}

	public Integer getInteger(String key, int def){
		Integer val = getInteger(key);
		return val == null ? def : val;
	}

	public Integer getInteger(String key){
		String val = getString(key);
		if(val == null){
			return null;
		}
		return Integer.valueOf(val);
	}

	/********************** typed ***********************************/

	public Short getShort(String key){
		String val = getString(key);
		if(val == null){
			return null;
		}
		return Short.valueOf(val);
	}

	public Long getLong(String key){
		String val = getString(key);
		if(val == null){
			return null;
		}
		return Long.valueOf(val);
	}

	public Float getFloat(String key){
		String val = getString(key);
		if(val == null){
			return null;
		}
		return Float.valueOf(val);
	}

	public Double getDouble(String key){
		String val = getString(key);
		if(val == null){
			return null;
		}
		return Double.valueOf(val);
	}

	/***************** required **********************************/

	public String getRequiredString(String key){
		String str = getString(key);
		if(str == null){
			throw new IllegalArgumentException("cannot find required String " + key);
		}
		return str;
	}

	/****************** basic ************************************/

	public List<Properties> getUnmodifiablePropertiesList(){
		return Collections.unmodifiableList(propertiesList);
	}
}
