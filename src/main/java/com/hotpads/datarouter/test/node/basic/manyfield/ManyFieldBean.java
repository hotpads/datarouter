package com.hotpads.datarouter.test.node.basic.manyfield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.field.imp.positive.VarIntField;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.util.core.collections.arrays.LongArray;


@Entity()
@AccessType("field")
public class ManyFieldBean extends BaseDatabean<ManyFieldBeanKey,ManyFieldBean>{
	
	public static final int DEFAULT_STRING_LENGTH = MySqlColumnType.MAX_LENGTH_VARCHAR;

	private static final int LEN_STRING_ENUM_FIELD = 20;
	
	/***************************** fields ********************************/
	
	@Id
	private ManyFieldBeanKey key;
	
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
	private Long incrementField;
	
	
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
			testSchemaUpdateField = "testSchemaUpdateField",
			incrementField = "incrementField";
	}
	
	public boolean equalsAllPersistentFields(ManyFieldBean that){
		if(DrObjectTool.notEquals(key, that.key)){ return false; }
		if(DrObjectTool.notEquals(booleanField, that.booleanField)){ return false; }
		if(DrObjectTool.notEquals(byteField, that.byteField)){ return false; }
		if(DrObjectTool.notEquals(shortField, that.shortField)){ return false; }
		if(DrObjectTool.notEquals(integerField, that.integerField)){ return false; }
		if(DrObjectTool.notEquals(longField, that.longField)){ return false; }
		if(DrObjectTool.notEquals(floatField, that.floatField)){ return false; }
		if(DrObjectTool.notEquals(doubleField, that.doubleField)){ return false; }
		if(DrObjectTool.notEquals(longDateField, that.longDateField)){ return false; }
		if(DrObjectTool.notEquals(characterField, that.characterField)){ return false; }
		if(DrObjectTool.notEquals(stringField, that.stringField)){ return false; }
		if(DrObjectTool.notEquals(varIntField, that.varIntField)){ return false; }
		if(DrObjectTool.notEquals(intEnumField, that.intEnumField)){ return false; }
		if(DrObjectTool.notEquals(varIntEnumField, that.varIntEnumField)){ return false; }
		if(DrObjectTool.notEquals(stringEnumField, that.stringEnumField)){ return false; }
		if(DrObjectTool.notEquals(stringByteField, that.stringByteField)){ return false; }
		if(DrObjectTool.notEquals(data, that.data)){ return false; }
		if(DrObjectTool.notEquals(longArrayField, that.longArrayField)){ return false; }
		if(DrObjectTool.notEquals(booleanArrayField, that.booleanArrayField)){ return false; }
		if(DrObjectTool.notEquals(integerArrayField, that.integerArrayField)){ return false; }
		if(DrObjectTool.notEquals(doubleArrayField, that.doubleArrayField)){ return false; }
		if(DrObjectTool.notEquals(delimitedStringArrayField, that.delimitedStringArrayField)){ return false; }
		if(DrObjectTool.notEquals(testSchemaUpdateField, that.testSchemaUpdateField)){ return false; }
		if(DrObjectTool.notEquals(incrementField, that.incrementField)){ return false; }
		return true;
	}
	
	public static class ManyFieldTypeBeanFielder extends BaseDatabeanFielder<ManyFieldBeanKey,ManyFieldBean>{
		public ManyFieldTypeBeanFielder(){}
		@Override
		public Class<ManyFieldBeanKey> getKeyFielderClass(){
			return ManyFieldBeanKey.class;
		}
		@Override
		public List<Field<?>> getNonKeyFields(ManyFieldBean d){
			List<Field<?>> fields = new ArrayList<>();
			fields.add(new BooleanField(F.booleanField, d.booleanField));
			fields.add(new SignedByteField(F.byteField, d.byteField));
			fields.add(new ShortField(F.shortField, d.shortField));
			fields.add(new IntegerField(F.integerField, d.integerField));
			fields.add(new LongField(F.longField, d.longField));
			fields.add(new DumbFloatField(F.floatField, d.floatField));
			fields.add(new DumbDoubleField(F.doubleField, d.doubleField));
			fields.add(new LongDateField(F.longDateField, d.longDateField));
			fields.add(new CharacterField(F.characterField, d.characterField));
			fields.add(new StringField(F.stringField, d.stringField, DEFAULT_STRING_LENGTH));
			fields.add(new VarIntField(F.varIntField, d.varIntField));
			fields.add(new IntegerEnumField<TestEnum>(TestEnum.class, F.intEnumField, d.intEnumField));
			fields.add(new VarIntEnumField<TestEnum>(TestEnum.class, F.varIntEnumField, d.varIntEnumField));
			fields.add(new StringEnumField<TestEnum>(TestEnum.class, F.stringEnumField, d.stringEnumField, LEN_STRING_ENUM_FIELD));
			fields.add(new ByteArrayField(F.stringByteField, d.stringByteField));
			fields.add(new ByteArrayField(F.data, d.data));
			fields.add(new UInt63ArrayField(F.longArrayField, d.longArrayField));
			fields.add(new BooleanArrayField(F.booleanArrayField, d.booleanArrayField));
			fields.add(new IntegerArrayField(F.integerArrayField, d.integerArrayField));
			fields.add(new DoubleArrayField(F.doubleArrayField, d.doubleArrayField));
			fields.add(new DelimitedStringArrayField(F.delimitedStringArrayField, ",", d.delimitedStringArrayField));
			fields.add(new StringField(F.testSchemaUpdateField, d.testSchemaUpdateField, DEFAULT_STRING_LENGTH));
			fields.add(new UInt63Field(F.incrementField, d.incrementField));
			return fields;
		}
		@Override
		public Map<String,List<Field<?>>> getIndexes(ManyFieldBean d){
			Map<String,List<Field<?>>> indexesByName = new TreeMap<>();
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
		
	public ManyFieldBean(){//no-arg and public
		this.key = new ManyFieldBeanKey();//let the key generate a random value
	}
	
	public ManyFieldBean(Long id){
		this.key = new ManyFieldBeanKey(id);
	}
	
	
	/************************* databean *********************************************/
	
	@Override
	public Class<ManyFieldBeanKey> getKeyClass() {
		return ManyFieldBeanKey.class;
	};
	
	@Override
	public ManyFieldBeanKey getKey(){
		return key;
	}
	
//	@Override
//	public String getKeyFieldName(){
//		//same as default, so not necssary to override
//	}
	
	
	/***************************** static methods *****************************/
	
	public static List<ManyFieldBean> filterForStringValue(Collection<ManyFieldBean> ins, String value){
		List<ManyFieldBean> outs = new LinkedList<>();
		for(ManyFieldBean in : DrIterableTool.nullSafe(ins)){
			if(DrObjectTool.equals(in.getStringField(), value)){
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
		if(booleanArrayField==null){ booleanArrayField = new ArrayList<>(); }
		booleanArrayField.add(val);
		return booleanArrayField;
	}
	
	public List<Double> appendToDoubleArrayField(Double val){
		if(doubleArrayField==null){ doubleArrayField = new ArrayList<>(); }
		doubleArrayField.add(val);
		return doubleArrayField;
	}
	
	public List<Integer> appendToIntegerArrayField(Integer val){
		if(integerArrayField==null){ integerArrayField = new ArrayList<>(); }
		integerArrayField.add(val);
		return integerArrayField;
	}
	
	public List<String> appendToDelimitedStringArrayField(String val){
		if(delimitedStringArrayField==null){ delimitedStringArrayField = new ArrayList<>(); }
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

	public void setKey(ManyFieldBeanKey key){
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

	public String getTestSchemaUpdateField(){
		return testSchemaUpdateField;
	}

	public void setTestSchemaUpdateField(String testSchemaUpdateField){
		this.testSchemaUpdateField = testSchemaUpdateField;
	}

	public Long getIncrementField(){
		return incrementField;
	}

	public void setIncrementField(Long incrementField){
		this.incrementField = incrementField;
	}
	
}
