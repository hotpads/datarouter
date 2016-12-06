package com.hotpads.spark.data.exporter;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.amazonaws.AmazonServiceException;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MultiDatabeanExporter<PK extends PrimaryKey<PK>,D extends Databean<PK, D>>{
	Map<Class<? extends Databean<PK, D>>, String> export()
			throws ExecutionException, InterruptedException, AmazonServiceException;
}
