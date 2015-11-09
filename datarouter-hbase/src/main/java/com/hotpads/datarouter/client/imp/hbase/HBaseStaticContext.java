package com.hotpads.datarouter.client.imp.hbase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Admin;

//TODO make injectable singleton
public class HBaseStaticContext{

	public static final Map<String,Configuration> CONFIG_BY_ZK_QUORUM = new ConcurrentHashMap<>();
	public static final Map<Configuration,Admin> ADMIN_BY_CONFIG = new ConcurrentHashMap<>();


}
