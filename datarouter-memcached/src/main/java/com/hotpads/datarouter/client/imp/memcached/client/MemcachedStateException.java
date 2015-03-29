package com.hotpads.datarouter.client.imp.memcached.client;

/**
 * thrown when memcached queue fills up - happens when a memcached connection dies
 * basically a checked wrapper for java.lang.IllegalStateException
 */
@SuppressWarnings("serial")
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
