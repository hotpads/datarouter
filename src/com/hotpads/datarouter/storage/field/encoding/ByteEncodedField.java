package com.hotpads.datarouter.storage.field.encoding;


public interface ByteEncodedField<T>{

	boolean isFixedLength();

	byte[] getColumnNameBytes();
	
	byte[] getBytes();
	T fromBytesButDoNotSet(byte[] bytes, int byteOffset);
	
	int numBytesWithSeparator(byte[] bytes, int byteOffset);
	byte[] getBytesWithSeparator();
	T fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset);
	
}
