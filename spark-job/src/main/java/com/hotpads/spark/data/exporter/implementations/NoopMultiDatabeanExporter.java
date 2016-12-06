package com.hotpads.spark.data.exporter.implementations;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.amazonaws.AmazonServiceException;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.spark.data.exporter.MultiDatabeanExporter;

public class NoopMultiDatabeanExporter implements MultiDatabeanExporter{
	@Override
	public Map<Class<? extends Databean>, String> export()
			throws ExecutionException, InterruptedException, AmazonServiceException{
		return new HashMap<>();
	}
}
