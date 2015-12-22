package com.hotpads.datarouter.storage.field;

import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.BooleanArrayField;
import com.hotpads.datarouter.storage.field.imp.array.ByteArrayField;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
import com.hotpads.datarouter.storage.field.imp.array.DoubleArrayField;
import com.hotpads.datarouter.storage.field.imp.array.IntegerArrayField;
import com.hotpads.datarouter.storage.field.imp.array.PrimitiveIntegerArrayField;
import com.hotpads.datarouter.storage.field.imp.array.PrimitiveLongArrayField;
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

public enum StandardFieldType{

	BOOLEAN(BooleanField.class),
	CHARACTER(CharacterField.class),
	SIGNED_BYTE(SignedByteField.class),
	SHORT(ShortField.class),
	INTEGER(IntegerField.class),
	LONG(LongField.class),
	DUMB_FLOAT(DumbFloatField.class),
	DUBM_DOUBLE(DumbDoubleField.class),

	STRING(StringField.class),
	DATE(DateField.class),
	LONG_DATE(LongDateField.class),

	//enums
	INTEGER_ENUM(IntegerEnumField.class),
	STRING_ENUM(StringEnumField.class),
	VAR_INT_ENUM(VarIntEnumField.class),

	//BaseListField
	BOOLEAN_ARRAY(BooleanArrayField.class),
	DELIMITED_STRING_ARRAY(DelimitedStringArrayField.class),
	DOUBLE_ARRAY(DoubleArrayField.class),
	INTEGER_ARRAY(IntegerArrayField.class),
	UINT63_ARRAY(UInt63ArrayField.class),
	UINT7_ARRAY(UInt7ArrayField.class),

	//primitive arrays
	BYTE_ARRAY(ByteArrayField.class),
	PRIMITIVE_INTEGER_ARRAY(PrimitiveIntegerArrayField.class),
	PRIMITIVE_LONG_ARRAY(PrimitiveLongArrayField.class),

	//positive numbers only
	UINT15(UInt15Field.class),
	UINT31(UInt31Field.class),
	UINT63(UInt63Field.class),
	UINT7(UInt7Field.class),
	UINT8(UInt8Field.class),
	VAR_INT(VarIntField.class),
	;


	//tricky to get the generics and enum fields to play nicely :/
	private final Class<? extends Field> fieldType;

	private StandardFieldType(Class<? extends Field> fieldType){
		this.fieldType = fieldType;
	}


	public Class<? extends Field> getFieldType(){
		return fieldType;
	}
}
