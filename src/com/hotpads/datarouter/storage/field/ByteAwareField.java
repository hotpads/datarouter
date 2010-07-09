package com.hotpads.datarouter.storage.field;


public interface ByteAwareField<T>{
	
	byte[] getMicroNameBytes();

	boolean isFixedLength();
	
	byte[] getBytes();
	byte[] getBytesWithSeparator();

	int numBytesWithSeparator(byte[] bytes, int byteOffset);
	T fromBytesButDoNotSet(byte[] bytes, int byteOffset);
	
}
