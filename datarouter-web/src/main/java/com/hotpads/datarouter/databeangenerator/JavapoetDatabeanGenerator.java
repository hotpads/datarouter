package com.hotpads.datarouter.databeangenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

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
import com.hotpads.datarouter.storage.field.imp.array.KeyedListField;
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
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
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

//TODO (potential additions):
//find actual class for enum types(not just a Javapoet ClassName without a known package)
//custom ordering of comments and members (currently no comments, since I can't control the order)
//line length limiting (esp. for constructors and other known long lines)
//(MYSQL) add limit of 16 fields per key? also, can't use blobs for keys
//more bean constructors (Key only, Key + non-Key fields)?
public class JavapoetDatabeanGenerator{

	private final String name;
	private final String packageName;
	private final List<NamedFieldDefinition<?>> keyNamedFieldDefinitions;
	private final List<NamedFieldDefinition<?>> beanNamedFieldDefinitions;
	private final List<IndexDefinition> indexes;

	private String bean;
	private String key;

	private static final TypeName
		FIELD = ParameterizedTypeName.get(ClassName.get(Field.class), WildcardTypeName.subtypeOf(Object.class)),
		FIELD_LIST = ParameterizedTypeName.get(ClassName.get(List.class), FIELD);

	private class NamedFieldDefinition<T>{
		JavapoetFieldDefinition def;
		String name;

		public NamedFieldDefinition(JavapoetFieldDefinition def, String name){
			this.def = def;
			this.name = name;
		}
	}

	private class IndexDefinition{
		String name;
		String lookupName;
		List<Integer> fieldIndices;

		public IndexDefinition(String name, String lookupName, List<Integer> fieldIndices){
			this.name = name;
			this.lookupName = lookupName;
			this.fieldIndices = fieldIndices;
		}
	}

	private static class JavapoetFieldDefinition{
		TypeName type;// for fields of databean/key
		TypeName fieldType;// for *Field in getFields/getNonKeyFields
		TypeName fieldKeyType;// for *FieldKey in FieldKeys (just fieldType's type name + "Key")
		boolean isEnum;

		private static ClassName getKeyFieldFromField(TypeName field){
			String fieldTypeName = field.toString();
			int index = fieldTypeName.lastIndexOf('.');
			String packageName = index > 0 ? fieldTypeName.substring(0, index) : "";
			String keyTypeName = (index > 0 ? fieldTypeName.substring(index + 1) : fieldTypeName) + "Key";
			return ClassName.get(packageName, keyTypeName);
		}

		/**
		 * Use this for simple fields
		 */
		public <T> JavapoetFieldDefinition(Class<T> type, Class<? extends BaseField<T>> fieldType){
			this.isEnum = false;
			this.type = type.isArray() ? ArrayTypeName.of(type.getComponentType()) : ClassName.get(type);
			this.fieldType = ClassName.get(fieldType);
			this.fieldKeyType = getKeyFieldFromField(this.fieldType);
		}

		/**
		 * Use this for list fields
		 */
		@SuppressWarnings("rawtypes")
		public JavapoetFieldDefinition(Class<? extends List> type, Class<? extends Comparable<?>> typeGeneric,
				Class<? extends KeyedListField<?,?,?>> fieldType){
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

	public static final Map<Class<?>,JavapoetFieldDefinition> typeNames = new HashMap<>();
	static{
		// lists
		typeNames.put(BooleanArrayField.class, new JavapoetFieldDefinition(List.class, Boolean.class,
				BooleanArrayField.class));
		typeNames.put(DoubleArrayField.class, new JavapoetFieldDefinition(List.class, Double.class,
				DoubleArrayField.class));
		typeNames.put(IntegerArrayField.class, new JavapoetFieldDefinition(List.class, Integer.class,
				IntegerArrayField.class));
		typeNames.put(UInt63ArrayField.class, new JavapoetFieldDefinition(List.class, Long.class,
				UInt63ArrayField.class));
		typeNames.put(UInt7ArrayField.class, new JavapoetFieldDefinition(List.class, Byte.class,
				UInt7ArrayField.class));
		typeNames.put(DelimitedStringArrayField.class, new JavapoetFieldDefinition(List.class, String.class,
				DelimitedStringArrayField.class));
		// simple types
		typeNames.put(ByteArrayField.class, new JavapoetFieldDefinition(byte[].class, ByteArrayField.class));
		typeNames.put(BooleanField.class, new JavapoetFieldDefinition(Boolean.class, BooleanField.class));
		typeNames.put(CharacterField.class, new JavapoetFieldDefinition(Character.class, CharacterField.class));
		typeNames.put(IntegerField.class, new JavapoetFieldDefinition(Integer.class, IntegerField.class));
		typeNames.put(LongField.class, new JavapoetFieldDefinition(Long.class, LongField.class));
		typeNames.put(ShortField.class, new JavapoetFieldDefinition(Short.class, ShortField.class));
		typeNames.put(SignedByteField.class, new JavapoetFieldDefinition(Byte.class, SignedByteField.class));
		typeNames.put(LongDateField.class, new JavapoetFieldDefinition(Date.class, LongDateField.class));
		typeNames.put(DumbDoubleField.class, new JavapoetFieldDefinition(Double.class, DumbDoubleField.class));
		typeNames.put(DumbFloatField.class, new JavapoetFieldDefinition(Float.class, DumbFloatField.class));
		typeNames.put(VarIntField.class, new JavapoetFieldDefinition(Integer.class, VarIntField.class));
		typeNames.put(UInt15Field.class, new JavapoetFieldDefinition(Short.class, UInt15Field.class));
		typeNames.put(UInt31Field.class, new JavapoetFieldDefinition(Integer.class, UInt31Field.class));
		typeNames.put(UInt63Field.class, new JavapoetFieldDefinition(Long.class, UInt63Field.class));
		typeNames.put(UInt7Field.class, new JavapoetFieldDefinition(Byte.class, UInt7Field.class));
		typeNames.put(UInt8Field.class, new JavapoetFieldDefinition(Byte.class, UInt8Field.class));
		typeNames.put(DateField.class, new JavapoetFieldDefinition(Date.class, DateField.class));
		typeNames.put(StringField.class, new JavapoetFieldDefinition(String.class, StringField.class));
	}

	// Map to look up field types when adding fields (contains all field types)
	public static Map<String,Class<?>> fieldTypeClassLookup;
	static{
		fieldTypeClassLookup = new HashMap<>();
		typeNames.keySet().forEach(field -> fieldTypeClassLookup.put(field.getSimpleName(), field));

		for(Class<?> field : Arrays.asList(IntegerEnumField.class, StringEnumField.class, VarIntEnumField.class)){
			fieldTypeClassLookup.put(field.getSimpleName(), field);
		}
	}

	public JavapoetDatabeanGenerator(String name, String packageName){
		this.name = name;
		this.packageName = packageName;
		this.keyNamedFieldDefinitions = new ArrayList<>();
		this.beanNamedFieldDefinitions = new ArrayList<>();
		this.indexes = new ArrayList<>();
	}

	public JavapoetDatabeanGenerator(String createScript){
		// skip blank lines
		int lineNum = 0;
		String[] lines = createScript.split("\n");
		while(lines[lineNum].trim().isEmpty()){
			lineNum++;
		}

		// first line contains name and packageName (this part is just extracted to make sure all fields are
		// initialized)
		String line = lines[lineNum++];
		this.packageName = line.substring(0, line.lastIndexOf("."));
		this.name = line.substring(line.lastIndexOf(".") + 1, line.lastIndexOf("{"));
		this.keyNamedFieldDefinitions = new ArrayList<>();
		this.beanNamedFieldDefinitions = new ArrayList<>();
		this.indexes = new ArrayList<>();

		// handle the rest of the file, now that everything is initialized
		boolean isPkField = false;
		while(lineNum < lines.length){
			line = lines[lineNum++].trim();
			if(DrStringTool.isEmpty(line)){
				continue;
			}
			if("pk{".equalsIgnoreCase(line) || "pk {".equalsIgnoreCase(line)){
				isPkField = true;
			}else if(isPkField && "}".equals(line)){
				isPkField = false;
			}else if(isPkField){ // pk field line
				line = line.replace(",", "");
				String enumType = DrStringTool.getStringSurroundedWith(line, "<", ">");
				if(DrStringTool.notEmpty(enumType)){
					line = line.replace("<" + enumType + ">", "");
				}
				addKeyField(line.split(" ")[0], line.split(" ")[1], enumType);
			}else if(line.startsWith("index(") || line.startsWith("index (")){ // index line
				if(line.contains("(") && line.contains(")")){
					line = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")"));
					addIndex(line.trim());
				}
			}else if("}".equals(line)){
				return;// this should be last line of the script
			}else{ // non pk field line
				line = line.replace(",", "").trim();
				if(DrStringTool.isEmpty(line)){
					continue;
				}
				String enumType = DrStringTool.getStringSurroundedWith(line, "<", ">");
				if(DrStringTool.notEmpty(enumType)){
					line = line.replace("<" + enumType + ">", "");
				}
				addField(line.split(" ")[0], line.split(" ")[1], enumType);
			}
		}
	}

	public <T> void addField(String type, String name, String genericType){
		if(fieldTypeClassLookup.containsKey(type)){
			beanNamedFieldDefinitions.add(makeFieldDef(fieldTypeClassLookup.get(type), name, genericType));
		}
	}

	public <T> void addKeyField(String type, String name, String genericType){
		if(fieldTypeClassLookup.containsKey(type)){
			keyNamedFieldDefinitions.add(makeFieldDef(fieldTypeClassLookup.get(type), name, genericType));
		}
	}

	public void addIndex(String... indexFields){
		StringBuilder indexName = new StringBuilder("index");
		StringBuilder lookupName = new StringBuilder(name + "By");
		List<Integer> fieldIndices = new ArrayList<>();
		for(String fieldName : indexFields){
			fieldName = fieldName.trim();
			if(DrStringTool.isEmpty(fieldName)){
				continue;
			}
			// could be optimized if field list is always sorted or if a map is used instead of a list
			for(int i = 0; i < beanNamedFieldDefinitions.size(); i++){
				NamedFieldDefinition<?> fieldDef = beanNamedFieldDefinitions.get(i);
				if(fieldName.equals(fieldDef.name)){
					indexName.append('_').append(fieldDef.name);
					lookupName.append(DrStringTool.capitalizeFirstLetter(fieldDef.name));
					fieldIndices.add(i);
				}
			}

		}

		if(!fieldIndices.isEmpty()){
			lookupName.append("Lookup");
			indexes.add(new IndexDefinition(indexName.toString(), lookupName.toString(), fieldIndices));
		}
	}

	public String toJavaDatabeanKey(){
		if(key == null){
			generate();
		}
		return key;
	}

	public String toJavaDatabean(){
		if(bean == null){
			generate();
		}
		return bean;
	}

	private static final String INDENT_TWO = "  ";
	private static final String INDENT_FOUR = "    ";

	public String toCreateScript(){
		StringBuilder script = new StringBuilder(packageName + '.' + name + "{\n");
		String indent = INDENT_TWO;

		script.append(indent).append("PK{");
		appendScriptFields(script, INDENT_FOUR, keyNamedFieldDefinitions);
		script.append("}\n");

		appendScriptFields(script, INDENT_TWO, beanNamedFieldDefinitions);

		for(Iterator<IndexDefinition> iter = indexes.iterator(); iter.hasNext();){
			IndexDefinition index = iter.next();
			appendIndex(script, index, INDENT_TWO);
			script.append(iter.hasNext() ? ",\n" : '\n');
		}

		script.append('}');

		return script.toString();
	}

	private void appendIndex(StringBuilder script, IndexDefinition index, String indent){
		script.append(indent + '(');
		for(Iterator<Integer> iter = index.fieldIndices.iterator(); iter.hasNext();){
			Integer fieldIndex = iter.next();
			script.append(beanNamedFieldDefinitions.get(fieldIndex).name);
			if(iter.hasNext()){
				script.append(", ");
			}
		}
		script.append(')');
	}

	private static void appendScriptFields(StringBuilder script, String indent, List<NamedFieldDefinition<?>> fields){
		for(Iterator<NamedFieldDefinition<?>> iter = fields.iterator(); iter.hasNext();){
			NamedFieldDefinition<?> field = iter.next();
			script.append(indent).append(field.def.fieldType.toString()).append(' ').append(field.name).append(iter
					.hasNext() ? ",\n" : "\n");
		}
	}

	private void generate(){
		// generating the key
		ClassName keyName = ClassName.get(packageName, name + "Key");

		List<FieldSpec> keyFieldSpecs = buildFields(keyNamedFieldDefinitions);

		TypeSpec keyfieldKeysType = buildFieldKeysClass(keyNamedFieldDefinitions);

		TypeSpec keyType = TypeSpec.classBuilder(keyName)
				.addModifiers(Modifier.PUBLIC)
				.superclass(ParameterizedTypeName.get(ClassName.get(BasePrimaryKey.class), keyName))
				.addFields(keyFieldSpecs).addType(keyfieldKeysType)
				.addMethod(buildGetFieldsMethod(keyNamedFieldDefinitions, keyFieldSpecs, keyfieldKeysType))
				.addMethod(buildDefaultConstructor())
				.addMethod(buildParameterizedConstructor(keyFieldSpecs))
				.addMethods(buildGettersAndSetters(keyFieldSpecs)).build();

		JavaFile keyFile = JavaFile.builder(packageName, keyType).build();
		this.key = keyFile.toString();

		// generating the bean
		ClassName beanName = ClassName.get(packageName, name);

		List<FieldSpec> beanFieldSpecs = buildFields(beanNamedFieldDefinitions);

		TypeSpec beanFieldKeysType = buildFieldKeysClass(beanNamedFieldDefinitions);

		FieldSpec beanKeyField = FieldSpec.builder(keyName, "key", Modifier.PRIVATE).build();

		List<TypeSpec> indexTypes = generateLookupTypes(keyName, beanFieldKeysType);
		MethodSpec getNonKeyFieldsMethod = buildGetNonKeyFieldsMethod(beanFieldSpecs, beanFieldKeysType, beanName);
		TypeSpec fielder = buildFielderType(keyName, beanName, getNonKeyFieldsMethod, indexTypes);

		TypeSpec.Builder beanBuilder = TypeSpec.classBuilder(beanName)
				.addModifiers(Modifier.PUBLIC)
				.superclass(ParameterizedTypeName.get(ClassName.get(BaseDatabean.class), keyName, beanName))
				.addField(beanKeyField)
				.addFields(beanFieldSpecs)
				.addType(beanFieldKeysType)
				.addType(fielder);
		if(!indexTypes.isEmpty()){
			beanBuilder.addTypes(indexTypes);
		}
		beanBuilder.addMethod(buildDefaultConstructor(beanKeyField, keyName))
				.addMethod(buildParameterizedConstructor(beanFieldSpecs, beanKeyField, keyName, keyFieldSpecs))
				.addMethods(buildKeyMethods(beanKeyField,keyName))
				.addMethods(buildGettersAndSetters(beanFieldSpecs));

		JavaFile beanFile = JavaFile.builder(packageName, beanBuilder.build()).build();
		this.bean = beanFile.toString();
	}

	private static List<FieldSpec> buildFields(List<NamedFieldDefinition<?>> fieldDefs){
		return fieldDefs.stream().map(fieldDef -> buildPrivateField(fieldDef.def.type, fieldDef.name)).collect(
				Collectors.toList());
	}

	private List<TypeSpec> generateLookupTypes(ClassName keyName, TypeSpec fieldKeysType){
		List<TypeSpec> lookupTypes = new ArrayList<>();
		for(IndexDefinition indexDef : indexes){
			List<NamedFieldDefinition<?>> lookupFieldDefs = indexDef.fieldIndices.stream().map(
					beanNamedFieldDefinitions::get).collect(Collectors.toList());
			List<FieldSpec> lookupFieldSpecs = buildFields(lookupFieldDefs);

			lookupTypes.add(TypeSpec.classBuilder(indexDef.lookupName)
					.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
					.superclass(ParameterizedTypeName.get(ClassName.get(BaseLookup.class), keyName))
					.addFields(lookupFieldSpecs)
					.addMethod(buildParameterizedConstructor(lookupFieldSpecs))
					.addMethod(buildGetFieldsMethod(lookupFieldDefs, lookupFieldSpecs, fieldKeysType))
					.build());
		}
		return lookupTypes;
	}

	private NamedFieldDefinition<?> makeFieldDef(Class<?> type, String name, String genericType){
		JavapoetFieldDefinition fieldDef = typeNames.get(type);
		if(fieldDef == null){
			if(type == IntegerEnumField.class || type == StringEnumField.class || type == VarIntEnumField.class){
				fieldDef = new JavapoetFieldDefinition(genericType, type);
			}else{
				return null;
			}
		}
		return new NamedFieldDefinition<>(fieldDef, name);
	}

	// build something like the following
	// private <type> <name>;
	private static FieldSpec buildPrivateField(TypeName type, String name){
		return getFieldBuilder(type, name, Modifier.PRIVATE).build();
	}

	// build something like the following
	// public static final <type> <name> = new <Type>("<name>");
	private static FieldSpec buildInitializedFieldKeyField(NamedFieldDefinition<?> namedFieldDef){
		FieldSpec.Builder builder = getFieldBuilder(namedFieldDef.def.fieldKeyType, namedFieldDef.name, Modifier.PUBLIC,
				Modifier.STATIC, Modifier.FINAL);
		if(namedFieldDef.def.isEnum){
			return builder.initializer("new $T($S, $T.class)", namedFieldDef.def.fieldKeyType, namedFieldDef.name,
					namedFieldDef.def.type).build();
		}else{
			return builder.initializer("new $T($S)", namedFieldDef.def.fieldKeyType, namedFieldDef.name).build();
		}
	}

	private static FieldSpec.Builder getFieldBuilder(TypeName type, String name, Modifier... modifiers){
		return FieldSpec.builder(type, name, modifiers);
	}

	private static TypeSpec buildFieldKeysClass(List<NamedFieldDefinition<?>> fields){
		TypeSpec.Builder builder = TypeSpec.classBuilder("FieldKeys").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
		fields.stream().forEach(field -> builder.addField(buildInitializedFieldKeyField(field)));
		return builder.build();
	}

	private TypeSpec buildFielderType(TypeName keyType, TypeName beanType, MethodSpec getNonKeyFieldsMethod,
			List<TypeSpec> indexTypes){

		TypeSpec.Builder builder = TypeSpec.classBuilder(name + "Fielder")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.superclass(ParameterizedTypeName.get(ClassName.get(BaseDatabeanFielder.class), keyType, beanType))
				.addMethod(buildFielderConstructor(keyType))
				.addMethod(getNonKeyFieldsMethod);
		if(!indexTypes.isEmpty()){
			builder.addMethod(buildGetIndexesMethod(beanType, indexTypes));
		}
		return builder.build();
	}

	private MethodSpec buildGetIndexesMethod(TypeName beanType, List<TypeSpec> lookupTypes){
		TypeName mapType = ParameterizedTypeName.get(ClassName.get(Map.class), TypeName.get(String.class), FIELD_LIST);
		ParameterSpec param = ParameterSpec.builder(beanType, "databean").build();

		MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getIndexes")
				.addModifiers(Modifier.PUBLIC)
				.returns(mapType)
				.addParameter(param)
				.addStatement("$T indexes = new $T<>()", mapType, HashMap.class);

		for(int i = 0; i < lookupTypes.size(); i++){
			TypeSpec lookupType = lookupTypes.get(i);

			StringBuilder statementString = new StringBuilder("indexes.put($S, new $N(");
			List<Object> statementArgs = new ArrayList<>();
			statementArgs.addAll(Arrays.asList(indexes.get(i).name, lookupType));

			for(Iterator<FieldSpec> iter = lookupType.fieldSpecs.iterator(); iter.hasNext();){
				FieldSpec fieldSpec = iter.next();
				statementString.append("$N.$N");
				statementArgs.addAll(Arrays.asList(param, fieldSpec));
				if(iter.hasNext()){
					statementString.append(", ");
				}
			}

			statementString.append(").$N())");
			// 0 is constructor and 1 is getFields (not worth writing reusable method, as this only happens here)
			statementArgs.add(lookupType.methodSpecs.get(1));
			methodBuilder.addStatement(statementString.toString(), statementArgs.toArray(new Object[0]));
		}

		return methodBuilder.addStatement("return indexes").build();
	}

	private static MethodSpec buildFielderConstructor(TypeName keyType){
		return MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).addStatement("super($T.class)", keyType)
				.build();
	}

	private static MethodSpec buildGetFieldsMethod(List<NamedFieldDefinition<?>> fieldDefs, List<FieldSpec> fields,
			TypeSpec fieldKeysType){
		return buildFieldsMethod(fieldDefs, fields, fieldKeysType, null, false);
	}

	private MethodSpec buildGetNonKeyFieldsMethod(List<FieldSpec> fields, TypeSpec fieldKeysType, TypeName beanType){
		return buildFieldsMethod(beanNamedFieldDefinitions, fields, fieldKeysType, beanType, true);
	}

	/**
	 * builds one of two very similar but annoyingly different methods: "getNonKeyFields" or "getFields". If isFielder
	 * is true, builds "getNonKeyFields", otherwise builds "getFields".
	 */
	private static MethodSpec buildFieldsMethod(List<NamedFieldDefinition<?>> fieldDefs, List<FieldSpec> fields,
			TypeSpec fieldKeysType, TypeName beanType, boolean isFielder){

		Iterator<NamedFieldDefinition<?>> defIterator = fieldDefs.iterator();
		Iterator<FieldSpec> fieldIterator = fields.iterator();

		// each line of the statement has either 5 or 4 substitutions
		List<Object> statementArgs = new ArrayList<>(fields.size() * (isFielder ? 5 : 4) + 1);
		StringBuilder statementString = new StringBuilder("return $T.asList(\n");
		statementArgs.add(Arrays.class);

		// set up databean param only for Fielder method
		ParameterSpec param = isFielder ? ParameterSpec.builder(beanType, "databean").build() : null;

		while(defIterator.hasNext()){
			TypeName fieldType = defIterator.next().def.fieldType;
			FieldSpec field = fieldIterator.next();

			if(isFielder){
				// basically queueing up the following to add in one big statement
				// new <fieldType>(FieldsKeys.<fieldName>, databean.<fieldName>),
				statementString.append("new $T($N.$N, $N.$N)").append(defIterator.hasNext() ? ",\n" : ")");
				statementArgs.addAll(Arrays.asList(fieldType, fieldKeysType, field, param, field));
			}else{
				// basically queueing up the following to add in one big statement
				// new <fieldType>(FieldsKeys.<fieldName>, <fieldName>),
				statementString.append("new $T($N.$N, $N)").append(defIterator.hasNext() ? ",\n" : ")");
				statementArgs.addAll(Arrays.asList(fieldType, fieldKeysType, field, field));
			}
		}

		MethodSpec.Builder builder = MethodSpec.methodBuilder(isFielder ? "getNonKeyFields" : "getFields")
				.addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).returns(FIELD_LIST);
		if(isFielder){
			// set up databean param only for Fielder method
			builder.addParameter(param);
		}
		return builder.addStatement(statementString.toString(), statementArgs.toArray(new Object[0])).build();
	}

	private static MethodSpec buildDefaultConstructor(){
		return buildDefaultConstructor(null, null);
	}

	private static MethodSpec buildDefaultConstructor(FieldSpec keyField, TypeName keyType){
		MethodSpec.Builder builder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
		if(keyField != null){
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

		// add passed through params and call key's constructor with them
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
			builder.addParameter(param).addStatement("this.$N = $N", field, param);
		}
	}

	private static List<MethodSpec> buildGettersAndSetters(List<FieldSpec> fields){
		List<MethodSpec> methods = new ArrayList<>(fields.size() * 2);

		for(FieldSpec field : fields){
			String methodNameSuffix = DrStringTool.capitalizeFirstLetter(field.name);

			// getter
			methods.add(generateGetterBuilder(field, "get" + methodNameSuffix).build());

			// setter
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
		MethodSpec getKeyClass = MethodSpec.methodBuilder("getKeyClass").addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(Class.class), keyClassName))
				.addStatement("return $T.class", keyClassName)
				.build();

		MethodSpec getKey = generateGetterBuilder(beanKeyField, "getKey").addAnnotation(Override.class).build();

		return Arrays.asList(getKeyClass, getKey);
	}

	private static MethodSpec.Builder generateGetterBuilder(FieldSpec beanKeyField, String methodName){
		return MethodSpec.methodBuilder(methodName)
				.addModifiers(Modifier.PUBLIC).returns(beanKeyField.type)
				.addStatement("return $N", beanKeyField);
	}

	public String getName(){
		return name;
	}

	public String getPackageName(){
		return packageName;
	}
}
