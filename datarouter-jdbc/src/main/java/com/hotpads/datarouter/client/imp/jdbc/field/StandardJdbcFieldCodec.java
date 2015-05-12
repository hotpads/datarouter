package com.hotpads.datarouter.client.imp.jdbc.field;

import com.hotpads.datarouter.client.imp.jdbc.field.array.BooleanArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.array.ByteArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.array.DelimitedStringArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.array.DoubleArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.array.IntegerArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.array.PrimitiveIntegerArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.array.PrimitiveLongArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.array.UInt63ArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.array.UInt7ArrayJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.custom.LongDateJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.dumb.DumbDoubleJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.dumb.DumbFloatJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.enums.IntegerEnumJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.enums.StringEnumJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.enums.VarIntEnumJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.positive.UInt15JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.positive.UInt31JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.positive.UInt63JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.positive.UInt7JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.positive.UInt8JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.positive.VarIntJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.primitive.BooleanJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.primitive.CharacterJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.primitive.IntegerJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.primitive.LongJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.primitive.ShortJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.primitive.SignedByteJdbcFieldCodec;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.java.ReflectionTool;

public enum StandardJdbcFieldCodec{

	BOOLEAN(BooleanJdbcFieldCodec.class),
	CHARACTER(CharacterJdbcFieldCodec.class),
	SIGNED_BYTE(SignedByteJdbcFieldCodec.class),
	SHORT(ShortJdbcFieldCodec.class),
	INTEGER(IntegerJdbcFieldCodec.class),
	LONG(LongJdbcFieldCodec.class),
	DUMB_FLOAT(DumbFloatJdbcFieldCodec.class),
	DUBM_DOUBLE(DumbDoubleJdbcFieldCodec.class),

	STRING(StringJdbcFieldCodec.class),
	DATE(DateJdbcFieldCodec.class),
	LONG_DATE(LongDateJdbcFieldCodec.class),
	
	//enums
	INTEGER_ENUM(IntegerEnumJdbcFieldCodec.class),
	STRING_ENUM(StringEnumJdbcFieldCodec.class),
	VAR_INT_ENUM(VarIntEnumJdbcFieldCodec.class),
	
	//BaseListJdbcFieldCodec
	BOOLEAN_ARRAY(BooleanArrayJdbcFieldCodec.class),
	DELIMITED_STRING_ARRAY(DelimitedStringArrayJdbcFieldCodec.class),
	DOUBLE_ARRAY(DoubleArrayJdbcFieldCodec.class),
	INTEGER_ARRAY(IntegerArrayJdbcFieldCodec.class),
	UINT63_ARRAY(UInt63ArrayJdbcFieldCodec.class),
	UINT7_ARRAY(UInt7ArrayJdbcFieldCodec.class),
	
	//primitive arrays
	BYTE_ARRAY(ByteArrayJdbcFieldCodec.class),
	PRIMITIVE_INTEGER_ARRAY(PrimitiveIntegerArrayJdbcFieldCodec.class),
	PRIMITIVE_LONG_ARRAY(PrimitiveLongArrayJdbcFieldCodec.class),
	
	//positive numbers only
	UINT15(UInt15JdbcFieldCodec.class),
	UINT31(UInt31JdbcFieldCodec.class),
	UINT63(UInt63JdbcFieldCodec.class),
	UINT7(UInt7JdbcFieldCodec.class),
	UINT8(UInt8JdbcFieldCodec.class),
	VAR_INT(VarIntJdbcFieldCodec.class),
	;
	

	private final Class<? extends JdbcFieldCodec> codecType;
	private final Class<? extends Field<?>> fieldType;
	
	private StandardJdbcFieldCodec(Class<? extends JdbcFieldCodec> codecType){
		this.codecType = codecType;
		this.fieldType = createCodec().getFieldType();
	}
	
	
	public Class<? extends Field<?>> getFieldType(){
		return fieldType;
	}
	
	public Class<? extends JdbcFieldCodec> getCodecType(){
		return codecType;
	}
	
//	public <T> getFieldAndCodecTypes(){
//		
//	}
	
//	public <T,C extends JdbcFieldCodec<T>> C createCodec(){
//		return (C)ReflectionTool.create(codecType);
//	}
	
	public JdbcFieldCodec<?,?> createCodec(){
		Field<?> dummyField = ReflectionTool.create(fieldType);
		return ReflectionTool.createWithArgs(codecType, dummyField);//pass a null Field object as first constructor param
	}
	
}
