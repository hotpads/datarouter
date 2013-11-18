package com.hotpads.datarouter.meta;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.hibernate.annotations.AccessType;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
//import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.ByteArrayField;
import com.hotpads.datarouter.storage.field.imp.array.DoubleArrayField;
import com.hotpads.datarouter.storage.field.imp.array.IntegerArrayField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbDoubleField;
import com.hotpads.datarouter.storage.field.imp.enums.IntegerEnumField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.field.imp.enums.VarIntEnumField;
import com.hotpads.datarouter.storage.field.imp.geo.SQuadStringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.handler.admin.DatabeanClassGeneratorHandler;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.map.SQuad;

public class DatabeanClassGenerator {

	private String createScript = "";
	private String packageName;
	private String name;
	private List<FieldDefinition<?>> fieldDefinitions;
	private List<FieldDefinition<?>> keyFieldDefinitions;
	
	public static Set<Class<?>> INTEGER_ENUM_FIELDS = SetTool.createHashSet();
	static {
		INTEGER_ENUM_FIELDS.add(IntegerEnumField.class);
		INTEGER_ENUM_FIELDS.add(VarIntEnumField.class);
	}
	public static Set<Class<?>> STRING_ENUM_FIELDS = SetTool.createHashSet();
	static{
		STRING_ENUM_FIELDS.add(StringEnumField.class);
	}
	private static Set<Class<?>> ENUM_FIELDS = SetTool.createHashSet();
	static{
		ENUM_FIELDS.addAll(INTEGER_ENUM_FIELDS);
		ENUM_FIELDS.addAll(STRING_ENUM_FIELDS);
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
			String primitiveType = getSuperClassGenericParameterString();
			return "private "+primitiveType+" "+name +";";
		}
		public String getJavaParamName(){
			if(StringTool.isEmpty(name)){
				return name;
			}
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i< name.length(); i++){
				if( i> 0 && name.charAt(i) >= 65 && name.charAt(i) <=90){
					sb.append("_");
				}
				sb.append(name.charAt(i));
			}
			return sb.toString().toUpperCase(); 
		}
		public String getSuperClassGenericParameterString(){
			ParameterizedType superClass = (ParameterizedType) type.getGenericSuperclass();
			int numGenericParams = superClass.getActualTypeArguments().length;
			String primitiveClass = superClass.getActualTypeArguments()[numGenericParams - 1].toString().replace(
					"class ", "");
			System.out.println(type + "================" + primitiveClass);

			if (primitiveClass.contains("<")) {
				// TODO it's fast and ugly. expected input: java.util.List<java.lang.Integer>
				Class<?> firstClass = DatabeanClassGeneratorHandler.getClassForName(primitiveClass.substring(0,
						primitiveClass.indexOf("<")));
				Class<?> secondClass = DatabeanClassGeneratorHandler.getClassForName(primitiveClass.substring(
						primitiveClass.lastIndexOf("<") + 1, primitiveClass.length() - 1));
				return firstClass.getSimpleName() + "<" + secondClass.getSimpleName() + ">";
			}
			Class<?> c = DatabeanClassGeneratorHandler.getClassForName(primitiveClass);
			if (c == null) {
				c = DatabeanClassGeneratorHandler.getClassForName(genericType);
			}

			if (c == null) {
				return StringTool.nullSafe(genericType);// fail
			}
			return c.getSimpleName();
		}
	}
	
	public DatabeanClassGenerator(String name){
		this.name = name;
		this.fieldDefinitions = Lists.newLinkedList();
		this.keyFieldDefinitions = Lists.newLinkedList();
	}
	
	
	private <T> void addField(Class<T> type, String name, String genericType, 
			List<FieldDefinition<?>> fieldList){
		fieldList.add(new FieldDefinition<T>(type,name,genericType));
	}
	public <T> void addKeyField(Class<T> type, String name, String genericType){
		addField(type,name,genericType,keyFieldDefinitions);
	}
	public <T> void addField(Class<T> type, String name, String genericType){
		addField(type,name,genericType,fieldDefinitions);
	}
	
	public String toJavaDatabean(){
		generateCreateScript();
		CodeStringBuilder javaCode = new CodeStringBuilder();
		
		if(StringTool.notEmpty(packageName)){
			javaCode
				.addLine("package " + packageName + ";")
				.addEmptyLine()
				;
		}
		
		/** Imports ***********************************************************/
		javaCode.add(generateImports(fieldDefinitions, keyFieldDefinitions, 
				Entity.class,Id.class,AccessType.class,Field.class, FieldTool.class, BaseDatabeanFielder.class, List.class,
				Date.class,
				BaseDatabean.class));
		javaCode.add(EMPTY_LINE);
		
		/** Class Definition **************************************************/
		String keyClassName = name + "Key";
		javaCode
			.addEmptyLine()
			.addLine("/** CREATE SCRIPT")
			.addLine(getCreateScript())
			.addLine("*/")
			.addLine("@SuppressWarnings(\"serial\")")
			.addLine("@"+Entity.class.getSimpleName())
			.addLine("@"+AccessType.class.getSimpleName()+"(\"field\")")
			.addLine("public class "+name+" extends " +BaseDatabean.class.getSimpleName()+"<"+keyClassName+", "+name+"> {")
			.add(EMPTY_LINE);
		
//		javaCode
//			.addLine(1,"public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;")
//			.add(EMPTY_LINE);
			
		
		/** Fields ************************************************************/
		javaCode
			.addStarCommentLine(1, "fields")
			.add(EMPTY_LINE)
			.addLine(1, "@"+Id.class.getSimpleName())
			.addLine(1, "private "+keyClassName+" key;")
			.add(EMPTY_LINE);
		for(FieldDefinition<?> f : fieldDefinitions){
			javaCode
				.addLine(1,f.getPrimitiveTypeDeclarationString());
		}
		
		if(!CollectionTool.isEmpty(fieldDefinitions)){
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
				nonkeyDefs.add("\t\t\tnew "+ f.type.getSimpleName()+"<"+f.getSuperClassGenericParameterString()+">("+f.getSuperClassGenericParameterString()+".class, F."+f.getJavaParamName()+", "+f.name+", "+MySqlColumnType.class.getSimpleName()+".MAX_LENGTH_VARCHAR)");
			} else  if(INTEGER_ENUM_FIELDS.contains(f.type)){
				nonkeyDefs.add("\t\t\tnew "+ f.type.getSimpleName()+"<"+f.getSuperClassGenericParameterString()+">("+f.getSuperClassGenericParameterString()+".class, F."+f.getJavaParamName()+", "+f.name+")");
			} else if(f.type.equals(StringField.class)){
				nonkeyDefs.add("\t\t\tnew "+ f.type.getSimpleName()+"(F."+f.getJavaParamName()+", " + f.name + ", "+MySqlColumnType.class.getSimpleName()+".MAX_LENGTH_VARCHAR)");
			} else {
				nonkeyDefs.add("\t\t\tnew "+ f.type.getSimpleName()+"(F."+f.getJavaParamName()+", "+f.name+")");	
			}
			
		}
		javaCode
			.add(NEW_LINE)
			.addLine(1, "public static class F {")
			.addLine(2, "public static final String")
			.addLine(Joiner.on("," + NEW_LINE).join(colDefs)+";")
			.addLine(1, "}")
			.add(NEW_LINE)
			.addLine(1, "@Override")
			.addLine(1, "public List<Field<?>> getNonKeyFields(){")
			.addLine(2, "return FieldTool.createList(")
			.addLine(Joiner.on("," + NEW_LINE).join(nonkeyDefs)+");")
			.addLine(1, "}")
			.add(NEW_LINE)
			.addStarCommentLine(1, "fielder")
			.add(NEW_LINE)
			.addLine(1, "public static class "+name+"Fielder")
			.addLine(2, "extends BaseDatabeanFielder<"+keyClassName+", "+name+">{")
			.addLine(2, "@Override")
			.addLine(2, "public Class<"+keyClassName+"> getKeyFielderClass() {")
			.addLine(3, "return "+keyClassName+".class;")
			.addLine(2, "}")
			.add(NEW_LINE)
			.addLine(2, "@Override")		
			.addLine(2, "public List<Field<?>> getNonKeyFields("+name+" "+StringTool.lowercaseFirstCharacter(name)+") {")
			.addLine(3, "return "+StringTool.lowercaseFirstCharacter(name)+".getNonKeyFields();")
			.addLine(2, "}")
			.addLine(1, "}")
			.addEmptyLine()
			;
		/** Constructors ******************************************************/
		List<String> keyArgsList = Lists.newLinkedList();
		for(FieldDefinition<?> kf : keyFieldDefinitions){ 
			keyArgsList.add(kf.name);
		}
		String keyArgs = Joiner.on(", ").join(keyArgsList);
		javaCode
			.addStarCommentLine(1, "constructors")
			.add(NEW_LINE)
			.addLine(1, "public "+name+"(){")
			.addLine(2, "this.key = new "+keyClassName+"();")
			.addLine(1, "}")
			.add(NEW_LINE)
			.addLine(1, "public "+name+"(" +makeMethodArguments(keyFieldDefinitions) + "){")
			.addLine(2, "this.key = new "+keyClassName+"("+keyArgs+");")
			.addLine(1, "}")
			.addEmptyLine();
		
		/** Keyclass methods **************************************************/
		javaCode
			.addStarCommentLine(1, "databean")
			.add(NEW_LINE)
			.addLine(1, "@Override")
			.addLine(1, "public Class<"+keyClassName+"> getKeyClass() {")
			.addLine(2, "return "+keyClassName+".class;")
			.addLine(1, "}")
			.addEmptyLine()
			.addLine(1, "@Override")
			.addLine(1, "public "+keyClassName+" getKey() {")
			.addLine(2, "return this.key;")
			.addLine(1, "}")
			.addEmptyLine()
			.addLine(1, "@Override")
			.addLine(1, "public boolean isFieldAware() {")
			.addLine(2, "return true;")
			.addLine(1, "}")
			.addEmptyLine();
		
		/** Getters and Setters ***********************************************/
		javaCode
			.addStarCommentLine(1, "getters/setters")
			.add(NEW_LINE)
			.addLine(1, "public void setKey("+keyClassName+" key) {")
			.addLine(2, "this.key = key;")
			.addLine(1, "}")
			.addEmptyLine();
		
		for(FieldDefinition<?> f : fieldDefinitions){
			String capField = StringTool.capitalizeFirstLetter(f.name);
			String type = f.getSuperClassGenericParameterString();
			//get
			javaCode
				.addLine(1, "public "+type+" get"+capField+"(){")
				.addLine(2, "return this."+f.name+";")
				.addLine(1,  "}")
				.addEmptyLine()
			//set
				.addLine(1, "public void set"+capField+"("+type+" "+f.name+"){")
				.addLine(2, "this."+f.name+" = "+f.name+";")
				.addLine(1, "}")
				.addEmptyLine();
		}
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			String capField = StringTool.capitalizeFirstLetter(kf.name);
			String type = kf.getSuperClassGenericParameterString();
			//get
			javaCode
				.addLine(1, "public "+type+" get"+capField+"(){")
				.addLine(2, "return this.key.get"+capField+"();")
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
			pkField.accumulate("genericType", StringTool.nullSafe(kf.genericType));
			pk.add(pkField);
		}

		JSONArray fields = new JSONArray();
		for(FieldDefinition<?> f : fieldDefinitions){
			String type = f.getSuperClassGenericParameterString();
			JSONObject field = new JSONObject();
			field .accumulate("name", f.name);
			field.accumulate("type", f.type.getCanonicalName());
			field.accumulate("genericType", StringTool.nullSafe(f.genericType));
			fields.add(field);
		}

		
		json.accumulate("pk",pk);
		json.accumulate("fields",fields);
		
		setCreateScript(json.toString(1));
	}
	public String toJavaDatabeanKey(){
		CodeStringBuilder javaCode = new CodeStringBuilder();
		
		if(StringTool.notEmpty(packageName)){
			javaCode
				.addLine("package " + packageName + ";")
				.addEmptyLine()
				;
		}
		
		/** Imports ***********************************************************/
		javaCode
			.add(generateImports(null,keyFieldDefinitions,
				List.class, Embeddable.class, Date.class,
				Field.class, FieldTool.class, BasePrimaryKey.class))
			.addEmptyLine();		
		
		/** Class Definition **************************************************/
		String keyClassName = name + "Key";
		javaCode
			.addLine("@SuppressWarnings(\"serial\")")
			.addLine("@"+Embeddable.class.getSimpleName())
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
			} else {
				nonkeyDefs.add("\t\t\tnew "+ f.type.getSimpleName()+"(F."+f.getJavaParamName()+", "+f.name+")");	
			}
			
		}
		javaCode
			.addStarCommentLine(1, "fields")
			;
		
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			javaCode
				.addLine("\t"+kf.getPrimitiveTypeDeclarationString())
				;
		}
		javaCode
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
			.addStarCommentLine(1, "constructors")
			.addLine(1, keyClassName+"(){}")
			.addEmptyLine()
			.addLine(1, "public "+keyClassName+"(" + makeMethodArguments(keyFieldDefinitions) + "){")
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
		javaCode.addStarCommentLine(1, "getters/setters");
		
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			String capField = StringTool.capitalizeFirstLetter(kf.name);
			String type = kf.getSuperClassGenericParameterString();
			//get
			javaCode
				.addLine(1, "public "+type+" get"+capField+"(){")
				.addLine(2, "return this." +kf.name+";")
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
	
	private static String makeMethodArguments(Collection<FieldDefinition<?>> fs){
		List<String> args = Lists.newLinkedList();
		for(FieldDefinition<?> f : fs){
			args.add(f.getSuperClassGenericParameterString()+" "+f.name);
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
				: Iterables.concat(CollectionTool.nullSafe(fs),
									CollectionTool.nullSafe(keyFs))){
//			if(fs == null){
//				cannonicalClassNames.add(
//						fieldImpPackage+"."+f.type.getSimpleName()+"");
//			}
			
			String canonical = f.type.getCanonicalName();
			if(canonical.startsWith(String.class.getPackage().getName()))
				continue;
			cannonicalClassNames.add(canonical);
			if(STRING_ENUM_FIELDS.contains(f.type)){
				containsStringEnum = true;
			}
			
			if(SQuadStringField.class.equals(f.type)){
				cannonicalClassNames.add(SQuad.class.getCanonicalName());
			}
			
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
		
		DatabeanClassGenerator g = 
			new DatabeanClassGenerator("ListingCounter");

//		g.addField(IntegerArrayField.class, "active", null);
//		g.addField(DumbDoubleField.class, "ds", null);
//		g.addField(DoubleArrayField.class, "ds", null);
//		g.addField(ByteArrayField.class, "ds", null);
////		g.addField(Boolean.class, "activeFileFF", true);
//		g.addField(DateField.class, "created", null);
//		
//		g.addKeyField(StringField.class, "name", null);

		g.setPackageName("com.hotpads.marius");
		
		for(Class c: DatabeanClassGeneratorHandler.FIELD_TYPES){
			String genericType = null;
			if(INTEGER_ENUM_FIELDS.contains(c)){
				genericType = "com.hotpads.databean.search.feed.enums.FeedReportFormat";
			} else if(STRING_ENUM_FIELDS.contains(c)){
				genericType = "com.hotpads.customer.CustomerType";
			}
			g.addKeyField(c, StringTool.lowercaseFirstCharacter(c.getSimpleName()) +"Cheie", genericType);
			g.addField(c, StringTool.lowercaseFirstCharacter(c.getSimpleName()), genericType);
		}
		
		System.err.println(g.toJavaDatabean());
		System.err.println("\n");
		System.err.println(g.toJavaDatabeanKey());
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
	private static final int maxCommentLine = 120;

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
			addTab(numTabs);
			if(StringTool.isEmpty(line)){
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
			if(StringTool.isEmpty(starComment) || starComment.length() > maxCommentLine - 5){
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
			for (int i = 0; i< numTabs; i++){
				sb.append(TAB);
			}
			return this;
		}
		
		public String build(){
			return sb.toString();
		}
	}
	
}
