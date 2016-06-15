package com.hotpads.datarouter.client.bigtable.imp.bigtable.client;

import org.apache.hadoop.hbase.client.Connection;

import com.google.cloud.bigtable.hbase.BigtableConfiguration;

public class SampleBigTableConnector {
	  private static final String PROJECT_ID = "YOUR_PROJECT_ID";
	  private static final String ZONE = "YOUR_ZONE_ID"; // for example, us-central1-b
	  private static final String CLUSTER_ID = "YOUR_CLUSTER_ID";

	  private static Connection connection = null;

	  public static void connect(){
	    connection = BigtableConfiguration.connect(PROJECT_ID, ZONE, CLUSTER_ID);
	  }
}