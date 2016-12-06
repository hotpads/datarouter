package com.hotpads.spark.data.downloaders;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.amazonaws.AmazonServiceException;
import com.hotpads.datarouter.storage.databean.Databean;

public class NoopTableExistenceCheckerAndDownloader implements TableExistenceCheckerAndDownloader{
	private final Map<Class<? extends Databean>, SparkTableParameters> databeanClassToParameters;
	public NoopTableExistenceCheckerAndDownloader(
			Map<Class<? extends Databean>, SparkTableParameters> databeanClassToParameters){
		this.databeanClassToParameters = databeanClassToParameters;
	}

	@Override
	public Map<Class<? extends Databean>, SparkTableParameters> downloadTablesIfNecessary()
			throws ExecutionException, InterruptedException, AmazonServiceException{
		return databeanClassToParameters;
	}
}
