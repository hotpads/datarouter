package com.hotpads.datarouter.storage.field.imp.array;

import org.junit.Assert;
import org.junit.Test;

public class ByteArrayFieldTests{

	@Test
	public void stringEncodedValue(){
		byte[] value;
		value = new byte[]{
				0x1,
				0x5,
				-0x8,
				0x7f,
				0x25,
				0x6a,
				-0x80,
				-0x12
		};
		ByteArrayField byteArrayField = new ByteArrayField("testField", value);
		byte[] encodedDecodedValue = byteArrayField.parseStringEncodedValueButDoNotSet(byteArrayField
				.getStringEncodedValue());
		Assert.assertArrayEquals(value, encodedDecodedValue);
	}
}
