package com.hotpads.datarouter.serialize;

import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean.ManyFieldTypeBeanFielder;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBeanKey;
import com.hotpads.datarouter.test.node.basic.manyfield.TestEnum;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.java.ReflectionTool;

public class JsonDatabeanTool{
	
	/********************** to json *************************/
	
	public static <PK extends PrimaryKey<PK>> 
	JSONObject primaryKeyToJson(PK pk, Fielder<PK> fielder){
		return fieldsToJson(fielder.getFields(pk));
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	JSONObject databeanToJson(D databean, DatabeanFielder<PK,D> fielder){
		JSONObject j = new JSONObject();
		j.element(databean.getKeyFieldName(), primaryKeyToJson(databean.getKey(), fielder.getKeyFielder()));
		addFieldsToJson(j, fielder.getNonKeyFields(databean));
		return j;
	}

	
	/******************** from json **************************/
	
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
	
	
	/********************** util ****************************/

	public static JSONObject fieldsToJson(List<Field<?>> fields) {
		JSONObject jsonObject = new JSONObject();
		addFieldsToJson(jsonObject, fields);
		return jsonObject;
	}
	
	private static void addFieldsToJson(JSONObject jsonObject, List<Field<?>> fields){
		for(Field<?> f : IterableTool.nullSafe(fields)){
			jsonObject.element(f.getColumnName(), f.getStringEncodedValue());
		}
	}
	
	
	/***************** tests **************************/
	
	public static class JsonDatabeanToolTests{
		private ManyFieldTypeBeanFielder fielder = new ManyFieldTypeBeanFielder();
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
	}
	
}
