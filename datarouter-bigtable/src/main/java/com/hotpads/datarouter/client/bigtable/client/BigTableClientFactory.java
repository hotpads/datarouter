package com.hotpads.datarouter.client.bigtable.client;

import java.util.concurrent.ExecutorService;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;

import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import com.google.cloud.bigtable.hbase.BigtableOptionsFactory;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.bigtable.BigTableClientType;
import com.hotpads.datarouter.client.imp.hbase.client.BaseHBaseClientFactory;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.routing.Datarouter;

public class BigTableClientFactory extends BaseHBaseClientFactory{

	private final BigTableOptions bigTableOptions;

	public BigTableClientFactory(DatarouterProperties datarouterProperties, Datarouter datarouter, String clientName,
			ClientAvailabilitySettings clientAvailabilitySettings, ExecutorService executor,
			BigTableClientType clientType){
		super(datarouterProperties, datarouter, clientName, clientAvailabilitySettings, executor, clientType);
		this.bigTableOptions = new BigTableOptions(multiProperties, clientName);
	}

	@Override
	protected Connection makeConnection(){
		String projectId = bigTableOptions.projectId();
		String instanceId = bigTableOptions.instanceId();
		Configuration config = BigtableConfiguration.configure(projectId, instanceId);
		String credsLocation = bigTableOptions.credentialsLocation();
		config.set(BigtableOptionsFactory.BIGTABLE_SERVICE_ACCOUNT_JSON_KEYFILE_LOCATION_KEY, credsLocation);
		return BigtableConfiguration.connect(config);
	}

}