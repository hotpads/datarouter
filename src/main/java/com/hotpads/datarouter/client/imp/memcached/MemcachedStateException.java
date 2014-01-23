package com.hotpads.datarouter.client.imp.memcached;

/**
 * thrown when memcached queue fills up - happens when a memcached connection dies
 * basically a checked wrapper for java.lang.IllegalStateException
 */
public class MemcachedStateException extends Exception {
	public MemcachedStateException(Exception e) {
		super(e);
	}
	
	public MemcachedStateException() {
		super();
	}
	
	public MemcachedStateException(String s) {
		super(s);
	}
}
