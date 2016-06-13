package com.hotpads.datarouter.client.imp.hbase.cluster;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;

public class DrTableSettings{

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

	public static final SortedSet<String> COLUMN_SETTINGS = new TreeSet<>();
	static {
		COLUMN_SETTINGS.add(BLOCKCACHE);
		COLUMN_SETTINGS.add(BLOCKSIZE);
		COLUMN_SETTINGS.add(BLOOMFILTER);
		COLUMN_SETTINGS.add(COMPRESSION);
		COLUMN_SETTINGS.add(DATA_BLOCK_ENCODING);
		COLUMN_SETTINGS.add(ENCODE_ON_DISK);
		COLUMN_SETTINGS.add(IN_MEMORY);
		COLUMN_SETTINGS.add(TTL);
		COLUMN_SETTINGS.add(VERSIONS);
	}

	public static final List<String>
		BLOOMFILTER_STRINGS = Arrays.asList("NONE", "ROW", "ROWCOL"),
		COMPRESSION_STRINGS = Arrays.stream(Algorithm.values())
				.map(Algorithm::toString)
				.collect(Collectors.toList()),
		DATA_BLOCK_ENCODING_STRINGS = Arrays.stream(DataBlockEncoding.values())
				.map(DataBlockEncoding::toString)
				.collect(Collectors.toList());

	public static final String
		DEFAULT_DATA_BLOCK_ENCODING = "NONE",
		DEFAULT_ENCODE_ON_DISK = "true";


	public static void validateColumnFamilySetting(String setting, String value) {
		if(BLOCKCACHE.equals(setting)) {
			if(!validBoolean(value)) {
				throw new IllegalArgumentException("invalid " + BLOCKCACHE);
			}
		}else if(BLOCKSIZE.equals(setting)) {
			Long.valueOf(value);
		}else if(BLOOMFILTER.equals(setting)) {
			if(!BLOOMFILTER_STRINGS.contains(value)) {
				throw new IllegalArgumentException("invalid " + BLOOMFILTER + " " + value);
			}
		}else if(COMPRESSION.equals(setting)) {
			if(!COMPRESSION_STRINGS.contains(value)) {
				throw new IllegalArgumentException("invalid " + COMPRESSION + " " + value);
			}
		}else if(DATA_BLOCK_ENCODING.equals(setting)) {
			if(!DATA_BLOCK_ENCODING_STRINGS.contains(value)) {
				throw new IllegalArgumentException("invalid " + DATA_BLOCK_ENCODING + " " + value);
			}
		}else if(ENCODE_ON_DISK.equals(setting)) {
			if(!validBoolean(value)) {
				throw new IllegalArgumentException("invalid " + ENCODE_ON_DISK);
			}
		}else if(IN_MEMORY.equals(setting)) {
			if(!validBoolean(value)) {
				throw new IllegalArgumentException("invalid " + IN_MEMORY);
			}
		}else if(TTL.equals(setting)) {
			Long.valueOf(value);
		}else if(VERSIONS.equals(setting)) {
			Integer.valueOf(value);
		}
		else{
			throw new IllegalArgumentException("unknown setting " + setting);
		}
	}


	protected static boolean validBoolean(String value) {
		return "TRUE".equalsIgnoreCase(value) || "FALSE".equalsIgnoreCase(value);
	}
}
