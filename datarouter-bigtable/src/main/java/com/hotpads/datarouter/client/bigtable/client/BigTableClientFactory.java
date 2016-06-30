package com.hotpads.datarouter.client.bigtable.client;

import java.util.concurrent.ExecutorService;

import org.apache.hadoop.hbase.client.Connection;

import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.hbase.client.BaseHBaseClientFactory;
import com.hotpads.datarouter.routing.Datarouter;

public class BigTableClientFactory extends BaseHBaseClientFactory{

	private final BigTableOptions bigTableOptions;

	public BigTableClientFactory(Datarouter datarouter, String clientName,
			ClientAvailabilitySettings clientAvailabilitySettings, ExecutorService executor){
		super(datarouter, clientName, clientAvailabilitySettings, executor);
		this.bigTableOptions = new BigTableOptions(multiProperties, clientName);
	}

	@Override
	protected Connection makeConnection(){
		String projectId = bigTableOptions.projectId();
		String instanceId = bigTableOptions.instanceId();
		return BigtableConfiguration.connect(projectId, instanceId);
	}


}