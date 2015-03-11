package com.hotpads.datarouter.meta;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
import com.hotpads.datarouter.storage.field.imp.enums.IntegerEnumField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.field.imp.enums.VarIntEnumField;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.admin.DatabeanGeneratorHandler;
import com.hotpads.handler.admin.DatabeanGeneratorHandler.UnknownField;

public class DatabeanGenerator {

	private String createScript = "";
	private String packageName;
	private String name;
	private List<FieldDefinition<?>> fieldDefinitions;
	private List<FieldDefinition<?>> keyFieldDefinitions;
	private List<IndexDefinition> indexDefinitions;
	private Set<IndexDefinition> uniqueIndexDefinitions;
	private Map<String, FieldDefinition<?>> nonKeyFieldNameToFieldDefinition;
	private Map<String, FieldDefinition<?>> keyFieldNameToFieldDefinition;
	
	public static Set<Class<?>> INTEGER_ENUM_FIELDS = new HashSet<>();
	static {
		INTEGER_ENUM_FIELDS.add(IntegerEnumField.class);
		INTEGER_ENUM_FIELDS.add(VarIntEnumField.class);
	}
	public static Set<Class<?>> STRING_ENUM_FIELDS = new HashSet<>();
	static{
		STRING_ENUM_FIELDS.add(StringEnumField.class);
	}
	private static Set<Class<?>> ENUM_FIELDS = new HashSet<>();
	static{
		ENUM_FIELDS.addAll(INTEGER_ENUM_FIELDS);
		ENUM_FIELDS.addAll(STRING_ENUM_FIELDS);
	}
	
	private static class IndexDefinition{
		List<FieldDefinition<?>> fields; 
		Set<String> sortedFieldNames;
		public IndexDefinition(){
			fields = DrListTool.createLinkedList();
			sortedFieldNames = new TreeSet<>();
		}
		public String toString(){
			return getCreateScriptString();
		}
		
		public void add(FieldDefinition<?> field){
			fields.add(field);
			sortedFieldNames.add(field.name);
		}
		
		public String getCreateScriptString(){
			if(DrCollectionTool.isEmpty(fields))
				return null;
			StringBuilder sb = new StringBuilder();
			sb.append("index(");
			List<String> indexedFields = DrListTool.createLinkedList();
			for(FieldDefinition<?> field : fields){
				indexedFields.add(field.name);
			}
			sb.append(Joiner.on(", ").join(indexedFields));
			sb.append(")");
			return sb.toString();
		}
		
		public String getIndexClassNameString(String databeanClassName){
			if(DrCollectionTool.isEmpty(fields))
				return null;
			StringBuilder sb = new StringBuilder();
			sb.append(databeanClassName + "sBy");
			for(FieldDefinition<?> field : fields){
				sb.append(DrStringTool.capitalizeFirstLetter(field.name));
			}
			sb.append("Lookup");
			return sb.toString();
		}
		
		public List<FieldDefinition<?>> getFieldDefinitions(){
			return fields;
		}
		public String getMySqlNameString() {
			if(DrCollectionTool.isEmpty(fields))
				return null;
			StringBuilder sb = new StringBuilder();
			sb.append("index");
			for(FieldDefinition<?> field : fields){
				sb.append("_" +field.name);
			}
			return sb.toString();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof IndexDefinition)) {
				return false;
			}
			String thisFieldNames = getStringForCompare();
			String otherFieldNames = ((IndexDefinition) obj).getStringForCompare();
			return thisFieldNames.equals(otherFieldNames);
		}

		private String getStringForCompare() {
			StringBuilder thisFieldNames = new StringBuilder();
			for (String fieldName : sortedFieldNames) {
				thisFieldNames.append(fieldName + "_");
			}
			return thisFieldNames.toString();
		}

		@Override
		public int hashCode() {
			return getStringForCompare().hashCode();
		}
	}
	
	private static class FieldDefinition<T>{
		Class<T> type;
		String name;
		String genericType = null;
		
		public FieldDefinition(Class<T> type, String name, String genericType){
			this.type = type;
			this.name = name;
			this.genericType = genericType;
		}
		public String toString(){
			return "private "+type.getSimpleName()+" "+name+";\n";
		}
		public String getPrimitiveTypeDeclarationString(){
			if(DrStringTool.notEmpty(genericType)){
				return "private "+genericType+" "+name +";";
			};
			String primitiveType = getSuperClassGenericParameterString();
			return "private "+primitiveType+" "+name +";";
		}
		
		public String getCreateScriptString(){
			if(type == null){
				return name + " Field";
			}
			return type.getSimpleName() + (DrStringTool.notEmpty(genericType)?"<"+genericType+">":"") + " "+name;
		}
		
		public String getJavaParamName(){
			return DrStringTool.lowercaseFirstCharacter(name);
//			if(StringTool.isEmpty(name)){
//				return name;
//			}
//			StringBuilder sb = new StringBuilder();
//			for(int i = 0; i< name.length(); i++){
//				if( i> 0 && name.charAt(i) >= 65 && name.charAt(i) <=90){
//					sb.append("_");
//				}
//				sb.append(name.charAt(i));
//			}
//			return sb.toString().toUpperCase(); 
		}
		public String getSuperClassGenericParameterString(){
			if(DrStringTool.notEmpty(genericType)){
				return DrStringTool.nullSafe(genericType);
			}
			if(type.getName().compareTo(UnknownField.class.getName()) == 0){
				return type.getSimpleName();
			}
			ParameterizedType superClass = (ParameterizedType) type.getGenericSuperclass();
			int numGenericParams = superClass.getActualTypeArguments().length;
			String primitiveClass = superClass.getActualTypeArguments()[numGenericParams - 1].toString().replace(
					"class ", "");
			//System.out.println(type + "================" + primitiveClass);

			if (primitiveClass.contains("<")) {
				// TODO it's fast and ugly. expected input: java.util.List<java.lang.Integer>
				Class<?> firstClass = DatabeanGeneratorHandler.getClassForName(primitiveClass.substring(0,
						primitiveClass.indexOf("<")));
				Class<?> secondClass = DatabeanGeneratorHandler.getClassForName(primitiveClass.substring(
						primitiveClass.lastIndexOf("<") + 1, primitiveClass.length() - 1));
				return firstClass.getSimpleName() + "<" + secondClass.getSimpleName() + ">";
			}
			Class<?> c = DatabeanGeneratorHandler.getClassForName(primitiveClass);
			if (c == null) {
				c = DatabeanGeneratorHandler.getClassForName(genericType);
			}

			if (c == null) {
				return DrStringTool.nullSafe(genericType);// fail
			}
			return c.getSimpleName();
		}
		
		public String getDefaultValueString(){
			return "null";
		}
	}
	
	public DatabeanGenerator(String name){
		this.name = name;
		this.fieldDefinitions = Lists.newLinkedList();
		this.keyFieldDefinitions = Lists.newLinkedList();
		this.indexDefinitions = Lists.newLinkedList();
		this.uniqueIndexDefinitions = Sets.newHashSet();
		this.nonKeyFieldNameToFieldDefinition = Maps.newHashMap();
		this.keyFieldNameToFieldDefinition = Maps.newHashMap();
	}
	
	
	private <T> void addField(Class<T> type, String name, String genericType, 
			List<FieldDefinition<?>> fieldList, boolean isKey){
		FieldDefinition<T> fieldDefinition = new FieldDefinition<T>(type,name,genericType); 
		fieldList.add(fieldDefinition);
		if(isKey){
			keyFieldNameToFieldDefinition.put(name, fieldDefinition);
		} else {
			nonKeyFieldNameToFieldDefinition.put(name, fieldDefinition);
		}
	}
	public <T> void addKeyField(Class<T> type, String name, String genericType){
		addField(type,name,genericType,keyFieldDefinitions, true);
	}
	public <T> void addField(Class<T> type, String name, String genericType){
		addField(type,name,genericType,fieldDefinitions, false);
	}
	
	public String getCsvKeyFieldNames (){
		return Joiner.on(", " ).join(getFieldsNames(keyFieldDefinitions));
	}
	
	public String getCsvFieldNames (){
		return Joiner.on(", " ).join(getFieldsNames(fieldDefinitions));
	}
	
	private List<String> getFieldsNames(Collection<FieldDefinition<?>> fieldDefinitions){
		List<String> fieldName = DrListTool.createLinkedList();
		for(FieldDefinition<?> fieldDefinition: fieldDefinitions){
			fieldName.add(fieldDefinition.name);
		}
		return fieldName;
	}
	
	public void addIndex(String ...indexFields){
		IndexDefinition indexDefinition = new IndexDefinition();
		for(String indexField : indexFields){
			if(DrStringTool.isEmpty(indexField)){
				continue;
			}
			//indexField example: StringField string
//			String fieldType = indexField.split(" ")[0]; 
//			String fieldName = indexField.split(" ")[1];
			String fieldName = indexField.trim();
			FieldDefinition<?> fieldDefinition = keyFieldNameToFieldDefinition.get(fieldName);
			if(fieldDefinition == null){
				fieldDefinition = nonKeyFieldNameToFieldDefinition.get(fieldName);
			}
			if(fieldDefinition == null){
				return;//drop the index
			}
			indexDefinition.add(new FieldDefinition(fieldDefinition.type, fieldName, fieldDefinition.genericType));
		}
		
		if(DrCollectionTool.notEmpty(indexDefinition.getFieldDefinitions())){
			addIndex(indexDefinition);
		}
	}
	
	private void addIndex(IndexDefinition index){
		if(uniqueIndexDefinitions.contains(index)){
			return;
		}
		indexDefinitions.add(index);
		uniqueIndexDefinitions.add(index);
	}
	
	private boolean isKeyField(String fieldName){
		return keyFieldNameToFieldDefinition.containsKey(fieldName);
	}
	
	public String toJavaDatabean(){
		generateCreateScript();
		CodeStringBuilder javaCode = new CodeStringBuilder();
		
		if(DrStringTool.notEmpty(packageName)){
			javaCode
				.addLine("package " + packageName + ";")
				.addEmptyLine()
				;
		}
		
		/** Imports ***********************************************************/
		javaCode.add(generateImports(fieldDefinitions, keyFieldDefinitions, 
				Field.class, FieldTool.class, BaseDatabeanFielder.class, List.class,
				Date.class,
				BaseDatabean.class, Map.class, DrMapTool.class, BaseLookup.class));
		javaCode.add(EMPTY_LINE);
		
		/** Class Definition **************************************************/
		String keyClassName = name + "Key";
		javaCode
			.addEmptyLine()
			.addLine("/** CREATE SCRIPT")
			.addLine(getCreateScript())
			.addLine("*/")
//			.addLine("@SuppressWarnings(\"serial\")")
//			.addLine("@"+Entity.class.getSimpleName())
//			.addLine("@"+AccessType.class.getSimpleName()+"(\"field\")")
			.addLine("public class "+name+" extends " +BaseDatabean.class.getSimpleName()+"<"+keyClassName+","+name+"> {")
			.add(EMPTY_LINE);
		
//		javaCode
//			.addLine(1,"public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;")
//			.add(EMPTY_LINE);
			
		
		/** Fields ************************************************************/
		javaCode
			.addStarCommentLine(1, "fields")
			.add(EMPTY_LINE)
//			.addLine(1, "@"+Id.class.getSimpleName())
			.addLine(1, "private "+keyClassName+" key;")
			.add(EMPTY_LINE);
		for(FieldDefinition<?> f : fieldDefinitions){
			javaCode
				.addLine(1,f.getPrimitiveTypeDeclarationString());
		}
		
		if(!DrCollectionTool.isEmpty(fieldDefinitions)){
			javaCode
				.add(EMPTY_LINE);	
		}
		
		/** Column Names ******************************************************/
		List<String> colDefs = Lists.newLinkedList();
		for(FieldDefinition<?> f : 	fieldDefinitions){
			colDefs.add("\t\t\t"+f.getJavaParamName()+" = \""+f.name+"\"");
		}
		List<String> nonkeyDefs = Lists.newLinkedList();
		for(FieldDefinition<?> f : 	fieldDefinitions){
			if(STRING_ENUM_FIELDS.contains(f.type)){
				nonkeyDefs.add("\t\t\t\tnew "+ f.type.getSimpleName()+"<"+f.getSuperClassGenericParameterString()+">("+f.getSuperClassGenericParameterString()+".class, F."+f.getJavaParamName()+", d."+f.name+", "+MySqlColumnType.class.getSimpleName()+".MAX_LENGTH_VARCHAR)");
			} else  if(INTEGER_ENUM_FIELDS.contains(f.type)){
				nonkeyDefs.add("\t\t\t\tnew "+ f.type.getSimpleName()+"<"+f.getSuperClassGenericParameterString()+">("+f.getSuperClassGenericParameterString()+".class, F."+f.getJavaParamName()+", d."+f.name+")");
			} else if(StringField.class.equals(f.type)){
				nonkeyDefs.add("\t\t\t\tnew "+ f.type.getSimpleName()+"(F."+f.getJavaParamName()+", d." + f.name + ", "+MySqlColumnType.class.getSimpleName()+".MAX_LENGTH_VARCHAR)");
			} else  if(DelimitedStringArrayField.class.equals(f.type)){
				nonkeyDefs.add("\t\t\t\tnew "+ f.type.getSimpleName()+"(F."+f.getJavaParamName()+", \",\", d."+f.name+")");
			} else {
				nonkeyDefs.add("\t\t\t\tnew "+ f.type.getSimpleName()+"(F."+f.getJavaParamName()+", d."+f.name+")");	
			}
			
		}
		
		javaCode
			.add(NEW_LINE)
			.addStarCommentLine(1, "columns")
			.add(NEW_LINE)
			.addLine(1, "public static class F {")
			.addLine(2, "public static final String")
			.addLine(Joiner.on("," + NEW_LINE).join(colDefs)+";")
			.addLine(1, "}")
			.add(NEW_LINE)
			.addStarCommentLine(1, "fielder")
			.add(EMPTY_LINE)
			.addLine(1, "public static class "+name+"Fielder")
			.addLine(2, "extends BaseDatabeanFielder<"+keyClassName+", "+name+">{")
			.addLine(2, "public "+name+"Fielder(){")
			.addLine(2, "}")
			.addEmptyLine()
			.addLine(2, "@Override")
			.addLine(2, "public Class<"+keyClassName+"> getKeyFielderClass() {")
			.addLine(3, "return "+keyClassName+".class;")
			.addLine(2, "}")
			.add(NEW_LINE)
			.addLine(2, "@Override")
			.addLine(2, "public List<Field<?>> getNonKeyFields("+name+" d){")
			.addLine(3, "return FieldTool.createList(")
			.addLine(Joiner.on("," + NEW_LINE).join(nonkeyDefs)+");")
			.addLine(2, "}")
			.addEmptyLine();
		
		if(DrCollectionTool.notEmpty(indexDefinitions)){
			javaCode
			.addLine(2,"@Override")
			.addLine(2,"public Map<String, List<Field<?>>> getIndexes("+name+" databean){")
			.addLine(3,"Map<String,List<Field<?>>> indexesByName = MapTool.createTreeMap();");
		
		for(IndexDefinition i: indexDefinitions){
			String indexDefinition = "indexesByName.put(\"" + i.getMySqlNameString() + "\", new "
					+ i.getIndexClassNameString(name) + "("+makeMethodArguments(i.getFieldDefinitions())+")" + ".getFields());";
			javaCode.addLine(3, indexDefinition);
		}
			
		javaCode
			.addLine(3,"return indexesByName;")
			.addLine(2,"}")
			.addLine(1, "}")
			.addEmptyLine()
			;	
		} else {
			javaCode
				.addLine(1, "}")
				.addEmptyLine()
				;	
		}
		
		/** Constructors ******************************************************/
		List<String> keyArgsList = Lists.newLinkedList();
		for(FieldDefinition<?> kf : keyFieldDefinitions){ 
			keyArgsList.add(kf.name);
		}
		String keyArgs = Joiner.on(", ").join(keyArgsList);
		javaCode
			.addStarCommentLine(1, "construct")
			.add(EMPTY_LINE)
			.addLine(1, "public "+name+"(){")
			.addLine(2, "this.key = new "+keyClassName+"();")
			.add(NEW_LINE)
			.addLine(1, "}")
			.add(NEW_LINE)
			.addLine(1, "public "+name+"(" +makeMethodParameters(keyFieldDefinitions) + "){")
			.addLine(2, "this.key = new "+keyClassName+"("+keyArgs+");")
			.addLine(1, "}")
			.addEmptyLine();
		
		/** Keyclass methods **************************************************/
		javaCode
			.addStarCommentLine(1, "databean")
			.add(EMPTY_LINE)
			.addLine(1, "@Override")
			.addLine(1, "public Class<"+keyClassName+"> getKeyClass() {")
			.addLine(2, "return "+keyClassName+".class;")
			.addLine(1, "}")
			.addEmptyLine()
			.addLine(1, "@Override")
			.addLine(1, "public "+keyClassName+" getKey() {")
			.addLine(2, "return key;")
			.addLine(1, "}")
			.addEmptyLine();
		
		/** Indexes   *********************************************************/
		if (DrCollectionTool.notEmpty(indexDefinitions)) {
			javaCode
				.addEmptyLine()
				.addStarCommentLine(1, "indexes")
				.addEmptyLine();
		}
		for(IndexDefinition i : indexDefinitions){
			javaCode
				.addLine(1, "public static class " + i.getIndexClassNameString(name)+" extends BaseLookup<"+keyClassName+">{");
			
			for(FieldDefinition<?> f : i.getFieldDefinitions()){
				javaCode.addLine(2, f.getPrimitiveTypeDeclarationString());
			}
			
			javaCode
				.addEmptyLine()
				.addLine(2, "public " + i.getIndexClassNameString(name) + "(" +makeMethodParameters(i.getFieldDefinitions()) + "){");
			
				for(FieldDefinition<?> f : i.getFieldDefinitions()){
					javaCode.addLine(3, "this."+f.name+" = "+f.name+";");
				}
			javaCode	
				.addLine(2, "}")
				.addEmptyLine();
			
			List<String> fDefs = Lists.newLinkedList();
			for(FieldDefinition<?> f : i.getFieldDefinitions()){
				String keyFieldArgument = isKeyField(f.name)?("DEFAULT_KEY_FIELD_NAME, "+keyClassName+"."):"";
				if(STRING_ENUM_FIELDS.contains(f.type)){
					fDefs.add("\t\t\t\tnew "+ f.type.getSimpleName()+"<"+f.getSuperClassGenericParameterString()+">("+f.getSuperClassGenericParameterString()+".class, "+keyFieldArgument+"F."+f.getJavaParamName()+", "+f.name+", "+MySqlColumnType.class.getSimpleName()+".MAX_LENGTH_VARCHAR)");
				} else  if(INTEGER_ENUM_FIELDS.contains(f.type)){
					fDefs.add("\t\t\t\tnew "+ f.type.getSimpleName()+"<"+f.getSuperClassGenericParameterString()+">("+f.getSuperClassGenericParameterString()+".class, "+keyFieldArgument+"F."+f.getJavaParamName()+", "+f.name+")");
				} else if(f.type.equals(StringField.class)){
					fDefs.add("\t\t\t\tnew "+ f.type.getSimpleName()+"("+keyFieldArgument+"F."+f.getJavaParamName()+", " + f.name + ", "+MySqlColumnType.class.getSimpleName()+".MAX_LENGTH_VARCHAR)");
				} else  if(DelimitedStringArrayField.class.equals(f.type)){
					fDefs.add("\t\t\t\tnew "+ f.type.getSimpleName()+"("+keyFieldArgument+"F."+f.getJavaParamName()+", \",\", "+f.name+")");
				} else {
					fDefs.add("\t\t\t\tnew "+ f.type.getSimpleName()+"("+keyFieldArgument+"F."+f.getJavaParamName()+", "+f.name+")");	
				}
				
			}
			javaCode
				.addLine(2, "public List<Field<?>> getFields(){")
				.addLine(3, "return FieldTool.createList(")
				.addLine(Joiner.on("," + NEW_LINE).join(fDefs) + ");")
				.addLine(2, "}")
				.addLine(1, "}")
				.addEmptyLine(); 
	
		}
				
		/** Getters and Setters ***********************************************/
		javaCode
			.addStarCommentLine(1, "get/set")
			.add(EMPTY_LINE)
			.addLine(1, "public void setKey("+keyClassName+" key) {")
			.addLine(2, "this.key = key;")
			.addLine(1, "}")
			.addEmptyLine();
		
		for(FieldDefinition<?> f : fieldDefinitions){
			String capField = DrStringTool.capitalizeFirstLetter(f.name);
			String type = f.getSuperClassGenericParameterString();
			//get
			javaCode
				.addLine(1, "public "+type+" get"+capField+"(){")
				.addLine(2, "return "+f.name+";")
				.addLine(1,  "}")
				.addEmptyLine()
			//set
				.addLine(1, "public void set"+capField+"("+type+" "+f.name+"){")
				.addLine(2, "this."+f.name+" = "+f.name+";")
				.addLine(1, "}")
				.addEmptyLine();
		}
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			String capField = DrStringTool.capitalizeFirstLetter(kf.name);
			String type = kf.getSuperClassGenericParameterString();
			//get
			javaCode
				.addLine(1, "public "+type+" get"+capField+"(){")
				.addLine(2, "return key.get"+capField+"();")
				.addLine(1, "}")
				.addEmptyLine()
			//set
				.addLine(1, "public void set"+capField+"("+type+" "+kf.name+"){")
				.addLine(2, "this.key.set"+capField+"("+kf.name+");")
				.addLine(1, "}")
				.addEmptyLine();
		}
		javaCode.addLine("}");

		return javaCode.build();
	}
	
	public void generateCreateScript(){
		JSONObject json = new JSONObject();
		
		json.accumulate("package", packageName);
		json.accumulate("class", name);
		
		JSONArray pk = new JSONArray();
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			String type = kf.getSuperClassGenericParameterString();
			JSONObject pkField = new JSONObject();
			pkField .accumulate("name", kf.name);
			pkField.accumulate("type", kf.type.getCanonicalName());
			pkField.accumulate("genericType", DrStringTool.nullSafe(kf.genericType));
			pk.add(pkField);
		}

		JSONArray fields = new JSONArray();
		for(FieldDefinition<?> f : fieldDefinitions){
			String type = f.getSuperClassGenericParameterString();
			JSONObject field = new JSONObject();
			field .accumulate("name", f.name);
			field.accumulate("type", f.type.getCanonicalName());
			field.accumulate("genericType", DrStringTool.nullSafe(f.genericType));
			fields.add(field);
		}

		
		json.accumulate("pk",pk);
		json.accumulate("fields",fields);
		
		//setCreateScript(json.toString(1));
		setCreateScript(toCreateScript());
		
	}
	
	public String toCreateScript(){
		CodeStringBuilder createScript = new CodeStringBuilder();
		if(DrStringTool.notEmpty(packageName)){
			createScript
				.addLine(packageName +"."+ name + "{")
				;
		} else {
			createScript
				.addLine(name + "{");
		}
		
		List<String> colDefs = Lists.newLinkedList();
		for(FieldDefinition<?> f : 	keyFieldDefinitions){
			colDefs.add("    " + f.getCreateScriptString());
		}
		createScript
				.addLine(1, "PK{", SPACE_TAB)
				.addLine(Joiner.on("," + NEW_LINE).join(colDefs))
				.addLine(1, "}", SPACE_TAB);
		colDefs = Lists.newLinkedList();
		for(FieldDefinition<?> f : 	fieldDefinitions){
			colDefs.add("  " + f.getCreateScriptString());
		}
		if(DrCollectionTool.notEmpty(indexDefinitions)){
			createScript
				.addLine(Joiner.on("," + NEW_LINE).join(colDefs) +",");	
		} else {
			createScript
				.addLine(Joiner.on("," + NEW_LINE).join(colDefs));
		}
		
				
		colDefs = Lists.newLinkedList();
		for(IndexDefinition f : 	indexDefinitions){
			colDefs.add("  " + f.getCreateScriptString());
		}
		createScript
				.addLine(Joiner.on("," + NEW_LINE).join(colDefs));
		
		createScript
				.addLine("}");
		return createScript.build();
	}
	
	public String toJavaDatabeanKey(){
		CodeStringBuilder javaCode = new CodeStringBuilder();
		
		if(DrStringTool.notEmpty(packageName)){
			javaCode
				.addLine("package " + packageName + ";")
				.addEmptyLine()
				;
		}
		
		/** Imports ***********************************************************/
		javaCode
			.add(generateImports(null,keyFieldDefinitions,
				List.class, Date.class,
				Field.class, FieldTool.class, BasePrimaryKey.class))
			.addEmptyLine();		
		
		/** Class Definition **************************************************/
		String keyClassName = name + "Key";
		javaCode
			.addLine("@SuppressWarnings(\"serial\")")
			//.addLine("@"+Embeddable.class.getSimpleName())
			.addLine("public class "+keyClassName+" extends "
				+BasePrimaryKey.class.getSimpleName()+"<"+keyClassName+"> {")
			.addEmptyLine()
			;
		
		/** Fields ************************************************************/
		List<String> colDefs = Lists.newLinkedList();
		for(FieldDefinition<?> f : keyFieldDefinitions){
			colDefs.add("\t\t\t"+f.getJavaParamName()+" = \""+f.name+"\"");
		}
		List<String> nonkeyDefs = Lists.newLinkedList();
		for(FieldDefinition<?> f : keyFieldDefinitions){
			if(STRING_ENUM_FIELDS.contains(f.type)){
				nonkeyDefs.add("\t\t\tnew "+ f.type.getSimpleName()+"<"+f.getSuperClassGenericParameterString()+">("+f.getSuperClassGenericParameterString()+".class, F."+f.getJavaParamName()+", "+f.name+", "+MySqlColumnType.class.getSimpleName()+".MAX_LENGTH_VARCHAR)");
			} else  if(INTEGER_ENUM_FIELDS.contains(f.type)){
				nonkeyDefs.add("\t\t\tnew "+ f.type.getSimpleName()+"<"+f.getSuperClassGenericParameterString()+">("+f.getSuperClassGenericParameterString()+".class, F."+f.getJavaParamName()+", "+f.name+")");
			} else if(f.type.equals(StringField.class)){
				nonkeyDefs.add("\t\t\tnew "+ f.type.getSimpleName()+"(F."+f.getJavaParamName()+", " + f.name + ", "+MySqlColumnType.class.getSimpleName()+".MAX_LENGTH_VARCHAR)");
			} else  if(DelimitedStringArrayField.class.equals(f.type)){
				nonkeyDefs.add("\t\t\tnew "+ f.type.getSimpleName()+"(F."+f.getJavaParamName()+", \",\", "+f.name+")");
			} else {
				nonkeyDefs.add("\t\t\tnew "+ f.type.getSimpleName()+"(F."+f.getJavaParamName()+", "+f.name+")");	
			}
			
		}
		javaCode
			.addStarCommentLine(1, "fields")
			.add(NEW_LINE)
			;
		
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			javaCode
				.addLine("\t"+kf.getPrimitiveTypeDeclarationString())
				;
		}
		javaCode
			.add(NEW_LINE)
			.addStarCommentLine(1, "columns")
			.add(NEW_LINE)
			.addLine(1, "public static class F {")
			.addLine(2, "public static final String")
			.addLine(Joiner.on(","+ NEW_LINE).join(colDefs)+";")
			.addLine(1, "}")
			.addEmptyLine()
			.addLine(1, "@Override")
			.addLine(1, "public List<Field<?>> getFields(){")
			.addLine(2, "return FieldTool.createList(")
			.addLine(Joiner.on("," + NEW_LINE).join(nonkeyDefs)+");")
			.addLine(1, "}")
			.addEmptyLine();
		
		/** Constructors ******************************************************/
		javaCode
			.addStarCommentLine(1, "construct")
			.add(NEW_LINE)
			.addLine(1, keyClassName+"(){")
			.addLine(1, "}")
			.addEmptyLine()
			.addLine(1, "public "+keyClassName+"(" + makeMethodParameters(keyFieldDefinitions) + "){")
			;
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			javaCode
				.addLine(2, "this."+kf.name+" = "+kf.name+";");
		}
		javaCode
			.addLine(1, "}")
			.addEmptyLine()
			;

		/** Getters and Setters ***********************************************/
		javaCode
			.addStarCommentLine(1, "get/set")
			.add(NEW_LINE);
		
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			String capField = DrStringTool.capitalizeFirstLetter(kf.name);
			String type = kf.getSuperClassGenericParameterString();
			//get
			javaCode
				.addLine(1, "public "+type+" get"+capField+"(){")
				.addLine(2, "return " +kf.name+";")
				.addLine(1, "}")
				.addEmptyLine()
			//set
				.addLine(1, "public void set"+capField+"("+type+" "+kf.name+"){")
				.addLine(2, "this." + kf.name + " = "+kf.name+";")
				.addLine(1, "}")
				.addEmptyLine();
		}
		javaCode
			.addLine("}")
			;
		return javaCode.build();
	}
	
	private static String makeMethodParameters(Collection<FieldDefinition<?>> fs){
		List<String> args = Lists.newLinkedList();
		for(FieldDefinition<?> f : fs){
			args.add(f.getSuperClassGenericParameterString()+" "+f.name);
		}
		return Joiner.on(", ").join(args);
	}
	
	private String makeMethodArguments(List<FieldDefinition<?>> fs) {
		List<String> args = Lists.newLinkedList();
		for(FieldDefinition<?> f : fs){
			args.add("" + f.getDefaultValueString());
		}
		return Joiner.on(", ").join(args);
	}
	
	private static String generateImports(
			Collection<FieldDefinition<?>> fs, 
			Collection<FieldDefinition<?>> keyFs, Class<?>... classes){
		
		SortedSet<String> cannonicalClassNames = Sets.newTreeSet();
		for(Class<?> c : classes){
			cannonicalClassNames.add(c.getCanonicalName());
		}
		
		String fieldImpPackage = StringField.class.getPackage().getName();
		boolean containsStringEnum = false;
		for(FieldDefinition<?> f 
				: Iterables.concat(DrCollectionTool.nullSafe(fs),
									DrCollectionTool.nullSafe(keyFs))){
//			if(fs == null){
//				cannonicalClassNames.add(
//						fieldImpPackage+"."+f.type.getSimpleName()+"");
//			}
			
			String canonical = f.type.getCanonicalName();
			if(canonical.startsWith(String.class.getPackage().getName()) || 
					canonical.equals(UnknownField.class.getCanonicalName())){
				continue;
			}
			cannonicalClassNames.add(canonical);
			if(STRING_ENUM_FIELDS.contains(f.type)){
				containsStringEnum = true;
			}
			
//			if(SQuadStringField.class.equals(f.type)){
//				cannonicalClassNames.add(SQuad.class.getCanonicalName());
//			}
			
			if(f.type.equals(StringField.class)){
				cannonicalClassNames.add(MySqlColumnType.class.getCanonicalName());
			}
		}
		
		if(containsStringEnum){
			cannonicalClassNames.add(MySqlColumnType.class.getCanonicalName());
		}
		
		CodeStringBuilder javaCode  = new CodeStringBuilder();
		String lastPackagePrefix = null;
		for(String canonical : cannonicalClassNames){
			if(DrStringTool.isEmpty(canonical)){
				continue;
			}
			int firstDot = canonical.indexOf('.');
			int secondDot = canonical.indexOf('.', firstDot+1);
			String packagePrefix = 
				canonical.substring(0,secondDot>0?secondDot:firstDot);
			if(lastPackagePrefix!=null 
					&& ! lastPackagePrefix.equals(packagePrefix)){
				javaCode.addEmptyLine();
			}
			lastPackagePrefix = packagePrefix;
			javaCode.addLine("import "+canonical+";");
		}
		
		return javaCode.build();
	}
	
	public static void main(String... args){
		
		DatabeanGenerator g = 
			new DatabeanGenerator("ListingCounter");

//		g.addField(IntegerArrayField.class, "active", null);
//		g.addField(DumbDoubleField.class, "ds", null);
//		g.addField(DoubleArrayField.class, "ds", null);
//		g.addField(ByteArrayField.class, "ds", null);
////		g.addField(Boolean.class, "activeFileFF", true);
//		g.addField(DateField.class, "created", null);
//		
//		g.addKeyField(StringField.class, "name", null);

		g.setPackageName("com.hotpads.marius");
		
		for(Class c: DatabeanGeneratorHandler.FIELD_TYPES){
			String genericType = null;
			if(INTEGER_ENUM_FIELDS.contains(c)){
				genericType = "com.hotpads.databean.search.feed.enums.FeedReportFormat";
			} else if(STRING_ENUM_FIELDS.contains(c)){
				genericType = "com.hotpads.customer.CustomerType";
			}
			g.addKeyField(c, DrStringTool.lowercaseFirstCharacter(c.getSimpleName()) +"Cheie", genericType);
			g.addField(c, DrStringTool.lowercaseFirstCharacter(c.getSimpleName()), genericType);
		}
		
		IndexDefinition index = new IndexDefinition();
		index.add(new FieldDefinition<>(StringField.class, "id", "fdf"));
		index.add(new FieldDefinition<>(StringField.class, "id2", ""));
		g.addIndex(index);
		
		index = new IndexDefinition();
		index.add(new FieldDefinition<>(StringField.class, "id4", "fdf"));
		index.add(new FieldDefinition<>(StringField.class, "id5", ""));
		g.addIndex(index);
		
		g.generateCreateScript();
//		System.out.println(g.getCreateScript());
		System.err.println(g.toJavaDatabean());
//		System.err.println("\n");
//		System.err.println(g.toJavaDatabeanKey());
	}


	public String getPackageName() {
		return packageName;
	}


	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public String getCreateScript() {
		return createScript;
	}


	public void setCreateScript(String createScript) {
		this.createScript = createScript;
	}

	private static final String SPACE = " ";
	private static final String EMPTY_LINE = "\n";
	private static final String NEW_LINE = EMPTY_LINE;
	private static final String TAB = "\t";
	private static final String SPACE_TAB = "  ";
	private static final int maxCommentLine = 74;

	private static class CodeStringBuilder{
		StringBuilder sb ;
		public CodeStringBuilder() {
			sb = new StringBuilder();
		}
		public CodeStringBuilder addLine(String line){
			sb.append(line + NEW_LINE);
			return this;
		}
		
		public CodeStringBuilder addEmptyLine(){
			sb.append(EMPTY_LINE);
			return this;
		}
		
		public CodeStringBuilder addLine(int numTabs, String line){
			return addLine(numTabs, line, TAB);
		}
		
		public CodeStringBuilder addLine(int numTabs, String line, String tab){
			addTab(numTabs, tab);
			if(DrStringTool.isEmpty(line)){
				addEmptyLine();
				return this;
			}
			sb.append(line + NEW_LINE);
			return this;
		}
		
		public CodeStringBuilder add(String codeBlock){
			sb.append(codeBlock);
			return this;
		}
		
		public CodeStringBuilder addStarCommentLine(int numTabs, String starComment){
			addTab(numTabs);
			if(DrStringTool.isEmpty(starComment) || starComment.length() > maxCommentLine - 5){
				starComment = "";
			}
			StringBuilder comments = new StringBuilder();
			comments.append("/** ");
			comments.append(starComment);
			comments.append(SPACE);
			for(int i = 0; i< maxCommentLine -5 -starComment.length(); i++){
				comments.append("*");
			}
			comments.append("*/");
			sb.append(comments.toString() + NEW_LINE);
			return this;
		}
		
		public CodeStringBuilder addTab(int numTabs){
			return addTab(numTabs, TAB);
		}
		
		public CodeStringBuilder addTab(int numTabs, String tab){
			for (int i = 0; i< numTabs; i++){
				sb.append(tab);
			}
			return this;
		}
		
		public String build(){
			return sb.toString();
		}
	}
	
}
