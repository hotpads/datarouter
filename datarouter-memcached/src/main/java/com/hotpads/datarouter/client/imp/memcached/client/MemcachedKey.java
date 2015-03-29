package com.hotpads.datarouter.client.imp.memcached.client;

import net.spy.memcached.HashAlgorithm;

public class MemcachedKey {

	private MemcachedKeyType type;
	private String version;
	private String key;
	private boolean hashKey;
	private HashAlgorithm hashAlg = HashAlgorithm.FNV1_64_HASH;
	
	private MemcachedKey() { }

	public MemcachedKey(MemcachedKeyType type, Integer version, String key){
		this(type, version.toString(), key, false);
	}
	
	public MemcachedKey(MemcachedKeyType type, String version, String key){
		this(type, version, key, false);
	}
	
	public MemcachedKey(MemcachedKeyType type, Integer version, String key, boolean hashKey) {
		this(type, version.toString(), key, hashKey);
	}
	
	public MemcachedKey(MemcachedKeyType type, String version, String key, boolean hashKey) {
		this.type=type;
		this.version=version;
		this.key=key.replace(" ", "%20");
		this.hashKey = hashKey;
	}
	
	@Override
	public String toString(){
		return type.getKey() + ':' 
				+ version + ':' 
				+ (hashKey ? hashAlg.hash(key) : key);
	}
	
	/**
	 * this wont work too well when there are extra ':' in the key or keys that have been hashed
	 */
	public static MemcachedKey fromString(String str) {
		String[] strs = str.split(":");
		MemcachedKey key = new MemcachedKey();
		key.type = MemcachedKeyType.fromString(strs[0]);
		key.version = strs[1];
		key.key = strs[2]; //nothing we can do really, if its hashed.  maybe possible to detect it and set hashKey=true, but thats it.
		return key;
	}
	
	

	public MemcachedKeyType getType() {
		return type;
	}

	public void setType(MemcachedKeyType type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isHashKey() {
		return hashKey;
	}

	public void setHashKey(boolean hashKey) {
		this.hashKey = hashKey;
	}

	public HashAlgorithm getHashAlg() {
		return hashAlg;
	}

	public void setHashAlg(HashAlgorithm hashAlg) {
		this.hashAlg = hashAlg;
	}
}