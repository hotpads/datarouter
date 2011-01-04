package com.hotpads.datarouter.storage.field;


public interface ByteAwareField<T>{

	boolean isFixedLength();

	byte[] getColumnNameBytes();
	byte[] getMicroColumnNameBytes();
	
	byte[] getBytes();
	T fromBytesButDoNotSet(byte[] bytes, int byteOffset);
	
	int numBytesWithSeparator(byte[] bytes, int byteOffset);
	byte[] getBytesWithSeparator();
	T fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset);
	
}
