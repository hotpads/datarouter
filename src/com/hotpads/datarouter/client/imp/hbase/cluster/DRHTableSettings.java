package com.hotpads.datarouter.client.imp.hbase.cluster;

import java.util.Set;

import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public class DRHTableSettings{

	public static final String
		BLOCKCACHE = "BLOCKCACHE",
		BLOCKSIZE = "BLOCKSIZE",
		BLOOMFILTER = "BLOOMFILTER",
		COMPRESSION = "COMPRESSION",
		IN_MEMORY = "IN_MEMORY",
		TTL = "TTL",
		VERSIONS = "VERSIONS";
	
	public static final Set<String>
		SET_BLOOMFILTER = SetTool.create("NONE", "ROW", "ROWCOL"),
		SET_COMPRESSION = SetTool.create("NONE", "LZO", "GZ"),
		SET_DATA_BLOCK_ENCODING = SetTool.create("NONE", "PREFIX", "DIFF", "FAST_DIFF", "TRIE");
		
	
	public static void validateColumnFamilySetting(String setting, String value) {
		if(BLOCKCACHE.equals(setting)) {
			if(!validBoolean(value)) {
				throw new IllegalArgumentException("invalid "+BLOCKCACHE);
			}
		}else if(BLOCKSIZE.equals(setting)) {
			Long.valueOf(value);
		}else if(BLOOMFILTER.equals(setting)) {
			if(!SET_COMPRESSION.contains(value)) {
				throw new IllegalArgumentException("invalid "+COMPRESSION);
			}
		}else if(COMPRESSION.equals(setting)) {
			if(!SET_COMPRESSION.contains(value)) {
				throw new IllegalArgumentException("invalid "+COMPRESSION);
			}
		}else if(IN_MEMORY.equals(setting)) {
			if(!validBoolean(value)) {
				throw new IllegalArgumentException("invalid "+IN_MEMORY);
			}
		}else if(TTL.equals(setting)) {
			Long.valueOf(value);
		}else if(VERSIONS.equals(setting)) {
			Integer.valueOf(value);
		}
	}
	
	
	protected static boolean validBoolean(String value) {
		return "TRUE".equalsIgnoreCase(value) || "FALSE".equalsIgnoreCase(value);
	}
}
