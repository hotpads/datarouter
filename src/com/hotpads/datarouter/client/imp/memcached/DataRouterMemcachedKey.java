package com.hotpads.datarouter.client.imp.memcached;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class DataRouterMemcachedKey<PK extends PrimaryKey<PK>>{

	protected String nodeName;
	protected Integer databeanVersion;
	protected PK primaryKey;
	
	public DataRouterMemcachedKey(String nodeName, Integer databeanVersion, PK primaryKey){
		if(nodeName.contains(":")){ throw new IllegalArgumentException("nodeName cannot contain \":\""); }
		this.nodeName = nodeName;
		this.databeanVersion = databeanVersion;
		this.primaryKey = primaryKey;
	}
	
	
	public String getVersionedKeyString(){
		return nodeName+":"+databeanVersion+":"+primaryKey.getPersistentString();
	}

	
	public static <PK extends PrimaryKey<PK>> List<String> getVersionedKeyStrings(
			String nodeName, int version, Collection<PK> fieldSets){
		List<String> outs = ListTool.createArrayListWithSize(fieldSets);
		for(PK f : IterableTool.nullSafe(fieldSets)){
			outs.add(new DataRouterMemcachedKey<PK>(nodeName, version, f).getVersionedKeyString());
		}
		return outs;
	}
}
