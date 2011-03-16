package com.hotpads.datarouter.test.node.basic.manyfield;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.ByteArrayField;
import com.hotpads.datarouter.storage.field.imp.array.UInt63ArrayField;
import com.hotpads.datarouter.storage.field.imp.comparable.ByteField;
import com.hotpads.datarouter.storage.field.imp.comparable.CharacterField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.ShortField;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateField;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbDoubleField;
import com.hotpads.datarouter.storage.field.imp.dumb.DumbFloatField;
import com.hotpads.datarouter.storage.field.imp.positive.VarIntField;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.collections.arrays.LongArray;


@SuppressWarnings("serial")
@Entity()
@AccessType("field")
public class ManyFieldTypeBean extends BaseDatabean<ManyFieldTypeBeanKey,ManyFieldTypeBean>{

	/*
	 * alter table ManyFieldTypeBean modify column longDateField bigint(20);
	 */
	
	@Id
	private ManyFieldTypeBeanKey key;
	
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

	@Lob @Column(length=1024)
	private byte[] stringByteField;
	
	@Lob @Column(length=1<<27)
	private byte[] data;
	
	@Lob @Column(length=1<<27)
	private List<Long> longArrayField;
	
	/***************************** columns ******************************/
	
	public static final String
		KEY_NAME = "key",
		COL_id = "id",
		COL_byteField = "byteField",
		COL_shortField = "shortField",
		COL_integerField = "integerField",
		COL_longField = "longField",
		COL_floatField = "floatField",
		COL_doubleField = "doubleField",
		COL_longDateField = "longDateField",
		COL_characterField = "characterField",
		COL_stringField = "stringField",
		COL_varIntField = "varIntField",
		COL_stringByteField = "stringByteField",
		COL_data = "data",
		COL_longArrayField = "longArrayField";
	
	@Override
	public List<Field<?>> getNonKeyFields(){
		List<Field<?>> fields = ListTool.createLinkedList();
		fields.add(new ByteField(COL_byteField, this.byteField));
		fields.add(new ShortField(COL_shortField, this.shortField));
		fields.add(new IntegerField(COL_integerField, this.integerField));
		fields.add(new LongField(COL_longField, this.longField));
		fields.add(new DumbFloatField(COL_floatField, this.floatField));
		fields.add(new DumbDoubleField(COL_doubleField, this.doubleField));
		fields.add(new LongDateField(COL_longDateField, this.longDateField));
		fields.add(new CharacterField(COL_characterField, this.characterField));
		fields.add(new StringField(COL_stringField, this.stringField));
		fields.add(new VarIntField(COL_varIntField, this.varIntField));
		fields.add(new ByteArrayField(COL_stringByteField, this.stringByteField));
		fields.add(new ByteArrayField(COL_data, this.data));
		fields.add(new UInt63ArrayField(COL_longArrayField, this.longArrayField));
		return fields;
	}
	
	@Override
	public boolean isFieldAware(){
		return true;
	}

	/***************************** constructor **************************************/
		
	public ManyFieldTypeBean() {
		this.key = new ManyFieldTypeBeanKey();
	}
	
	/***************************** method ************************************/
	
	public List<Long> appendToLongArrayField(long val){
		if(longArrayField==null){ longArrayField = new LongArray(); }
		longArrayField.add(val);
		return longArrayField;
	}
	
	/***************************** get/set **************************************/
	
	@Override
	public Class<ManyFieldTypeBeanKey> getKeyClass() {
		return ManyFieldTypeBeanKey.class;
	};
	
	@Override
	public ManyFieldTypeBeanKey getKey() {
		return key;
	}

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



	
	
}
