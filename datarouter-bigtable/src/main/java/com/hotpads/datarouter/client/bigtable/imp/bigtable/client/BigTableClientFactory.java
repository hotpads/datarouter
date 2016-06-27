package com.hotpads.datarouter.client.bigtable.imp.bigtable.client;

import java.util.concurrent.ExecutorService;

import org.apache.hadoop.hbase.client.Connection;

import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.hbase.factory.BaseHBaseClientFactory;
import com.hotpads.datarouter.routing.Datarouter;

public class BigTableClientFactory extends BaseHBaseClientFactory{

	private static final String PROJECT_ID = "smiling-box-118021";
	private static final String ZONE = "us-central1-b"; // for example, us-central1-b
	private static final String CLUSTER_ID = "bigtable1";

	public BigTableClientFactory(Datarouter datarouter, String clientName,
			ClientAvailabilitySettings clientAvailabilitySettings, ExecutorService executor){
		super(datarouter, clientName, clientAvailabilitySettings, executor);
	}

	@Override
	protected Connection makeConnection(){
		return BigtableConfiguration.connect(PROJECT_ID, ZONE, CLUSTER_ID);
	}


}