package com.hotpads.datarouter.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.type.writebehind.WriteBehindSortedMapStorageNode;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.util.core.iterable.BatchingIterable;
import com.hotpads.util.core.profile.PhaseTimer;

@Guice(moduleFactory=TestDatarouterJdbcModuleFactory.class)
public class JdbcBatchSizeOptimizerTester{
	
	@Inject
	private BatchSizeOptimizer batchSizeOptimizer;
	@Inject
	private BatchSizeOptimizerNodes	nodes;
	@Inject
	private BatchSizeOptimizerTestRouter router;
	
	@BeforeClass
	@AfterClass
	public void cleanUp(){
		nodes.getOpPerformanceRecordNode().deleteAll(null);
		((WriteBehindSortedMapStorageNode<?,?,?>) nodes.getOpPerformanceRecordNode()).flush();
	}
	
	@Test(enabled=false)
	public void testWithGetMulti(){
		String opName = "getMulti";
		List<TestDatabean> databeans = new ArrayList<>();
		for(int i = 0 ; i < 10000 ; i++){
			databeans.add(new TestDatabean(randomString(), randomString(), randomString()));
		}
		router.testDatabean.putMulti(databeans, null);
		List<TestDatabeanKey> keys = DatabeanTool.getKeys(databeans);
		//warm up
		router.testDatabean.getMulti(keys, 
				new Config().setIterateBatchSize(
						batchSizeOptimizer.getRecommendedBatchSize(opName, keys.size())));
		for(int j = 0 ; j < 100 ; j++){
			for(int i = 0 ; i < 20 ; i ++){
				int batchSize = batchSizeOptimizer.getRecommendedBatchSize(opName, keys.size());
				long start = System.currentTimeMillis();
				router.testDatabean.getMulti(keys, new Config().setIterateBatchSize(batchSize));
				long timeSpent = System.currentTimeMillis() - start;
				batchSizeOptimizer.recordBatchSizeAndTime(opName, batchSize, keys.size(), timeSpent);
			}
			batchSizeOptimizer.computeAndCacheRecommendedBatchSize(opName);
		}
		
	}
	
	@Test(enabled=false)
	public void testWithPutMulti(){
		String opName = "putMulti";
		for(int j = 0 ; j < 100 ; j++){
			PhaseTimer timer = new PhaseTimer();
			for(int i = 0 ; i < 20 ; i ++){
				List<TestDatabean> databeans = new ArrayList<>();
				for(int k = 0 ; k < 2000 ; k++){
					databeans.add(new TestDatabean(randomString(), randomString(), randomString()));
				}
				int batchSize = batchSizeOptimizer.getRecommendedBatchSize(opName, databeans.size());
				long start = System.currentTimeMillis();
				for(List<TestDatabean> databeanBatch : new BatchingIterable<>(databeans, batchSize)){
					router.testDatabean.putMulti(databeanBatch, null);
				}
				long timeSpent = System.currentTimeMillis() - start;
				batchSizeOptimizer.recordBatchSizeAndTime(opName, batchSize, databeans.size(), timeSpent);
			}
			timer.add("record");
			batchSizeOptimizer.computeAndCacheRecommendedBatchSize(opName);
			timer.add("analyse");
			System.out.println(timer.toString());
		}
	}
	
	private static final String randomString(){
		return UUID.randomUUID().toString();
	}
}