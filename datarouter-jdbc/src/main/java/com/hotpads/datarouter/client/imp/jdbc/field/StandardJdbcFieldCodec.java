package com.hotpads.datarouter.client.imp.jdbc.field;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.array.BooleanArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.array.ByteArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.array.DelimitedStringArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.array.DoubleArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.array.IntegerArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.array.PrimitiveIntegerArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.array.PrimitiveLongArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.array.UInt63ArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.array.UInt7ArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.custom.LongDateJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.dumb.DumbDoubleJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.dumb.DumbFloatJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.enums.IntegerEnumJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.enums.StringEnumJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.enums.VarIntEnumJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.positive.UInt15JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.positive.UInt31JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.positive.UInt63JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.positive.UInt7JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.positive.UInt8JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.positive.VarIntJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.primitive.BooleanJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.primitive.CharacterJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.primitive.LongJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.primitive.ShortJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.primitive.SignedByteJdbcFieldCodec;
import com.hotpads.datarouter.storage.field.Field;
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

public enum StandardJdbcFieldCodec{

	BOOLEAN(BooleanField.class, BooleanJdbcFieldCodec.class),
	CHARACTER(CharacterField.class, CharacterJdbcFieldCodec.class),
	SIGNED_BYTE(SignedByteField.class, SignedByteJdbcFieldCodec.class),
	SHORT(ShortField.class, ShortJdbcFieldCodec.class),
	INTEGER(IntegerField.class, UInt31JdbcFieldCodec.class),
	LONG(LongField.class, LongJdbcFieldCodec.class),
	DUMB_FLOAT(DumbFloatField.class, DumbFloatJdbcFieldCodec.class),
	DUBM_DOUBLE(DumbDoubleField.class, DumbDoubleJdbcFieldCodec.class),

	STRING(StringField.class, StringJdbcFieldCodec.class),
	DATE(DateField.class, DateJdbcFieldCodec.class),
	LONG_DATE(LongDateField.class, LongDateJdbcFieldCodec.class),

	//enums
	INTEGER_ENUM(IntegerEnumField.class, IntegerEnumJdbcFieldCodec.class),
	STRING_ENUM(StringEnumField.class, StringEnumJdbcFieldCodec.class),
	VAR_INT_ENUM(VarIntEnumField.class, VarIntEnumJdbcFieldCodec.class),

	//BaseListJdbcFieldCodec
	BOOLEAN_ARRAY(BooleanArrayField.class, BooleanArrayJdbcFieldCodec.class),
	DELIMITED_STRING_ARRAY(DelimitedStringArrayField.class, DelimitedStringArrayJdbcFieldCodec.class),
	DOUBLE_ARRAY(DoubleArrayField.class, DoubleArrayJdbcFieldCodec.class),
	INTEGER_ARRAY(IntegerArrayField.class, IntegerArrayJdbcFieldCodec.class),
	UINT63_ARRAY(UInt63ArrayField.class, UInt63ArrayJdbcFieldCodec.class),
	UINT7_ARRAY(UInt7ArrayField.class, UInt7ArrayJdbcFieldCodec.class),

	//primitive arrays
	BYTE_ARRAY(ByteArrayField.class, ByteArrayJdbcFieldCodec.class),
	PRIMITIVE_INTEGER_ARRAY(PrimitiveIntegerArrayField.class, PrimitiveIntegerArrayJdbcFieldCodec.class),
	PRIMITIVE_LONG_ARRAY(PrimitiveLongArrayField.class, PrimitiveLongArrayJdbcFieldCodec.class),

	//positive numbers only
	UINT15(UInt15Field.class, UInt15JdbcFieldCodec.class),
	UINT31(UInt31Field.class, UInt31JdbcFieldCodec.class),
	UINT63(UInt63Field.class, UInt63JdbcFieldCodec.class),
	UINT7(UInt7Field.class, UInt7JdbcFieldCodec.class),
	UINT8(UInt8Field.class, UInt8JdbcFieldCodec.class),
	VAR_INT(VarIntField.class, VarIntJdbcFieldCodec.class),
	;

	private final Class<? extends Field<?>> fieldType;
	private final Class<? extends JdbcFieldCodec> codecType;

	private <T,F extends Field<T>,C extends JdbcFieldCodec<T,? extends Field<T>>>
	StandardJdbcFieldCodec(Class<F> fieldType, Class<C> codecType){
		this.fieldType = fieldType;
		this.codecType = codecType;
	}


	public <T,F extends Field<T>> Class<F> getFieldType(){
		return (Class<F>)fieldType;
	}

	public <T,F extends Field<T>,C extends JdbcFieldCodec<T,F>> Class<C> getCodecType(){
		return (Class<C>)codecType;
	}

}
