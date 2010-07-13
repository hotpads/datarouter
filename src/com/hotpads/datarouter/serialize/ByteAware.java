package com.hotpads.datarouter.serialize;


public interface ByteAware{

	byte[] getBytes(boolean allowNulls); //mostly for compound keys
	
}
