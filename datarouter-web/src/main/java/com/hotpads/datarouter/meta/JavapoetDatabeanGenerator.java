package com.hotpads.datarouter.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.BooleanArrayField;
import com.hotpads.datarouter.storage.field.imp.array.ByteArrayField;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
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
import com.hotpads.datarouter.storage.field.imp.enums.VarIntEnumField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt15Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt31Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt7Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt8Field;
import com.hotpads.datarouter.storage.field.imp.positive.VarIntField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JavapoetDatabeanGenerator {//TODO move this to services, so that more classes can be discovered correctly?

	private String bean;
	private String key;


	public static List<Class<?>> FIELD_TYPES = new ArrayList<>();
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
		FIELD_TYPES.add(VarIntEnumField.class);
		FIELD_TYPES.add(VarIntField.class);
		FIELD_TYPES.add(UInt15Field.class);
		FIELD_TYPES.add(UInt31Field.class);
		FIELD_TYPES.add(UInt63Field.class);
		FIELD_TYPES.add(UInt7Field.class);
		FIELD_TYPES.add(UInt8Field.class);
		FIELD_TYPES.add(DateField.class);
		FIELD_TYPES.add(StringField.class);
		FIELD_TYPES.add(DelimitedStringArrayField.class);
		Collections.sort(FIELD_TYPES, new Comparator<Class<?>>() {
			@Override
			public int compare(Class<?> o1, Class<?> o2) {
				return o1.getSimpleName().compareTo(o2.getSimpleName());
			}
		});
	}

	//TODO order these less stupidly
	public static final Map<Class<?>, JavapoetFieldDefinition<?>> typeNames = new HashMap<>();
	static {
		typeNames.put(BooleanArrayField.class,
				new JavapoetFieldDefinition<>(List.class, Boolean.class, BooleanArrayField.class));
		typeNames.put(DoubleArrayField.class,
				new JavapoetFieldDefinition<>(List.class, Double.class, DoubleArrayField.class));
		typeNames.put(IntegerArrayField.class,
				new JavapoetFieldDefinition<>(List.class, Integer.class, IntegerArrayField.class));
		typeNames.put(UInt63ArrayField.class,
				new JavapoetFieldDefinition<>(List.class, Long.class, UInt63ArrayField.class));
		typeNames.put(UInt7ArrayField.class,
				new JavapoetFieldDefinition<>(List.class, Byte.class, UInt7ArrayField.class));
		typeNames.put(DelimitedStringArrayField.class,
				new JavapoetFieldDefinition<>(List.class, String.class, DelimitedStringArrayField.class));//TODO correct
		typeNames.put(ByteArrayField.class, new JavapoetFieldDefinition<>(byte[].class, ByteArrayField.class));

		typeNames.put(BooleanField.class, new JavapoetFieldDefinition<>(Boolean.class, BooleanField.class));
		typeNames.put(CharacterField.class, new JavapoetFieldDefinition<>(Character.class, CharacterField.class));
		typeNames.put(IntegerField.class, new JavapoetFieldDefinition<>(Integer.class, IntegerField.class));
		typeNames.put(LongField.class, new JavapoetFieldDefinition<>(Long.class, LongField.class));
		typeNames.put(ShortField.class, new JavapoetFieldDefinition<>(Short.class, ShortField.class));
		typeNames.put(SignedByteField.class, new JavapoetFieldDefinition<>(Byte.class, SignedByteField.class));
		typeNames.put(LongDateField.class, new JavapoetFieldDefinition<>(Date.class, LongDateField.class));
		typeNames.put(DumbDoubleField.class, new JavapoetFieldDefinition<>(Double.class, DumbDoubleField.class));
		typeNames.put(DumbFloatField.class, new JavapoetFieldDefinition<>(Float.class, DumbFloatField.class));
		typeNames.put(VarIntField.class, new JavapoetFieldDefinition<>(Integer.class, VarIntField.class));
		typeNames.put(UInt15Field.class, new JavapoetFieldDefinition<>(Short.class, UInt15Field.class));
		typeNames.put(UInt31Field.class, new JavapoetFieldDefinition<>(Integer.class, UInt31Field.class));
		typeNames.put(UInt63Field.class, new JavapoetFieldDefinition<>(Long.class, UInt63Field.class));
		typeNames.put(UInt7Field.class, new JavapoetFieldDefinition<>(Byte.class, UInt7Field.class));
		typeNames.put(UInt8Field.class, new JavapoetFieldDefinition<>(Byte.class, UInt8Field.class));
		typeNames.put(DateField.class, new JavapoetFieldDefinition<>(Date.class, DateField.class));
		typeNames.put(StringField.class, new JavapoetFieldDefinition<>(String.class, StringField.class));
	}

	private String createScript = "";
	private String packageName;
	private String name;
	private List<FieldDefinition<?>> fieldDefinitions;
	private List<FieldDefinition<?>> keyFieldDefinitions;
	private List<IndexDefinition> indexDefinitions;
	private Set<IndexDefinition> uniqueIndexDefinitions;
	private Map<String, FieldDefinition<?>> nonKeyFieldNameToFieldDefinition;
	private Map<String, FieldDefinition<?>> keyFieldNameToFieldDefinition;

	private static final TypeName FIELD = ParameterizedTypeName.get(ClassName.get(Field.class),
			WildcardTypeName.subtypeOf(Object.class));
	private static final TypeName FIELD_LIST = ParameterizedTypeName.get(ClassName.get(List.class), FIELD);

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
			fields = new LinkedList<>();
			sortedFieldNames = new TreeSet<>();
		}
		@Override
		public String toString(){
			return getCreateScriptString();
		}

		public void add(FieldDefinition<?> field){
			fields.add(field);
			sortedFieldNames.add(field.name);
		}

		public String getCreateScriptString(){
			if(DrCollectionTool.isEmpty(fields)) {
				return null;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("index(");
			List<String> indexedFields = new LinkedList<>();
			for(FieldDefinition<?> field : fields){
				indexedFields.add(field.name);
			}
			sb.append(Joiner.on(", ").join(indexedFields));
			sb.append(")");
			return sb.toString();
		}

		public List<FieldDefinition<?>> getFieldDefinitions(){
			return fields;
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

	private static class NamedFieldDefinition<T> {
		JavapoetFieldDefinition<T> def;
		String name;

		public NamedFieldDefinition(JavapoetFieldDefinition<T> def, String name){
			this.def = def;
			this.name = name;
		}
	}

	private static class JavapoetFieldDefinition<T> {
		TypeName type;//for fields of databean/key
		TypeName fieldType;//for *Field in getFields/getNonKeyFields
		TypeName fieldKeyType;//for *FieldKey in FieldKeys (just fieldType's type name + "Key")
		boolean isEnum;

		private static ClassName getKeyFieldFromField(TypeName field){
			String fieldTypeName = field.toString();
			int index = fieldTypeName.lastIndexOf('.');
			String packageName = index > 0 ? fieldTypeName.substring(0, index): "";
			String keyTypeName = (index > 0 ? fieldTypeName.substring(index + 1) : fieldTypeName) + "Key";
			return ClassName.get(packageName, keyTypeName);
		}

		/**
		 * Use this for non-generic, non-enum fields
		 */
		public JavapoetFieldDefinition(Class<T> type, Class<? extends BaseField<T>> fieldType){
			this.isEnum = false;
			this.type = type.isArray() ? ArrayTypeName.of(type.getComponentType()) : ClassName.get(type);
			this.fieldType = ClassName.get(fieldType);
			this.fieldKeyType = getKeyFieldFromField(this.fieldType);
		}

		/**
		 * Use this for generic fields (collections and arrays)
		 */
		//TODO consider correlating all the generics properly according to Base/KeyedListField
		public JavapoetFieldDefinition(Class<?> type, Class<?> typeGeneric,
				Class<? extends BaseField<T>> fieldType){
			this.isEnum = false;
			this.type = ParameterizedTypeName.get(type, typeGeneric);
			this.fieldType = ClassName.get(fieldType);
			this.fieldKeyType = getKeyFieldFromField(this.fieldType);
		}

		/**
		 * Use this for enum fields
		 */
		public JavapoetFieldDefinition(String enumTypeString, Class<?> fieldType){
			TypeName enumTypeName = ClassName.get("", enumTypeString);
			this.isEnum = true;
			this.type = enumTypeName;
			this.fieldType = ParameterizedTypeName.get(ClassName.get(fieldType), enumTypeName);
			this.fieldKeyType = ParameterizedTypeName.get(getKeyFieldFromField(ClassName.get(fieldType)), enumTypeName);
		}

	}

	private void generate(){

		//TODO things left to add:
		//nicer comments
		//indexes
		//proper indentation?
		//section comments/desired order?
		//proper genericization without warnings
		//line length limiting?
		//more bean constructors? (key only, key + fields)

		//generating the key
		ClassName keyName = ClassName.get(packageName, name + "Key");

		List<NamedFieldDefinition<?>> keyNamedFieldDefinitions = getNamedFieldDefinitions(keyFieldDefinitions);

		List<FieldSpec> keyFieldSpecs = keyNamedFieldDefinitions.stream()
				.map(fieldDef -> buildPrivateField(fieldDef.def.type, fieldDef.name))
				.collect(Collectors.toList());

		TypeSpec keyfieldKeysType = buildFieldKeysClass(keyNamedFieldDefinitions);

		TypeSpec keyType = TypeSpec.classBuilder(keyName)
				.addModifiers(Modifier.PUBLIC)
				.superclass(ParameterizedTypeName.get(ClassName.get(BasePrimaryKey.class), keyName))
				.addFields(keyFieldSpecs)
				.addType(keyfieldKeysType)
				.addMethod(buildGetFieldsMethod(keyNamedFieldDefinitions, keyFieldSpecs, keyfieldKeysType))
				.addMethod(buildDefaultConstructor())
				.addMethod(buildParameterizedConstructor(keyFieldSpecs))
				.addMethods(buildGettersAndSetters(keyFieldSpecs))
				.build();

		JavaFile keyFile = JavaFile.builder(packageName, keyType)
				.build();
		this.key = keyFile.toString();

		//generating the bean
		ClassName beanName = ClassName.get(packageName, name);

		List<NamedFieldDefinition<?>> beanNamedFieldDefinitions = getNamedFieldDefinitions(fieldDefinitions);

		List<FieldSpec> beanFieldSpecs = beanNamedFieldDefinitions.stream()
				.map(fieldDef -> buildPrivateField(fieldDef.def.type, fieldDef.name))
				.collect(Collectors.toList());

		TypeSpec beanFieldKeysType = buildFieldKeysClass(beanNamedFieldDefinitions);

		FieldSpec beanKeyField = FieldSpec.builder(keyName, "key", Modifier.PRIVATE).build();

		//TODO build index (Lookup) classes
		List<TypeSpec> indexTypes = null;
		MethodSpec getNonKeyFieldsMethod = buildGetNonKeyFieldsMethod(beanNamedFieldDefinitions, beanFieldSpecs,
				beanFieldKeysType, beanName);
		//TODO index methods go in here
		TypeSpec fielder = buildFielderClass(name + "Fielder", keyName, beanName, getNonKeyFieldsMethod);

		TypeSpec beanType = TypeSpec.classBuilder(beanName)
				.addModifiers(Modifier.PUBLIC)
				.superclass(ParameterizedTypeName.get(ClassName.get(BaseDatabean.class), keyName, beanName))
				.addField(beanKeyField)
				.addFields(beanFieldSpecs)
				.addType(beanFieldKeysType)
				.addType(fielder)
				//TODO .addTypes(null) index types go in here
				.addMethod(buildDefaultConstructor(beanKeyField, keyName))
				.addMethod(buildParameterizedConstructor(beanFieldSpecs, beanKeyField, keyName, keyFieldSpecs))
				.addMethods(buildKeyMethods(beanKeyField, keyName))
				.addMethods(buildGettersAndSetters(beanFieldSpecs))
				.build();

		JavaFile beanFile = JavaFile.builder(packageName, beanType)
				.build();
		this.bean = beanFile.toString();
	}


	private List<NamedFieldDefinition<?>> getNamedFieldDefinitions(List<FieldDefinition<?>> fieldDefinitions){
		List<NamedFieldDefinition<?>> namedFieldDefs = new ArrayList<>(fieldDefinitions.size());
		for(FieldDefinition<?> field : fieldDefinitions){
			JavapoetFieldDefinition<?> fieldDef = typeNames.get(field.type);
			if (fieldDef == null){
				JavapoetFieldDefinition<?> javapoetDef;
				if (field.type == IntegerEnumField.class || field.type == StringEnumField.class){
					javapoetDef = new JavapoetFieldDefinition<>(field.genericType, field.type);
				} else {
					//TODO handle unknown/mismatches
					javapoetDef = null;
				}
				namedFieldDefs.add(new NamedFieldDefinition<>(javapoetDef, field.name));
			} else {
				namedFieldDefs.add(new NamedFieldDefinition<>(fieldDef, field.name));
			}
		}
		return namedFieldDefs;
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
		@Override
		public String toString(){
			return "private "+type.getSimpleName()+" "+name+";\n";//TODO
		}

		public String getCreateScriptString(){//TODO?
			if(type == null){
				return name + " Field";
			}
			return type.getSimpleName() + (DrStringTool.notEmpty(genericType)?"<"+genericType+">":"") + " "+name;
		}


	}

	public JavapoetDatabeanGenerator(String name){
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
		FieldDefinition<T> fieldDefinition = new FieldDefinition<>(type,name,genericType);
		fieldList.add(fieldDefinition);
		if(isKey){
			keyFieldNameToFieldDefinition.put(name, fieldDefinition);
		} else {
			nonKeyFieldNameToFieldDefinition.put(name, fieldDefinition);
		}
	}
	public <T> void addField(Class<T> type, String name, String genericType){
		addField(type,name,genericType,fieldDefinitions, false);
	}
	public <T> void addKeyField(Class<T> type, String name, String genericType){
		addField(type,name,genericType,keyFieldDefinitions, true);
	}
	public String getCsvKeyFieldNames(){
		return Joiner.on(", " ).join(getFieldsNames(keyFieldDefinitions));
	}

	public String getCsvFieldNames(){
		return Joiner.on(", " ).join(getFieldsNames(fieldDefinitions));
	}

	private List<String> getFieldsNames(Collection<FieldDefinition<?>> fieldDefinitions){
		List<String> fieldName = new LinkedList<>();
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
			indexDefinition.add(new FieldDefinition<>(fieldDefinition.type, fieldName, fieldDefinition.genericType));
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

	public String toJavaDatabean(){//TODO indexes
		if (bean == null){
			generate();
		}
		return bean;
	}

	public void generateCreateScript(){//TODO? also make one to go to mysql?
		JSONObject json = new JSONObject();

		json.accumulate("package", packageName);
		json.accumulate("class", name);

		JSONArray pk = new JSONArray();
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			JSONObject pkField = new JSONObject();
			pkField .accumulate("name", kf.name);
			pkField.accumulate("type", kf.type.getCanonicalName());
			pkField.accumulate("genericType", DrStringTool.nullSafe(kf.genericType));
			pk.add(pkField);
		}

		JSONArray fields = new JSONArray();
		for(FieldDefinition<?> f : fieldDefinitions){
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

	public String toCreateScript(){//TODO? also make one to go to mysql?
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

	//build something like the following
	//private <type> <name>;
	private static FieldSpec buildPrivateField(TypeName type, String name){
		return getFieldBuilder(type, name, Modifier.PRIVATE).build();
	}

	//build something like the following
	//public static final <type> <name> = new <Type>("<name>");
	private static FieldSpec buildInitializedFieldKeyField(NamedFieldDefinition<?> namedFieldDef){
		FieldSpec.Builder builder = getFieldBuilder(
				namedFieldDef.def.fieldKeyType, namedFieldDef.name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
		if (namedFieldDef.def.isEnum){
			return builder
					.initializer("new $T($S, $T.class)", namedFieldDef.def.fieldKeyType, namedFieldDef.name,
							namedFieldDef.def.type)
					.build();
		} else {
			return builder
					.initializer("new $T($S)", namedFieldDef.def.fieldKeyType, namedFieldDef.name)
					.build();
		}
	}

	private static FieldSpec.Builder getFieldBuilder(TypeName type, String name,
			Modifier... modifiers){
		return FieldSpec.builder(type, name, modifiers);
	}

	private static TypeSpec buildFieldKeysClass(List<NamedFieldDefinition<?>> fields){
		TypeSpec.Builder builder = TypeSpec.classBuilder("FieldKeys")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
		fields.stream()
				.forEach(field -> builder.addField(buildInitializedFieldKeyField(field)));
		return builder.build();
	}

	private static TypeSpec buildFielderClass(String name, TypeName keyType, TypeName beanType,
			MethodSpec getNonKeyFieldsMethod){
		return TypeSpec.classBuilder(name)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.superclass(ParameterizedTypeName.get(ClassName.get(BaseDatabeanFielder.class), keyType, beanType))
				.addMethod(buildFielderConstructor(keyType))
				.addMethod(getNonKeyFieldsMethod)
				.build();
	}

	private static MethodSpec buildFielderConstructor(TypeName keyType){
		return MethodSpec.constructorBuilder()
				.addStatement("super($T.class)", keyType)
				.build();
	}

	private static MethodSpec buildGetFieldsMethod(List<NamedFieldDefinition<?>> fieldDefs, List<FieldSpec> fields,
			TypeSpec fieldKeysType) {
		return buildFieldsMethod(fieldDefs, fields, fieldKeysType, null, false);
	}

	private static MethodSpec buildGetNonKeyFieldsMethod(List<NamedFieldDefinition<?>> fieldDefs,
			List<FieldSpec> fields, TypeSpec fieldKeysType, TypeName beanType) {
		return buildFieldsMethod(fieldDefs, fields, fieldKeysType, beanType, true);
	}

	/**
	 * builds one of two very similar but annoyingly different methods: "getNonKeyFields" or "getFields".
	 * If isFielder is true, builds "getNonKeyFields", otherwise builds "getFields".
	 */
	private static MethodSpec buildFieldsMethod(List<NamedFieldDefinition<?>> fieldDefs, List<FieldSpec> fields,
			TypeSpec fieldKeysType, TypeName beanType, boolean isFielder){

		Iterator<NamedFieldDefinition<?>> defIterator = fieldDefs.iterator();
		Iterator<FieldSpec> fieldIterator = fields.iterator();
		Iterator<FieldSpec> fieldKeysFieldIterator = fieldKeysType.fieldSpecs.iterator();

		//each line of the statement has either 5 or 4 substitutions
		List<Object> statementArgs = new ArrayList<>(fields.size() * (isFielder ? 5 : 4) + 1);
		StringBuilder statementString = new StringBuilder("return $T.asList(\n");
		statementArgs.add(Arrays.class);

		//set up databean param only for Fielder method
		ParameterSpec param = isFielder ? ParameterSpec.builder(beanType, "databean").build() : null;

		while(defIterator.hasNext()){
			TypeName fieldType = defIterator.next().def.fieldType;
			FieldSpec field = fieldIterator.next();
			FieldSpec fieldKeysField = fieldKeysFieldIterator.next();

			if (isFielder){
				//basically queueing up the following to add in one big statement
				//new <fieldType>(FieldsKeys.<fieldName>, databean.<fieldName>),
				statementString.append("new $T($N.$N, $N.$N)").append(defIterator.hasNext() ? ",\n" : ")");
				statementArgs.addAll(Arrays.asList(fieldType, fieldKeysType, fieldKeysField, param, field));
			} else {
				//basically queueing up the following to add in one big statement
				//new <fieldType>(FieldsKeys.<fieldName>, <fieldName>),
				statementString.append("new $T($N.$N, $N)").append(defIterator.hasNext() ? ",\n" : ")");
				statementArgs.addAll(Arrays.asList(fieldType, fieldKeysType, fieldKeysField, field));
			}
		}

		MethodSpec.Builder builder = MethodSpec.methodBuilder(isFielder ? "getNonKeyFields" : "getFields")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(FIELD_LIST);
		if (isFielder){
			//set up databean param only for Fielder method
			builder.addParameter(param);
		}
		return builder.addStatement(statementString.toString(), statementArgs.toArray(new Object[0])).build();
	}

	private static MethodSpec buildDefaultConstructor(){
		return buildDefaultConstructor(null, null);
	}

	private static MethodSpec buildDefaultConstructor(FieldSpec keyField, TypeName keyType){
		MethodSpec.Builder builder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
		if (keyField != null){
			builder.addStatement("this.$N = new $T()", keyField, keyType);
		}
		return builder.build();
	}

	private static MethodSpec buildParameterizedConstructor(List<FieldSpec> fields){
		MethodSpec.Builder builder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
		addConstructorParamsAndInitialize(fields, builder);
		return builder.build();
	}

	private static MethodSpec buildParameterizedConstructor(List<FieldSpec> fields, FieldSpec keyField,
			TypeName keyType, List<FieldSpec> keyFields){
		MethodSpec.Builder builder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

		//add passed through params and call key's constructor with them
		List<Object> keyConstructorCallArguments = new ArrayList<>(keyFields.size() + 2);
		StringBuilder keyConstructorCallString = new StringBuilder("this.$N = new $T(");
		keyConstructorCallArguments.addAll(Arrays.asList(keyField, keyType));
		for(Iterator<FieldSpec> iter = keyFields.iterator(); iter.hasNext();){
			FieldSpec field = iter.next();
			ParameterSpec param = ParameterSpec.builder(field.type, field.name).build();
			builder.addParameter(param);
			keyConstructorCallString.append("$N").append(iter.hasNext() ? ", " : ")");
			keyConstructorCallArguments.add(param);
		}
		builder.addStatement(keyConstructorCallString.toString(), keyConstructorCallArguments.toArray(new Object[0]));

		addConstructorParamsAndInitialize(fields, builder);
		return builder.build();
	}

	private static void addConstructorParamsAndInitialize(List<FieldSpec> fields, MethodSpec.Builder builder){
		for(FieldSpec field : fields){
			ParameterSpec param = ParameterSpec.builder(field.type, field.name).build();
			builder.addParameter(param)
					.addStatement("this.$N = $N", field, param);
		}
	}

	private static List<MethodSpec> buildGettersAndSetters(List<FieldSpec> fields){
		List<MethodSpec> methods = new ArrayList<>(fields.size() * 2);

		for(FieldSpec field : fields){
			String methodNameSuffix = DrStringTool.capitalizeFirstLetter(field.name);//TODO replace with non-DR?

			//getter
			methods.add(generateGetterBuilder(field, "get" + methodNameSuffix).build());

			//setter
			ParameterSpec param = ParameterSpec.builder(field.type, field.name).build();
			methods.add(MethodSpec.methodBuilder("set" + methodNameSuffix)
					.addModifiers(Modifier.PUBLIC)
					.returns(TypeName.VOID)
					.addParameter(param)
					.addStatement("this.$N = $N", field, param)
					.build());
		}

		return methods;
	}

	private static Iterable<MethodSpec> buildKeyMethods(FieldSpec beanKeyField, TypeName keyClassName){
		MethodSpec getKeyClass = MethodSpec.methodBuilder("getKeyClass")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(Class.class), keyClassName))
				.addStatement("return $T.class", keyClassName)
				.build();

		MethodSpec getKey = generateGetterBuilder(beanKeyField, "getKey")
				.addAnnotation(Override.class)
				.build();

		return Arrays.asList(getKeyClass, getKey);
	}


	private static MethodSpec.Builder generateGetterBuilder(FieldSpec beanKeyField, String methodName){
		return MethodSpec.methodBuilder(methodName)
				.addModifiers(Modifier.PUBLIC)
				.returns(beanKeyField.type)
				.addStatement("return $N", beanKeyField);
	}

	public String toJavaDatabeanKey(){
		if (key == null) {
			generate();
		}
		return key;
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

	private static final String EMPTY_LINE = "\n";
	private static final String NEW_LINE = EMPTY_LINE;
	private static final String SPACE_TAB = "  ";

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

		public CodeStringBuilder addLine(int numTabs, String line, String tab){
			addTab(numTabs, tab);
			if(DrStringTool.isEmpty(line)){
				addEmptyLine();
				return this;
			}
			sb.append(line + NEW_LINE);
			return this;
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
