/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.test.node.basic.manyfield;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.DateToLongFieldCodec;
import io.datarouter.model.field.codec.IntListFieldCodec;
import io.datarouter.model.field.codec.IntegerMappedEnumFieldCodec;
import io.datarouter.model.field.codec.StringListToBinaryCsvFieldCodec;
import io.datarouter.model.field.codec.StringListToCsvFieldCodec;
import io.datarouter.model.field.codec.StringMappedEnumFieldCodec;
import io.datarouter.model.field.imp.LocalDateField;
import io.datarouter.model.field.imp.LocalDateFieldKey;
import io.datarouter.model.field.imp.StringEncodedField;
import io.datarouter.model.field.imp.StringEncodedFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.array.ByteArrayEncodedField;
import io.datarouter.model.field.imp.array.ByteArrayEncodedFieldKey;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.field.imp.array.ByteArrayFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.field.imp.comparable.DoubleField;
import io.datarouter.model.field.imp.comparable.DoubleFieldKey;
import io.datarouter.model.field.imp.comparable.FloatField;
import io.datarouter.model.field.imp.comparable.FloatFieldKey;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.field.imp.comparable.InstantFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerEncodedField;
import io.datarouter.model.field.imp.comparable.IntegerEncodedFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.field.imp.comparable.ShortField;
import io.datarouter.model.field.imp.comparable.ShortFieldKey;
import io.datarouter.model.field.imp.custom.LocalDateTimeField;
import io.datarouter.model.field.imp.custom.LocalDateTimeFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.util.lang.ObjectTool;

public class ManyFieldBean extends BaseDatabean<ManyFieldBeanKey,ManyFieldBean>{

	public static final int DEFAULT_STRING_LENGTH = CommonFieldSizes.DEFAULT_LENGTH_VARCHAR;

	private static final int LEN_STRING_ENUM_FIELD = 20;

	private Boolean booleanField;
	private Short shortField;
	private Integer integerField;
	private TestEnum enumToIntegerField;//for testing IntegerEncodedField
	private Long longField;
	private Float floatField;
	private Double doubleField;
	private Date dateToLongField;//for testing LongEncodedField
	private LocalDate localDateField;
	private LocalDateTime localDateTimeField;
	private Instant instantField;
	private String stringField;
	private TestEnum enumToStringField;//for testing StringEncodedField

	private byte[] stringByteField;

	private byte[] data;

	private List<String> csvField;
	private List<String> csvBytesField;
	private byte[] byteArrayField;
	private List<Integer> intListToByteArrayField;
	private String testSchemaUpdateField;

	public static class FieldKeys{
		public static final BooleanFieldKey booleanField = new BooleanFieldKey("booleanField");
		public static final ShortFieldKey shortField = new ShortFieldKey("shortField");
		public static final IntegerFieldKey integerField = new IntegerFieldKey("integerField");
		public static final IntegerEncodedFieldKey<TestEnum> enumToIntegerField = new IntegerEncodedFieldKey<>(
				"enumToIntegerField",
				new IntegerMappedEnumFieldCodec<>(TestEnum.BY_PERSISTENT_INTEGER));
		public static final LongFieldKey longField = new LongFieldKey("longField");
		public static final FloatFieldKey floatField = new FloatFieldKey("floatField");
		public static final DoubleFieldKey doubleField = new DoubleFieldKey("doubleField");
		public static final LongEncodedFieldKey<Date> dateToLongField = new LongEncodedFieldKey<>(
				"dateToLongField",
				new DateToLongFieldCodec());
		public static final LocalDateFieldKey localDateField = new LocalDateFieldKey("localDateField");
		public static final LocalDateTimeFieldKey localDateTimeField = new LocalDateTimeFieldKey("localDateTimeField");
		public static final InstantFieldKey instantField = new InstantFieldKey("instantField");
		public static final StringFieldKey stringField = new StringFieldKey("stringField")
				.withSize(CommonFieldSizes.MAX_KEY_LENGTH_UTF8MB4);
		public static final StringEncodedFieldKey<TestEnum> enumToStringField = new StringEncodedFieldKey<>(
				"enumToStringField",
				new StringMappedEnumFieldCodec<>(TestEnum.BY_PERSISTENT_STRING));
		public static final ByteArrayFieldKey stringByteField = new ByteArrayFieldKey("stringByteField")
				.withSize(CommonFieldSizes.MAX_LENGTH_LONGBLOB);
		public static final ByteArrayFieldKey data = new ByteArrayFieldKey("data")
				.withSize(CommonFieldSizes.MAX_LENGTH_LONGBLOB);
		public static final ByteArrayFieldKey byteArrayField = new ByteArrayFieldKey("byteArrayField");
		public static final ByteArrayEncodedFieldKey<List<Integer>> intListToByteArrayField
				= new ByteArrayEncodedFieldKey<>("intListToByteArrayField", IntListFieldCodec.INSTANCE);
		public static final StringEncodedFieldKey<List<String>> csvField = new StringEncodedFieldKey<>(
				"csvField",
				StringListToCsvFieldCodec.INSTANCE);
		public static final ByteArrayEncodedFieldKey<List<String>> csvBytesField = new ByteArrayEncodedFieldKey<>(
				"csvBytesField",
				StringListToBinaryCsvFieldCodec.INSTANCE);
		public static final StringFieldKey testSchemaUpdateField = new StringFieldKey("testSchemaUpdateField")
				.withSize(CommonFieldSizes.MAX_KEY_LENGTH_UTF8MB4);
	}

	public boolean equalsAllPersistentFields(ManyFieldBean that){
		if(ObjectTool.notEquals(getKey(), that.getKey())){
			return false;
		}
		if(ObjectTool.notEquals(booleanField, that.booleanField)){
			return false;
		}
		if(ObjectTool.notEquals(shortField, that.shortField)){
			return false;
		}
		if(ObjectTool.notEquals(integerField, that.integerField)){
			return false;
		}
		if(ObjectTool.notEquals(enumToIntegerField, that.enumToIntegerField)){
			return false;
		}
		if(ObjectTool.notEquals(longField, that.longField)){
			return false;
		}
		if(ObjectTool.notEquals(floatField, that.floatField)){
			return false;
		}
		if(ObjectTool.notEquals(doubleField, that.doubleField)){
			return false;
		}
		if(ObjectTool.notEquals(dateToLongField, that.dateToLongField)){
			return false;
		}
		if(ObjectTool.notEquals(localDateField, that.localDateField)){
			return false;
		}
		if(ObjectTool.notEquals(localDateTimeField, that.localDateTimeField)){
			return false;
		}
		if(ObjectTool.notEquals(instantField, that.instantField)){
			return false;
		}
		if(ObjectTool.notEquals(stringField, that.stringField)){
			return false;
		}
		if(ObjectTool.notEquals(stringByteField, that.stringByteField)){
			return false;
		}
		if(ObjectTool.notEquals(enumToStringField, that.enumToStringField)){
			return false;
		}
		if(ObjectTool.notEquals(data, that.data)){
			return false;
		}
		if(ObjectTool.notEquals(byteArrayField, that.byteArrayField)){
			return false;
		}
		if(ObjectTool.notEquals(intListToByteArrayField, that.intListToByteArrayField)){
			return false;
		}
		if(ObjectTool.notEquals(csvField, that.csvField)){
			return false;
		}
		if(ObjectTool.notEquals(csvBytesField, that.csvBytesField)){
			return false;
		}
		if(ObjectTool.notEquals(testSchemaUpdateField, that.testSchemaUpdateField)){
			return false;
		}
		return true;
	}

	public static class ManyFieldTypeBeanFielder extends BaseDatabeanFielder<ManyFieldBeanKey,ManyFieldBean>{

		public ManyFieldTypeBeanFielder(){
			super(ManyFieldBeanKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(ManyFieldBean databean){
			return List.of(
					new BooleanField(FieldKeys.booleanField, databean.booleanField),
					new ShortField(FieldKeys.shortField, databean.shortField),
					new IntegerField(FieldKeys.integerField, databean.integerField),
					new IntegerEncodedField<>(FieldKeys.enumToIntegerField, databean.enumToIntegerField),
					new LongField(FieldKeys.longField, databean.longField),
					new FloatField(FieldKeys.floatField, databean.floatField),
					new DoubleField(FieldKeys.doubleField, databean.doubleField),
					new LongEncodedField<>(FieldKeys.dateToLongField, databean.dateToLongField),
					new LocalDateField(FieldKeys.localDateField, databean.localDateField),
					new LocalDateTimeField(FieldKeys.localDateTimeField, databean.localDateTimeField),
					new InstantField(FieldKeys.instantField, databean.instantField),
					new StringField(FieldKeys.stringField, databean.stringField),
					new StringEncodedField<>(FieldKeys.enumToStringField, databean.enumToStringField),
					new ByteArrayField(FieldKeys.stringByteField, databean.stringByteField),
					new ByteArrayField(FieldKeys.data, databean.data),
					new ByteArrayEncodedField<>(FieldKeys.intListToByteArrayField, databean.intListToByteArrayField),
					new ByteArrayField(FieldKeys.byteArrayField, databean.byteArrayField),
					new StringEncodedField<>(FieldKeys.csvField, databean.csvField),
					new ByteArrayEncodedField<>(FieldKeys.csvBytesField, databean.csvBytesField),
					new StringField(FieldKeys.testSchemaUpdateField, databean.testSchemaUpdateField));
		}

	}

	public ManyFieldBean(){
		super(new ManyFieldBeanKey());// let the key generate a random value
	}

	public ManyFieldBean(Long id){
		super(new ManyFieldBeanKey(id));
	}

	@Override
	public Supplier<ManyFieldBeanKey> getKeySupplier(){
		return ManyFieldBeanKey::new;
	}

	public List<String> appendToCsvField(String val){
		if(csvField == null){
			csvField = new ArrayList<>();
		}
		csvField.add(val);
		return csvField;
	}

	public List<String> appendToCsvBytesField(String val){
		if(csvBytesField == null){
			csvBytesField = new ArrayList<>();
		}
		csvBytesField.add(val);
		return csvBytesField;
	}

	public byte[] getData(){
		return data;
	}

	public void setData(byte[] data){
		this.data = data;
	}

	public Integer getIntegerField(){
		return integerField;
	}

	public void setIntegerField(Integer integerField){
		this.integerField = integerField;
	}

	public TestEnum getEnumToIntegerField(){
		return this.enumToIntegerField;
	}

	public void setEnumToIntegerField(TestEnum enumToIntegerField){
		this.enumToIntegerField = enumToIntegerField;
	}

	public Short getShortField(){
		return shortField;
	}

	public void setShortField(Short shortField){
		this.shortField = shortField;
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

	public String getStringField(){
		return stringField;
	}

	public void setStringField(String stringField){
		this.stringField = stringField;
	}

	public TestEnum getEnumToStringField(){
		return enumToStringField;
	}

	public void setEnumToStringField(TestEnum enumToStringField){
		this.enumToStringField = enumToStringField;
	}

	public byte[] getStringByteField(){
		return stringByteField;
	}

	public void setStringByteField(byte[] stringByteField){
		this.stringByteField = stringByteField;
	}

	public byte[] getByteArrayField(){
		return byteArrayField;
	}

	public void setByteArrayField(byte[] byteArrayField){
		this.byteArrayField = byteArrayField;
	}

	public List<Integer> getIntListToByteArrayField(){
		return this.intListToByteArrayField;
	}

	public void setIntListToByteArrayField(List<Integer> intListToByteArrayField){
		this.intListToByteArrayField = intListToByteArrayField;
	}

	public Date getDateToLongField(){
		return dateToLongField;
	}

	public void setDateToLongField(Date dateToLongField){
		this.dateToLongField = dateToLongField;
	}

	public LocalDate getLocalDateField(){
		return localDateField;
	}

	public void setLocalDateField(LocalDate localDateField){
		this.localDateField = localDateField;
	}

	public LocalDateTime getDateTimeField(){
		return localDateTimeField;
	}

	public void setDateTimeField(LocalDateTime dateTimeField){
		this.localDateTimeField = dateTimeField;
	}

	public Instant getInstantField(){
		return instantField;
	}

	public void setInstantField(Instant instantField){
		this.instantField = instantField;
	}

	public Boolean getBooleanField(){
		return booleanField;
	}

	public void setBooleanField(Boolean booleanField){
		this.booleanField = booleanField;
	}

	public List<String> getCsvField(){
		return csvField;
	}

	public void setCsvField(List<String> csvField){
		this.csvField = csvField;
	}

	public List<String> getCsvBytesField(){
		return csvBytesField;
	}

	public void setCsvBytesField(List<String> csvBytesField){
		this.csvBytesField = csvBytesField;
	}

	public String getTestSchemaUpdateField(){
		return testSchemaUpdateField;
	}

	public void setTestSchemaUpdateField(String testSchemaUpdateField){
		this.testSchemaUpdateField = testSchemaUpdateField;
	}

}
