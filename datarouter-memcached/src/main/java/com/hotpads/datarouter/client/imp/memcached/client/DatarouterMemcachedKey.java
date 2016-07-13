package com.hotpads.datarouter.client.imp.memcached.client;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.PrimaryKeyPercentCodec;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;

public class DatarouterMemcachedKey{

	public static final Integer DATAROUTER_VERSION = 3;

	private final String nodeName;
	private final Integer databeanVersion;
	private final PrimaryKey<?> primaryKey;

	public DatarouterMemcachedKey(String nodeName, Integer databeanVersion, PrimaryKey<?> primaryKey){
		if(nodeName.contains(":")){
			throw new IllegalArgumentException("nodeName cannot contain \":\"");
		}
		this.nodeName = nodeName;
		this.databeanVersion = databeanVersion;
		this.primaryKey = primaryKey;
	}

	public String getVersionedKeyString(){
		String encodedPk = PrimaryKeyPercentCodec.encode(primaryKey);
		return DATAROUTER_VERSION + ":" + nodeName + ":" + databeanVersion + ":" + encodedPk;
	}

	public static List<String> getVersionedKeyStrings(String nodeName, int version,
			Collection<? extends PrimaryKey<?>> pks){
		List<String> outs = DrListTool.createArrayListWithSize(pks);
		for(PrimaryKey<?> pk : DrIterableTool.nullSafe(pks)){
			outs.add(new DatarouterMemcachedKey(nodeName, version, pk).getVersionedKeyString());
		}
		return outs;
	}

}
