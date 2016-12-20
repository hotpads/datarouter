package com.hotpads.datarouter.storage.field.imp.positive;

import java.util.Random;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.number.RandomTool;

public class UInt63Field extends BasePrimitiveField<Long>{

	public UInt63Field(String prefix, UInt63FieldKey key, Long value){
		super(prefix, key, value);
	}

	public UInt63Field(UInt63FieldKey key, Long value){
		this(null, key, value);
	}

	@Deprecated
	public UInt63Field(String name, Long value){
		this(name, true, value);
	}

	@Deprecated
	public UInt63Field(String name, boolean nullable, Long value){
		this(null, name, nullable, FieldGeneratorType.NONE, value);
	}

	@Deprecated
	public UInt63Field(String prefix, String name, boolean nullable, FieldGeneratorType fieldGeneratorType, Long value){
		this(prefix, new UInt63FieldKey(name).withFieldGeneratorType(fieldGeneratorType).withNullable(nullable), value);
	}

	/************************ static *********************************/

	private static final Random random = new Random();

	public static long nextPositiveRandom(){
		return RandomTool.nextPositiveLong(random);
	}

	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.toString();
	}

	@Override
	public Long parseStringEncodedValueButDoNotSet(String str){
		if(DrStringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return Long.valueOf(str);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value == null ? null : LongByteTool.getUInt63Bytes(value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}

	@Override
	public Long fromBytesButDoNotSet(byte[] bytes, int offset){
		return LongByteTool.fromUInt63Bytes(bytes, offset);
	}

}
