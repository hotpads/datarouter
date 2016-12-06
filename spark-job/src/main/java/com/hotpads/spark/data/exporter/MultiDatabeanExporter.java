package com.hotpads.spark.data.downloaders;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.amazonaws.AmazonServiceException;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface TableExistenceCheckerAndDownloader<PK extends PrimaryKey<PK>,D extends Databean<PK, D>> {
	public Map<Class<? extends Databean<PK, D>>, SparkTableParameters<PK, D>> downloadTablesIfNecessary()
			throws ExecutionException, InterruptedException, AmazonServiceException;
}
