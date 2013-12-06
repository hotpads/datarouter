package com.hotpads.handler.admin;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.meta.DatabeanClassGenerator;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.BooleanArrayField;
import com.hotpads.datarouter.storage.field.imp.array.ByteArrayField;
import com.hotpads.datarouter.storage.field.imp.array.DoubleArrayField;
import com.hotpads.datarouter.storage.field.imp.array.IntegerArrayField;
import com.hotpads.datarouter.storage.field.imp.array.UInt63ArrayField;
import com.hotpads.datarouter.storage.field.imp.array.UInt7ArrayField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.comparable.CharacterField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.ShortField;
import com.hotpads.datarouter.storage.field.imp.comparable.SignedByteField;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateField;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbDoubleField;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbFloatField;
import com.hotpads.datarouter.storage.field.imp.enums.IntegerEnumField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.field.imp.geo.SQuadStringField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt15Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt7Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt8Field;
import com.hotpads.datarouter.storage.field.imp.positive.VarIntField;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.java.ReflectionTool;

public class DatabeanClassGeneratorHandler extends BaseHandler {

	public static List<Class> FIELD_TYPES = ListTool.create();
	static{
		FIELD_TYPES.add(BooleanArrayField.class);
		FIELD_TYPES.add(ByteArrayField.class);
		FIELD_TYPES.add(DoubleArrayField.class);
		FIELD_TYPES.add(IntegerArrayField.class);
		FIELD_TYPES.add(UInt63ArrayField.class);
		FIELD_TYPES.add(UInt7ArrayField.class);
		FIELD_TYPES.add(BooleanField.class);
		FIELD_TYPES.add(CharacterField.class);
		FIELD_TYPES.add(IntegerField.class);
		FIELD_TYPES.add(LongField.class);
		FIELD_TYPES.add(ShortField.class);
		FIELD_TYPES.add(SignedByteField.class);
		FIELD_TYPES.add(LongDateField.class);
		FIELD_TYPES.add(DumbDoubleField.class);
		FIELD_TYPES.add(DumbFloatField.class);
		FIELD_TYPES.add(IntegerEnumField.class);
		FIELD_TYPES.add(StringEnumField.class);
		FIELD_TYPES.add(VarIntField.class);
		FIELD_TYPES.add(SQuadStringField.class);
		FIELD_TYPES.add(UInt15Field.class);
		FIELD_TYPES.add(UInt31Field.class);
		FIELD_TYPES.add(UInt63Field.class);
		FIELD_TYPES.add(UInt7Field.class);
		FIELD_TYPES.add(UInt8Field.class);
		FIELD_TYPES.add(DateField.class);
		FIELD_TYPES.add(StringField.class);
		Collections.sort(FIELD_TYPES, new Comparator<Class>() {
			@Override
			public int compare(Class o1, Class o2) {
				return o1.getSimpleName().compareTo(o2.getSimpleName());
			}
		});
	}
	
	public static final String PARAM_DATABEAN_NAME = "databeanName";
	public static final String PARAM_DATABEAN_PACKAGE = "databeanPackage";
	public static final String PARAM_KEYFIELD_ENUM_TYPE = "keyField_enumType_";
	public static final String PARAM_KEYFIELD_NAME = "keyField_name_";
	public static final String PARAM_KEYFIELD_TYPE = "keyField_type_";
	public static final String PARAM_FIELD_ENUM_TYPE = "field_enumType_";
	public static final String PARAM_FIELD_NAME = "field_name_";
	public static final String PARAM_FIELD_TYPE = "field_type_";
	public static final String PARAM_CREATE_SCRIPT = "script";

	public static final int MAX_KEYFIELDS = 100;
	public static final int MAX_FIELDS = 200;
	
	@Handler
	protected Mav handleDefault() {
		Mav mav = new Mav("/jsp/admin/datarouter/generateJavaClasses.jsp");
		StringBuilder sb = new StringBuilder();
		StringBuilder sb1 = new StringBuilder("FIELD TYPES:\n------------\n");
		for (Class clazz : FIELD_TYPES) {
			sb.append("<option value=\"" + clazz.getSimpleName() + "\">" + clazz.getSimpleName() + "</option>");
			sb1.append(clazz.getSimpleName()+"\n");
		}
		mav.put("fieldTypes", sb.toString());
		mav.put("fieldTypesAsString", sb1.toString());
		return mav;
	}

	@Handler
	protected Mav generateJavaCode() {
		try {
			DataBeanParams databeanParams = new DataBeanParams();
			collectParams(databeanParams);
			String javaCode = databeanParams.getJavaCode();
			out.write(javaCode);
			logger.warn(javaCode);
		} catch (Exception e) {
			e.printStackTrace();
			out.write("failed");
		}
		return null;
	}
	
	@Handler
	protected Mav getDemoScript() {
		try {
			DatabeanClassGenerator g = 
					new DatabeanClassGenerator("ListingCounter");

				g.setPackageName("com.hotpads.marius");
				
				for(Class c: DatabeanClassGeneratorHandler.FIELD_TYPES){
					String genericType = null;
					if(DatabeanClassGenerator.INTEGER_ENUM_FIELDS.contains(c)){
						continue;
					} else if(DatabeanClassGenerator.STRING_ENUM_FIELDS.contains(c)){
						continue;
					} else if(c.equals(UInt8Field.class)){
						continue;
					}
					g.addKeyField(c, StringTool.lowercaseFirstCharacter(c.getSimpleName()) +"DemoKey", genericType);
					g.addField(c, StringTool.lowercaseFirstCharacter(c.getSimpleName())+"Demo", genericType);
				}
				g.addIndex(g.getCsvFieldNames().split(","));
				g.addIndex(g.getCsvKeyFieldNames().split(","));
				g.addIndex((g.getCsvFieldNames() +", " + g.getCsvKeyFieldNames()).split(","));
			g.generateCreateScript();
			String demoScript  = g.getCreateScript();
			out.write(demoScript);
			logger.warn(demoScript);
		} catch (Exception e) {
			e.printStackTrace();
			out.write("failed");
		}
		return null;
	}
	

	private void collectParams(DataBeanParams databeanParams) {
		String createScript = RequestTool.get(request, PARAM_CREATE_SCRIPT, null);
		if(StringTool.notEmpty(createScript)){
			collectParamsFromCreateScript(createScript, databeanParams);
			return;
		} else {
		
			databeanParams.setDataBeanName(StringTool.capitalizeFirstLetter(RequestTool.get(request, PARAM_DATABEAN_NAME, null)));
			databeanParams.setDataBeanPackage(RequestTool.get(request, PARAM_DATABEAN_PACKAGE, null));
	
			for (int i = 0; i < MAX_KEYFIELDS; i++) {
				String keyFieldName = RequestTool.get(request, PARAM_KEYFIELD_NAME + i, null);
				String keyFieldType = RequestTool.get(request, PARAM_KEYFIELD_TYPE + i, null);
				String keyFieldEnumType = RequestTool.get(request, PARAM_KEYFIELD_ENUM_TYPE + i, null);
				databeanParams.addKeyField(keyFieldName, keyFieldType, keyFieldEnumType);
			}
	
			for (int i = 0; i < MAX_FIELDS; i++) {
				String fieldName = RequestTool.get(request, PARAM_FIELD_NAME + i, null);
				String fieldType = RequestTool.get(request, PARAM_FIELD_TYPE + i, null);
				String fieldEnumType = RequestTool.get(request, PARAM_FIELD_ENUM_TYPE + i, null);
				databeanParams.addField(fieldName, fieldType, fieldEnumType);
			}
		}
	}

	private void collectParamsFromCreateScript(String createScript, DataBeanParams databeanParams) {
		boolean isPKField = false;
		for(String line : createScript.split("\n")){
			if(StringTool.isEmpty(line)){
				continue;
			}
			line = line.trim();
			if(line.equalsIgnoreCase("pk{") || line.equalsIgnoreCase("pk {")){
				isPKField = true;
			} else if(isPKField && line.equals("}")){
				isPKField = false;
			} else if(line.endsWith("{")){//package and class name line
				String packageName = "";
				String  className = "";
				if(line.contains(".")){
					packageName = line.substring(0, line.lastIndexOf("."));
					className = line.substring(line.lastIndexOf(".") + 1, line.lastIndexOf("{"));
				} else {
					className = line.substring(0, line.lastIndexOf("{"));
				}
				databeanParams.setDataBeanName(className);
				databeanParams.setDataBeanPackage(packageName);
			} else if (isPKField) { //pk field line
				line = line.replace(",", "");
				databeanParams.addKeyField(line.split(" ")[1], line.split(" ")[0], "");
			} else if(line.startsWith("index(") || line.startsWith("index (")){ //index line
				if(line.contains("(") && line.contains(")")){
					line = line.substring(line.indexOf("(")+1, line.lastIndexOf(")"));
					databeanParams.addIndex(line);
				}
			} else if(line.equals("}")){
				return;//this should be last line of the script
			} else { // non pk field line
				line = line.replace(",", "").trim();
				if(StringTool.isEmpty(line)) {
					continue;
				}
				databeanParams.addField(line.split(" ")[1], line.split(" ")[0], "");
			}
		}
	}

	private static class DataBeanParams {
		String dataBeanName, dataBeanPackage;

		List<String> keyfieldEnumTypes = ListTool.createArrayList();
		List<String> keyFieldNames = ListTool.createArrayList();
		List<String> keyFieldTypes = ListTool.createArrayList();

		List<String> fieldEnumTypes = ListTool.createArrayList();
		List<String> fieldNames = ListTool.createArrayList();
		List<String> fieldTypes = ListTool.createArrayList();
		
		List<String> indexes = ListTool.createArrayList();

		public void addKeyField(String name, String type, String enumType) {
			if (StringTool.isNull(name) || StringTool.isNull(type) || StringTool.isNull(enumType)) {
				return;
			}
			keyfieldEnumTypes.add(enumType);
			keyFieldNames.add(name);
			keyFieldTypes.add(type);
		}

		public void addField(String name, String type, String enumType) {
			if (StringTool.isNull(name) || StringTool.isNull(type) || StringTool.isNull(enumType)) {
				return;
			}
			fieldEnumTypes.add(enumType);
			fieldNames.add(name);
			fieldTypes.add(type);
		}
		
		public void addIndex(String csvIndexFields){
			this.indexes.add(csvIndexFields);
		}

		public void setDataBeanName(String name) {
			this.dataBeanName = name;
		}

		public void setDataBeanPackage(String dataBeanPackage) {
			this.dataBeanPackage = dataBeanPackage;
		}
		
		public String getJavaCode(){
			//return toString();
			
			
			DatabeanClassGenerator generator = new DatabeanClassGenerator(dataBeanName);
			generator.setPackageName(dataBeanPackage);
			for(int i =0; i< keyFieldNames.size(); i++){
					generator.addKeyField(getClassForName(keyFieldTypes.get(i)), keyFieldNames.get(i), keyfieldEnumTypes.get(i));
			}
			
			for(int i =0; i< fieldNames.size(); i++){
				generator.addField(getClassForName(fieldTypes.get(i)), fieldNames.get(i), fieldEnumTypes.get(i));
			}
			
			for(String indexFields : indexes){
				if(StringTool.isEmpty(indexFields)){
					continue;
				}
				indexFields = indexFields.trim();
				generator.addIndex(indexFields.split(","));	
			}
			
			
			StringBuilder javaCode = new StringBuilder();
			javaCode.append(dataBeanName + "~~##~~");
			javaCode.append(generator.toJavaDatabean());
			javaCode.append("\n/****************************************************/");
			javaCode.append(generator.toJavaDatabeanKey());
			return  javaCode.toString();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append("DataBeanName:" + dataBeanName);
			sb.append("\nDataBeanPackage:" + dataBeanPackage);
			sb.append("\n-------------------------");
			for (int i = 0; i < keyfieldEnumTypes.size(); i++) {
				sb.append("\nKeyField[" + i + "].name:" + keyFieldNames.get(i));
				sb.append("\nKeyField[" + i + "].type:" + keyFieldTypes.get(i));
				sb.append("\nKeyField[" + i + "].enumType:" + keyfieldEnumTypes.get(i));
			}

			sb.append("\n-------------------------");
			for (int i = 0; i < fieldEnumTypes.size(); i++) {
				sb.append("\nField[" + i + "].name:" + fieldNames.get(i));
				sb.append("\nField[" + i + "].type:" + fieldTypes.get(i));
				sb.append("\nField[" + i + "].enumType:" + fieldEnumTypes.get(i));
			}

			return sb.toString();
		}
		
	}
	
	public static Map<String, String> simpleClassNameToCanonicalClassName;
	static{
		simpleClassNameToCanonicalClassName = MapTool.createHashMap();
		for(Class<?> field : FIELD_TYPES){
			simpleClassNameToCanonicalClassName.put(field.getSimpleName(), field.getCanonicalName());
		}
	}
	
	public static Class<?> getClassForName(String name){
		Class<?> cls = null;
		try {
			String canonicalName = simpleClassNameToCanonicalClassName.get(name);
			if(StringTool.isEmpty(canonicalName)){
				canonicalName = name;
			}
			cls = Class.forName(canonicalName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return cls;
	}
}
