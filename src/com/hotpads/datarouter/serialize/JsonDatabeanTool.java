package com.hotpads.datarouter.serialize;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean.ManyFieldTypeBeanFielder;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBeanKey;
import com.hotpads.datarouter.test.node.basic.manyfield.TestEnum;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.java.ReflectionTool;

public class JsonDatabeanTool{
	
	/********************** pk to json *************************/
	
	public static <PK extends PrimaryKey<PK>> 
	JSONObject primaryKeyToJson(PK pk, Fielder<PK> fielder){
		return fieldsToJson(fielder.getFields(pk));
	}
	
	public static <PK extends PrimaryKey<PK>>
	JSONArray primaryKeysToJson(Iterable<PK> pks, Fielder<PK> fielder){
		JSONArray array = new JSONArray();
		for(PK pk : IterableTool.nullSafe(pks)){
			array.add(addFieldsToJsonObject(new JSONObject(), fielder.getFields(pk)));
		}
		return array;
	}

	
	/********************** databean to json *************************/
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	JSONObject databeanToJson(D databean, DatabeanFielder<PK,D> fielder){
		JSONObject j = new JSONObject();
		j.element(databean.getKeyFieldName(), primaryKeyToJson(databean.getKey(), fielder.getKeyFielder()));
		addFieldsToJsonObject(j, fielder.getNonKeyFields(databean));
		return j;
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	JSONArray databeansToJson(Iterable<D> databeans, DatabeanFielder<PK,D> fielder){
		JSONArray array = new JSONArray();
		for(D databean : IterableTool.nullSafe(databeans)){
			array.add(databeanToJson(databean, fielder));
		}
		return array;
	}

	
	/******************** pk from json **************************/
	
	public static <PK extends PrimaryKey<PK>>
	PK primaryKeyFromJson(Class<PK> pkClass, Fielder<PK> fielder, JSONObject json){
		PK pk = ReflectionTool.create(pkClass);
		primaryKeyFromJson(pk, fielder, json);
		return pk;
	}
	
	public static <PK extends PrimaryKey<PK>>
	void primaryKeyFromJson(PK pk, Fielder<PK> fielder, JSONObject json){
		List<Field<?>> fields = fielder.getFields(pk);
		for(Field<?> field : fields){
			String jsonFieldName = field.getColumnName();
			String jsonValueString = json.getString(jsonFieldName);//PK fields are required
			Object value = field.parseStringEncodedValueButDoNotSet(jsonValueString);
			field.setUsingReflection(pk, value);
		}
	}
	
	public static <PK extends PrimaryKey<PK>>
	List<PK> primaryKeysFromJson(Class<PK> pkClass, Fielder<PK> fielder, JSONArray json){
		List<PK> pks = ListTool.createArrayList();
		Iterator<?> iter = json.iterator();
		while(iter.hasNext()){
			JSONObject jsonPk = (JSONObject)iter.next();
			PK pk = ReflectionTool.create(pkClass);
			primaryKeyFromJson(pk, fielder, jsonPk);
			pks.add(pk);
		}
		return pks;
	}
	
	//wrappers
	public static <PK extends PrimaryKey<PK>>
	PK primaryKeyFromJson(Class<PK> pkClass, Fielder<PK> fielder, String json){
		return primaryKeyFromJson(pkClass, fielder, stringToJsonObject(json));
	}
	
	public static <PK extends PrimaryKey<PK>>
	void primaryKeyFromJson(PK pk, Fielder<PK> fielder, String json){
		primaryKeyFromJson(pk, fielder, stringToJsonObject(json));
	}
	
	public static <PK extends PrimaryKey<PK>>
	List<PK> primaryKeysFromJson(Class<PK> pkClass, Fielder<PK> fielder, String json){
		return primaryKeysFromJson(pkClass, fielder, stringToJsonArray(json));
	}
	
	


	/********************** databean from json *************************/

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	D databeanFromJson(Class<D> databeanClass, DatabeanFielder<PK,D> fielder, JSONObject json){
		D databean = ReflectionTool.create(databeanClass);
		JSONObject pkJson = json.getJSONObject(databean.getKeyFieldName());
		primaryKeyFromJson(databean.getKey(), fielder.getKeyFielder(), pkJson);
		List<Field<?>> fields = fielder.getNonKeyFields(databean);
		for(Field<?> field : fields){
			String jsonFieldName = field.getColumnName();
			String jsonValueString = json.optString(jsonFieldName, null);
			if(jsonValueString==null){ continue; }//careful: only skip nulls, not empty strings
			Object value = field.parseStringEncodedValueButDoNotSet(jsonValueString);
			field.setUsingReflection(databean, value);
		}
		return databean;
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	List<D> databeansFromJson(Class<D> databeanClass, DatabeanFielder<PK,D> fielder, JSONArray json){
		List<D> databeans = ListTool.createArrayList();
		Iterator<?> iter = json.iterator();
		while(iter.hasNext()){
			JSONObject jsonDatabean = (JSONObject)iter.next();
			D databean = databeanFromJson(databeanClass, fielder, jsonDatabean);
			databeans.add(databean);
		}
		return databeans;
	}
	
	//wrappers
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	D databeanFromJson(Class<D> databeanClass, DatabeanFielder<PK,D> fielder, String json){
		return databeanFromJson(databeanClass, fielder, stringToJsonObject(json));
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	List<D> databeansFromJson(Class<D> databeanClass, DatabeanFielder<PK,D> fielder, String json){
		return databeansFromJson(databeanClass, fielder, stringToJsonArray(json));
	}
	
	
	/********************** util ****************************/
	
	public static JSONObject stringToJsonObject(String s){
		return (JSONObject)JSONSerializer.toJSON(s);
	}

	public static JSONArray stringToJsonArray(String s){
		return (JSONArray)JSONSerializer.toJSON(s);
	}

	public static JSONObject fieldsToJson(List<Field<?>> fields) {
		JSONObject jsonObject = new JSONObject();
		addFieldsToJsonObject(jsonObject, fields);
		return jsonObject;
	}
	
	private static JSONObject addFieldsToJsonObject(JSONObject jsonObject, List<Field<?>> fields){
		for(Field<?> f : IterableTool.nullSafe(fields)){
			jsonObject.element(f.getColumnName(), f.getStringEncodedValue());
		}
		return jsonObject;
	}
	
	
	/***************** tests **************************/
	
	public static class JsonDatabeanToolTests{
		private ManyFieldTypeBeanFielder fielder = new ManyFieldTypeBeanFielder();
		private SortedBeanFielder sortedBeanFielder = new SortedBeanFielder();
		
		@Test
		public void testRoundTrip(){
			ManyFieldTypeBeanKey keyIn = new ManyFieldTypeBeanKey(12345L);
			JSONObject keyJsonObject = primaryKeyToJson(keyIn, fielder.getKeyFielder());
			System.out.println(keyJsonObject.toString());
			ManyFieldTypeBeanKey keyOut = primaryKeyFromJson(ManyFieldTypeBeanKey.class, fielder.getKeyFielder(), 
					keyJsonObject);
			Assert.assertEquals(keyIn, keyOut);
			
			ManyFieldTypeBean beanIn = new ManyFieldTypeBean(33333L);
			beanIn.setBooleanField(false);
			beanIn.setByteField((byte)-55);
			beanIn.setCharacterField('Z');
			beanIn.setDoubleField(-78.2345);
			beanIn.setFloatField(45.12345f);
			beanIn.setIntegerField(-9876);
			beanIn.setIntEnumField(TestEnum.fish);
			beanIn.setLongDateField(new Date());
			beanIn.setLongField(-87658765876L);
			beanIn.setShortField((short)-30000);
			beanIn.setStringEnumField(TestEnum.beast);
			beanIn.setStringField("_%crazy-string\\asdf");
			beanIn.setVarIntEnumField(TestEnum.cat);
			beanIn.setVarIntField(5555);
			
			JSONObject databeanJson = databeanToJson(beanIn, fielder);
			System.out.println(databeanJson);
			ManyFieldTypeBean beanOut = databeanFromJson(ManyFieldTypeBean.class, fielder, databeanJson);
			Assert.assertTrue(beanIn.equalsAllPersistentFields(beanOut));
		}
		@Test
		public void testMultiRoundTrip(){
			SortedBeanKey key0 = new SortedBeanKey("a", "b", 0, "d");
			SortedBeanKey key1 = new SortedBeanKey("a", "b", 1, "dasdf");
			SortedBeanKey key2 = new SortedBeanKey("a", "basdf", 2, "sdsdsd");
			List<SortedBeanKey> keysIn = ListTool.createArrayList(key0, key1, key2);
			JSONArray jsonKeys = primaryKeysToJson(keysIn, sortedBeanFielder.getKeyFielder());
			System.out.println(jsonKeys);
			List<SortedBeanKey> keysOut = primaryKeysFromJson(SortedBeanKey.class, sortedBeanFielder.getKeyFielder(), 
					jsonKeys);
			Assert.assertEquals(3, CollectionTool.size(keysOut));
			Assert.assertArrayEquals(keysIn.toArray(), keysOut.toArray());
			
			SortedBean bean0 = new SortedBean(key0, "1", 2L, null, 45.67d);
			SortedBean bean1 = new SortedBean(key1, "ert", -987654L, "cheesetoast", -45.67d);
			List<SortedBean> databeansIn = ListTool.createArrayList(bean0, bean1);
			JSONArray jsonDatabeans = databeansToJson(databeansIn, sortedBeanFielder);
			System.out.println(jsonDatabeans);
			List<SortedBean> databeansOut = databeansFromJson(SortedBean.class, sortedBeanFielder, jsonDatabeans);
			Assert.assertEquals(2, CollectionTool.size(databeansOut));
			Assert.assertArrayEquals(databeansIn.toArray(), databeansOut.toArray());
			Assert.assertArrayEquals(keysIn.subList(0,2).toArray(), KeyTool.getKeys(databeansOut).toArray());
			
			
		}
	}
	
}
