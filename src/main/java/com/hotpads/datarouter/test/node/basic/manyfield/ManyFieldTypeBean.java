package com.hotpads.datarouter.test.node.basic.manyfield;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.BooleanArrayField;
import com.hotpads.datarouter.storage.field.imp.array.ByteArrayField;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
import com.hotpads.datarouter.storage.field.imp.array.DoubleArrayField;
import com.hotpads.datarouter.storage.field.imp.array.IntegerArrayField;
import com.hotpads.datarouter.storage.field.imp.array.UInt63ArrayField;
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
import com.hotpads.datarouter.storage.field.imp.positive.VarIntField;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.collections.arrays.LongArray;


@SuppressWarnings("serial")
@Entity()
@AccessType("field")
public class ManyFieldTypeBean extends BaseDatabean<ManyFieldTypeBeanKey,ManyFieldTypeBean>{
	
	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;

	private static final int LEN_STRING_ENUM_FIELD = 20;
	
	/***************************** fields ********************************/
	
	@Id
	private ManyFieldTypeBeanKey key;
	
	private Boolean booleanField;
	private Byte byteField;
	private Short shortField;
	private Integer integerField;
	private Long longField;
	private Float floatField;
	private Double doubleField;
	@Column(columnDefinition="bigint(20)")
	private Date longDateField;
	private Character characterField;
	private String stringField;
	private Integer varIntField;
	@Column(columnDefinition="int")
	private TestEnum intEnumField;
	@Column(columnDefinition="int")
	private TestEnum varIntEnumField;
	@Column(columnDefinition="varchar(" +LEN_STRING_ENUM_FIELD +")")
	private TestEnum stringEnumField;

	@Lob @Column(length=1024)
	private byte[] stringByteField;
	
	@Lob @Column(length=1<<27)
	private byte[] data;
	
	@Lob @Column(length=1<<27)
	private List<Long> longArrayField;
	@Lob @Column(length=1<<27)
	private List<Boolean> booleanArrayField;
	@Lob @Column(length=1<<27)
	private List<Integer> integerArrayField;
	@Lob @Column(length=1<<27)
	private List<Double> doubleArrayField;
	@Lob @Column(length=1<<27)
	private List<String> delimitedStringArrayField;
	
	private String testSchemaUpdateField;
	
	
	public static class F{
		public static final String
			KEY_NAME = "key",
			booleanField = "booleanField",
			byteField = "byteField",
			shortField = "shortField",
			integerField = "integerField",
			longField = "longField",
			floatField = "floatField",
			doubleField = "doubleField",
			longDateField = "longDateField",
			characterField = "characterField",
			stringField = "stringField",
			varIntField = "varIntField",
			intEnumField = "intEnumField",
			varIntEnumField = "varIntEnumField",
			stringEnumField = "stringEnumField",
			stringByteField = "stringByteField",
			data = "data",
			longArrayField = "longArrayField",
			booleanArrayField = "booleanArrayField",
			integerArrayField = "integerArrayField",
			doubleArrayField = "doubleArrayField",
			delimitedStringArrayField = "delimitedStringArrayField",
			testSchemaUpdateField = "testSchemaUpdateField";
	}
	
	
	@Override
	public List<Field<?>> getNonKeyFields(){
		List<Field<?>> fields = ListTool.createLinkedList();
		fields.add(new BooleanField(F.booleanField, booleanField));
		fields.add(new SignedByteField(F.byteField, byteField));
		fields.add(new ShortField(F.shortField, shortField));
		fields.add(new IntegerField(F.integerField, integerField));
		fields.add(new LongField(F.longField, longField));
		fields.add(new DumbFloatField(F.floatField, floatField));
		fields.add(new DumbDoubleField(F.doubleField, doubleField));
		fields.add(new LongDateField(F.longDateField, longDateField));
		fields.add(new CharacterField(F.characterField, characterField));
		fields.add(new StringField(F.stringField, stringField, DEFAULT_STRING_LENGTH));
		fields.add(new VarIntField(F.varIntField, varIntField));
		fields.add(new IntegerEnumField<TestEnum>(TestEnum.class, F.intEnumField, intEnumField));
		fields.add(new VarIntEnumField<TestEnum>(TestEnum.class, F.varIntEnumField, varIntEnumField));
		fields.add(new StringEnumField<TestEnum>(TestEnum.class, F.stringEnumField, stringEnumField, LEN_STRING_ENUM_FIELD));
		fields.add(new ByteArrayField(F.stringByteField, stringByteField));
		fields.add(new ByteArrayField(F.data, data));
		fields.add(new UInt63ArrayField(F.longArrayField, longArrayField));
		fields.add(new BooleanArrayField(F.booleanArrayField, booleanArrayField));
		fields.add(new IntegerArrayField(F.integerArrayField, integerArrayField));
		fields.add(new DoubleArrayField(F.doubleArrayField, doubleArrayField));
		fields.add(new DelimitedStringArrayField(F.delimitedStringArrayField, ",", delimitedStringArrayField));
		fields.add(new StringField(F.testSchemaUpdateField, testSchemaUpdateField, DEFAULT_STRING_LENGTH));
		return fields;
	}
	
	public boolean equalsAllPersistentFields(ManyFieldTypeBean that){
		if(ObjectTool.notEquals(key, that.key)){ return false; }
		if(ObjectTool.notEquals(booleanField, that.booleanField)){ return false; }
		if(ObjectTool.notEquals(byteField, that.byteField)){ return false; }
		if(ObjectTool.notEquals(shortField, that.shortField)){ return false; }
		if(ObjectTool.notEquals(integerField, that.integerField)){ return false; }
		if(ObjectTool.notEquals(longField, that.longField)){ return false; }
		if(ObjectTool.notEquals(floatField, that.floatField)){ return false; }
		if(ObjectTool.notEquals(doubleField, that.doubleField)){ return false; }
		if(ObjectTool.notEquals(longDateField, that.longDateField)){ return false; }
		if(ObjectTool.notEquals(characterField, that.characterField)){ return false; }
		if(ObjectTool.notEquals(stringField, that.stringField)){ return false; }
		if(ObjectTool.notEquals(varIntField, that.varIntField)){ return false; }
		if(ObjectTool.notEquals(intEnumField, that.intEnumField)){ return false; }
		if(ObjectTool.notEquals(varIntEnumField, that.varIntEnumField)){ return false; }
		if(ObjectTool.notEquals(stringEnumField, that.stringEnumField)){ return false; }
		if(ObjectTool.notEquals(stringByteField, that.stringByteField)){ return false; }
		if(ObjectTool.notEquals(data, that.data)){ return false; }
		if(ObjectTool.notEquals(longArrayField, that.longArrayField)){ return false; }
		if(ObjectTool.notEquals(booleanArrayField, that.booleanArrayField)){ return false; }
		if(ObjectTool.notEquals(integerArrayField, that.integerArrayField)){ return false; }
		if(ObjectTool.notEquals(doubleArrayField, that.doubleArrayField)){ return false; }
		if(ObjectTool.notEquals(delimitedStringArrayField, that.delimitedStringArrayField)){ return false; }
		if(ObjectTool.notEquals(testSchemaUpdateField, that.testSchemaUpdateField)){ return false; }
		return true;
	}
	
	public static class ManyFieldTypeBeanFielder extends BaseDatabeanFielder<ManyFieldTypeBeanKey,ManyFieldTypeBean>{
		public ManyFieldTypeBeanFielder(){}
		@Override
		public Class<ManyFieldTypeBeanKey> getKeyFielderClass(){
			return ManyFieldTypeBeanKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(ManyFieldTypeBean d){
			return d.getNonKeyFields();
		}
		@Override
		public Map<String,List<Field<?>>> getIndexes(ManyFieldTypeBean d){
			Map<String,List<Field<?>>> indexesByName = MapTool.createTreeMap();
			indexesByName.put("index_shortInt", FieldTool.createList(
					new ShortField(F.shortField, d.shortField),
					new IntegerField(F.integerField, d.integerField)));
			indexesByName.put("index_stringTestUpdate", FieldTool.createList(
					new StringField(F.stringField, d.stringField, DEFAULT_STRING_LENGTH),
					new StringField(F.testSchemaUpdateField, d.testSchemaUpdateField, DEFAULT_STRING_LENGTH)));
			return indexesByName;
		}
	}

	
	/***************************** constructor **************************************/
		
	public ManyFieldTypeBean(){//no-arg and public
		this.key = new ManyFieldTypeBeanKey();//let the key generate a random value
	}
	
	public ManyFieldTypeBean(Long id){
		this.key = new ManyFieldTypeBeanKey(id);
	}
	
	
	/************************* databean *********************************************/
	
	@Override
	public Class<ManyFieldTypeBeanKey> getKeyClass() {
		return ManyFieldTypeBeanKey.class;
	};
	
	@Override
	public ManyFieldTypeBeanKey getKey(){
		return key;
	}
	
//	@Override
//	public String getKeyFieldName(){
//		//same as default, so not necssary to override
//	}
	
	@Override
	public boolean isFieldAware(){
		return true;
	}
	
	
	/***************************** static methods *****************************/
	
	public static List<ManyFieldTypeBean> filterForStringValue(Collection<ManyFieldTypeBean> ins, String value){
		List<ManyFieldTypeBean> outs = ListTool.createLinkedList();
		for(ManyFieldTypeBean in : IterableTool.nullSafe(ins)){
			if(ObjectTool.equals(in.getStringField(), value)){
				outs.add(in);
			}
		}
		return outs;
	}
	
	
	/***************************** methods ************************************/
	
	public List<Long> appendToLongArrayField(long val){
		if(longArrayField==null){ longArrayField = new LongArray(); }
		longArrayField.add(val);
		return longArrayField;
	}

	public List<Boolean> appendToBooleanArrayField(Boolean val){
		if(booleanArrayField==null){ booleanArrayField = ListTool.create(); }
		booleanArrayField.add(val);
		return booleanArrayField;
	}
	
	public List<Double> appendToDoubleArrayField(Double val){
		if(doubleArrayField==null){ doubleArrayField = ListTool.create(); }
		doubleArrayField.add(val);
		return doubleArrayField;
	}
	
	public List<Integer> appendToIntegerArrayField(Integer val){
		if(integerArrayField==null){ integerArrayField = ListTool.create(); }
		integerArrayField.add(val);
		return integerArrayField;
	}
	
	public List<String> appendToDelimitedStringArrayField(String val){
		if(delimitedStringArrayField==null){ delimitedStringArrayField = ListTool.create(); }
		delimitedStringArrayField.add(val);
		return delimitedStringArrayField;
	}
	
	/***************************** get/set **************************************/
	
	public byte[] getData(){
		return data;
	}

	public void setData(byte[] data){
		this.data = data;
	}

	public void setKey(ManyFieldTypeBeanKey key){
		this.key = key;
	}

	public Long getId(){
		return key.getId();
	}

	public void setId(Long id){
		key.setId(id);
	}

	public Integer getIntegerField(){
		return integerField;
	}

	public void setIntegerField(Integer integerField){
		this.integerField = integerField;
	}

	public Short getShortField(){
		return shortField;
	}

	public void setShortField(Short shortField){
		this.shortField = shortField;
	}

	public Byte getByteField(){
		return byteField;
	}

	public void setByteField(Byte byteField){
		this.byteField = byteField;
	}

	public Long getLongField(){
		return longField;
	}

	public void setLongField(Long longField){
		this.longField = longField;
	}

	public Float getFloatField(){
		return floatField;
	}

	public void setFloatField(Float floatField){
		this.floatField = floatField;
	}

	public Double getDoubleField(){
		return doubleField;
	}

	public void setDoubleField(Double doubleField){
		this.doubleField = doubleField;
	}

	public Character getCharacterField(){
		return characterField;
	}

	public void setCharacterField(Character characterField){
		this.characterField = characterField;
	}

	public String getStringField(){
		return stringField;
	}

	public void setStringField(String stringField){
		this.stringField = stringField;
	}

	public byte[] getStringByteField(){
		return stringByteField;
	}

	public void setStringByteField(byte[] stringByteField){
		this.stringByteField = stringByteField;
	}


	public List<Long> getLongArrayField(){
		return longArrayField;
	}


	public void setLongArrayField(List<Long> longArrayField){
		this.longArrayField = longArrayField;
	}

	public List<Boolean> getBooleanArrayField(){
		return booleanArrayField;
	}


	public void setBooleanArrayField(List<Boolean> booleanArrayField){
		this.booleanArrayField = booleanArrayField;
	}
	
	public List<Double> getDoubleArrayField(){
		return doubleArrayField;
	}


	public void setDoubleArrayField(List<Double> doubleArrayField){
		this.doubleArrayField = doubleArrayField;
	}
	
	public List<Integer> getIntegerArrayField(){
		return integerArrayField;
	}


	public void setIntegerArrayField(List<Integer> integerArrayField){
		this.integerArrayField = integerArrayField;
	}

	public Date getLongDateField(){
		return longDateField;
	}


	public void setLongDateField(Date longDateField){
		this.longDateField = longDateField;
	}

	public Integer getVarIntField(){
		return varIntField;
	}

	public void setVarIntField(Integer varIntField){
		this.varIntField = varIntField;
	}

	public TestEnum getIntEnumField(){
		return intEnumField;
	}

	public void setIntEnumField(TestEnum intEnumField){
		this.intEnumField = intEnumField;
	}


	public TestEnum getVarIntEnumField(){
		return varIntEnumField;
	}


	public void setVarIntEnumField(TestEnum varIntEnumField){
		this.varIntEnumField = varIntEnumField;
	}


	public TestEnum getStringEnumField(){
		return stringEnumField;
	}


	public void setStringEnumField(TestEnum stringEnumField){
		this.stringEnumField = stringEnumField;
	}


	public Boolean getBooleanField(){
		return booleanField;
	}


	public void setBooleanField(Boolean booleanField){
		this.booleanField = booleanField;
	}

	public List<String> getDelimitedStringArrayField(){
		return delimitedStringArrayField;
	}

	public void setDelimitedStringArrayField(List<String> delimitedStringArrayField){
		this.delimitedStringArrayField = delimitedStringArrayField;
	}
	
}