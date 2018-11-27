/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.model.serialize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.model.serialize.fielder.Fielder;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.lang.ReflectionTool;

public class JsonDatabeanTool{

	private static final JsonParser jsonParser = new JsonParser();

	/*----------------------------- pk to json ------------------------------*/

	public static <PK extends PrimaryKey<PK>> JsonObject primaryKeyToJson(PK pk, Fielder<PK> fielder){
		if(pk == null){
			return null;
		}
		return fieldsToJson(fielder.getFields(pk));
	}

	public static <PK extends PrimaryKey<PK>> JsonArray primaryKeysToJson(Iterable<PK> pks, Fielder<PK> fielder){
		JsonArray array = new JsonArray();
		for(PK pk : IterableTool.nullSafe(pks)){
			array.add(addFieldsToJsonObject(new JsonObject(), fielder.getFields(pk)));
		}
		return array;
	}

	/*-------------------------- databean to json ---------------------------*/

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	JsonObject databeanToJson(D databean, DatabeanFielder<PK,D> fielder){
		return databeanToJson(databean, fielder, false);
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	JsonObject databeanToJson(D databean, DatabeanFielder<PK,D> fielder, boolean flatKey){
		if(databean == null){
			return null;
		}
		JsonObject jsonObject = new JsonObject();
		if(flatKey){
			addFieldsToJsonObject(jsonObject, fielder.getKeyFields(databean));
		}else{
			jsonObject.add(databean.getKeyFieldName(), primaryKeyToJson(databean.getKey(), fielder.getKeyFielder()));
		}
		addFieldsToJsonObject(jsonObject, fielder.getNonKeyFields(databean));
		return jsonObject;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	String databeanToJsonString(D databean, DatabeanFielder<PK,D> fielder){
		return databeanToJsonString(databean, fielder, false);
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	String databeanToJsonString(D databean, DatabeanFielder<PK,D> fielder, boolean flatKey){
		return databeanToJson(databean, fielder, flatKey).toString();
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	JsonArray databeansToJson(Iterable<D> databeans, DatabeanFielder<PK,D> fielder){
		JsonArray array = new JsonArray();
		for(D databean : IterableTool.nullSafe(databeans)){
			array.add(databeanToJson(databean, fielder));
		}
		return array;
	}

	/*--------------------------- pk from json ------------------------------*/

	public static <PK extends PrimaryKey<PK>>
	PK primaryKeyFromJson(Class<PK> pkClass, Fielder<PK> fielder, JsonObject json){
		if(json == null){
			return null;
		}
		PK pk = ReflectionTool.create(pkClass);
		primaryKeyFromJson(pk, fielder, json);
		return pk;
	}

	private static <PK extends PrimaryKey<PK>> void primaryKeyFromJson(PK pk, Fielder<PK> fielder, JsonObject json){
		if(json == null){
			return;
		}
		List<Field<?>> fields = fielder.getFields(pk);
		for(Field<?> field : fields){
			String jsonFieldName = field.getKey().getColumnName();
			JsonElement jsonValue = json.get(jsonFieldName);
			if(jsonValue == null || jsonValue instanceof JsonNull){//PK fields are required
				throw new IllegalStateException(json + " does not contain required key " + jsonFieldName);
			}
			Object value = field.parseStringEncodedValueButDoNotSet(jsonValue.getAsString());
			field.setUsingReflection(pk, value);
		}
	}

	public static <PK extends PrimaryKey<PK>> List<PK> primaryKeysFromJson(Class<PK> pkClass, Fielder<PK> fielder,
			JsonArray json){
		List<PK> pks = new ArrayList<>();
		if(json == null){
			return pks;
		}
		Iterator<JsonElement> iter = json.iterator();
		while(iter.hasNext()){
			JsonObject jsonPk = iter.next().getAsJsonObject();
			PK pk = ReflectionTool.create(pkClass);
			primaryKeyFromJson(pk, fielder, jsonPk);
			pks.add(pk);
		}
		return pks;
	}

	/*------------------------ databean from json ---------------------------*/

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	D databeanFromJson(Supplier<D> databeanSupplier, DatabeanFielder<PK,D> fielder, JsonObject json){
		return databeanFromJson(databeanSupplier, fielder, json, false);
	}

	private static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	D databeanFromJson(Supplier<D> databeanSupplier, DatabeanFielder<PK,D> fielder, JsonObject json, boolean flatKey){
		if(json == null){
			return null;
		}
		D databean = databeanSupplier.get();
		JsonObject pkJson;
		if(flatKey){
			pkJson = json;
		}else{
			pkJson = json.getAsJsonObject(databean.getKeyFieldName());
		}
		primaryKeyFromJson(databean.getKey(), fielder.getKeyFielder(), pkJson);
		List<Field<?>> fields = fielder.getNonKeyFields(databean);
		for(Field<?> field : fields){
			String jsonFieldName = field.getKey().getColumnName();
			JsonElement jsonValue = json.get(jsonFieldName);
			if(jsonValue == null || jsonValue.isJsonNull()){// careful: only skip nulls, not empty strings
				continue;
			}
			String valueString;
			if(jsonValue.isJsonObject()){
				valueString = jsonValue.toString();
			}else{
				valueString = jsonValue.getAsString();
			}
			Object value = field.parseStringEncodedValueButDoNotSet(valueString);
			field.setUsingReflection(databean, value);
		}
		return databean;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	D databeanFromJson(Supplier<D> databeanSupplier, DatabeanFielder<PK,D> fielder, String json){
		return databeanFromJson(databeanSupplier, fielder, json, false);
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	D databeanFromJson(Supplier<D> databeanSupplier, DatabeanFielder<PK,D> fielder, String json, boolean flatKey){
		return databeanFromJson(databeanSupplier, fielder, stringToJsonObject(json), flatKey);
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	List<D> databeansFromJson(Supplier<D> databeanSupplier, DatabeanFielder<PK,D> fielder, JsonArray json){
		List<D> databeans = new ArrayList<>();
		if(json == null){
			return databeans;
		}
		Iterator<JsonElement> iter = json.iterator();
		while(iter.hasNext()){
			JsonObject jsonDatabean = iter.next().getAsJsonObject();
			D databean = databeanFromJson(databeanSupplier, fielder, jsonDatabean);
			databeans.add(databean);
		}
		return databeans;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	List<D> databeansFromJson(Supplier<D> databeanSupplier, DatabeanFielder<PK,D> fielder, String json){
		return databeansFromJson(databeanSupplier, fielder, stringToJsonArray(json));
	}

	/*------------------------------- util ----------------------------------*/

	private static JsonObject stringToJsonObject(String string){
		return jsonParser.parse(string).getAsJsonObject();
	}

	private static JsonArray stringToJsonArray(String string){
		return jsonParser.parse(string).getAsJsonArray();
	}

	public static JsonObject fieldsToJson(List<Field<?>> fields){
		JsonObject jsonObject = new JsonObject();
		addFieldsToJsonObject(jsonObject, fields);
		return jsonObject;
	}

	private static JsonObject addFieldsToJsonObject(JsonObject jsonObject, List<Field<?>> fields){
		for(Field<?> f : IterableTool.nullSafe(fields)){
			jsonObject.addProperty(f.getKey().getColumnName(), f.getStringEncodedValue());
		}
		return jsonObject;
	}

}
