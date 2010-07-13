package com.hotpads.datarouter.storage.field;


public interface ByteAwareField<T>{
	
	byte[] getMicroNameBytes();

	boolean isFixedLength();
	
	byte[] getBytes();
	T fromBytesButDoNotSet(byte[] bytes, int byteOffset);
	
	int numBytesWithSeparator(byte[] bytes, int byteOffset);
	byte[] getBytesWithSeparator();
	T fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset);
	
}
