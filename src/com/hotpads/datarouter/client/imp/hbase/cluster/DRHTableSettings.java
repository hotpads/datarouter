package com.hotpads.datarouter.client.imp.hbase.cluster;

import java.util.Set;
import java.util.SortedSet;

import com.hotpads.util.core.SetTool;

public class DRHTableSettings{

	public static final Boolean HBASE_94 = false;
	
	public static final String
		BLOCKCACHE = "BLOCKCACHE",
		BLOCKSIZE = "BLOCKSIZE",
		BLOOMFILTER = "BLOOMFILTER",
		COMPRESSION = "COMPRESSION",
		DATA_BLOCK_ENCODING = "DATA_BLOCK_ENCODING",
		ENCODE_ON_DISK = "ENCODE_ON_DISK",
		IN_MEMORY = "IN_MEMORY",
		TTL = "TTL",
		VERSIONS = "VERSIONS";
	
	public static final SortedSet<String> COLUMN_SETTINGS = SetTool.createTreeSet();
	static {
		COLUMN_SETTINGS.add(BLOCKCACHE);
		COLUMN_SETTINGS.add(BLOCKSIZE);
		COLUMN_SETTINGS.add(BLOOMFILTER);
		COLUMN_SETTINGS.add(COMPRESSION);
		if(HBASE_94) {
			COLUMN_SETTINGS.add(DATA_BLOCK_ENCODING);
			COLUMN_SETTINGS.add(ENCODE_ON_DISK);
		}
		COLUMN_SETTINGS.add(IN_MEMORY);
		COLUMN_SETTINGS.add(TTL);
		COLUMN_SETTINGS.add(VERSIONS);
	}
	
	public static final Set<String>
		SET_BLOOMFILTER = SetTool.create("NONE", "ROW", "ROWCOL"),
		SET_COMPRESSION = SetTool.create("NONE", "LZO", "GZ"),
		SET_DATA_BLOCK_ENCODING = SetTool.create("NONE", "PREFIX", "DIFF", "FAST_DIFF", "TRIE");
		
	
	public static void validateColumnFamilySetting(String setting, String value) {
		if(BLOCKCACHE.equals(setting)) {
			if(!validBoolean(value)) {
				throw new IllegalArgumentException("invalid "+BLOCKCACHE);
			}
		}
		else if(BLOCKSIZE.equals(setting)) {
			Long.valueOf(value);
		}
		else if(BLOOMFILTER.equals(setting)) {
			if(!SET_BLOOMFILTER.contains(value)) {
				throw new IllegalArgumentException("invalid "+BLOOMFILTER+" "+value);
			}
		}
		else if(COMPRESSION.equals(setting)) {
			if(!SET_COMPRESSION.contains(value)) {
				throw new IllegalArgumentException("invalid "+COMPRESSION+" "+value);
			}
		}
		else if(DATA_BLOCK_ENCODING.equals(setting)) {
			if(!SET_DATA_BLOCK_ENCODING.contains(value)) {
				throw new IllegalArgumentException("invalid "+DATA_BLOCK_ENCODING+" "+value);
			}
		}
		else if(ENCODE_ON_DISK.equals(setting)) {
			if(!validBoolean(value)) {
				throw new IllegalArgumentException("invalid "+ENCODE_ON_DISK);
			}
		}
		else if(IN_MEMORY.equals(setting)) {
			if(!validBoolean(value)) {
				throw new IllegalArgumentException("invalid "+IN_MEMORY);
			}
		}
		else if(TTL.equals(setting)) {
			Long.valueOf(value);
		}
		else if(VERSIONS.equals(setting)) {
			Integer.valueOf(value);
		}
	}
	
	
	protected static boolean validBoolean(String value) {
		return "TRUE".equalsIgnoreCase(value) || "FALSE".equalsIgnoreCase(value);
	}
}
