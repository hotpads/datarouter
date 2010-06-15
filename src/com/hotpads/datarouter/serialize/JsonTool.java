package com.hotpads.datarouter.serialize;

import java.util.List;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.IterableTool;

public class JsonTool{

	public static JSON getJson(List<Field<?>> fields) {
		JSONObject j = new JSONObject();
		for(Field<?> f : IterableTool.nullSafe(fields)){
			j.element(f.getName(), f.getValue());
		}
		return j;
	}

	public static <J extends Databean<?>> J fromJsonString(Class<J> clazz, String jsonString) {
		JSONObject jsonObject = (JSONObject)JSONSerializer.toJSON(jsonString);
		JsonConfig jsonConfig = new JsonConfig();  
		jsonConfig.setRootClass(clazz);  
		@SuppressWarnings("unchecked")
		J j = (J)JSONObject.toBean(jsonObject, jsonConfig );
		return j;
	}
	
}
