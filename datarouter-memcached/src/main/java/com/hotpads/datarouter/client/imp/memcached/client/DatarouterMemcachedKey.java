package com.hotpads.datarouter.client.imp.memcached.client;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;

public class DatarouterMemcachedKey<PK extends PrimaryKey<PK>>{

	public static final Integer DATAROUTER_VERSION = 2;
	
	protected String nodeName;
	protected Integer databeanVersion;
	protected PK primaryKey;
	
	public DatarouterMemcachedKey(String nodeName, Integer databeanVersion, PK primaryKey){
		if(nodeName.contains(":")){ throw new IllegalArgumentException("nodeName cannot contain \":\""); }
		this.nodeName = nodeName;
		this.databeanVersion = databeanVersion;
		this.primaryKey = primaryKey;
	}
	
	
	public String getVersionedKeyString(){
		return DATAROUTER_VERSION+":"+nodeName+":"+databeanVersion+":"+primaryKey.getPersistentString();
	}

	
	public static <PK extends PrimaryKey<PK>> List<String> getVersionedKeyStrings(
			String nodeName, int version, Collection<PK> fieldSets){
		List<String> outs = DrListTool.createArrayListWithSize(fieldSets);
		for(PK f : DrIterableTool.nullSafe(fieldSets)){
			outs.add(new DatarouterMemcachedKey<PK>(nodeName, version, f).getVersionedKeyString());
		}
		return outs;
	}
}
