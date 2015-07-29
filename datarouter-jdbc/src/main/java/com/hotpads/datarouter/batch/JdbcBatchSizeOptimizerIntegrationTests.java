package com.hotpads.datarouter.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;

import org.testng.Assert;
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
public class JdbcBatchSizeOptimizerIntegrationTests{
	
	private static final int DEFAULT_BATCH_SIZE = BatchSizeOptimizer.DEFAULT_BATCH_SIZE;
	private static final double DEFAULT_CURIOSITY = BatchSizeOptimizer.DEFAULT_CURIOSITY;
	private static final Random random = new Random();
	
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
	
	@Test
	public void testDefaultGetBatchSize(){
		Assert.assertTrue(batchSizeOptimizer.getRecommendedBatchSize("", "", 
				2 * DEFAULT_BATCH_SIZE) <= (1 + DEFAULT_CURIOSITY) * DEFAULT_BATCH_SIZE);
		Assert.assertTrue(batchSizeOptimizer.getRecommendedBatchSize("", "", 
				2 * DEFAULT_BATCH_SIZE) >= (1 - DEFAULT_CURIOSITY) * DEFAULT_BATCH_SIZE);
	}
	
	@Test(enabled = false)
	public void testRecordOpPerformance(){
		String nodeName = nodes.getOpPerformanceRecordNode().getName();
		String opName = "";
		int targetOptimal = 210;
		int rowCount = 10000;
		while(batchSizeOptimizer.getOptimalBatchSize(nodeName, opName) != targetOptimal){
			for(int count = 0 ; count < 1000 ; count++){
				int batchSize = batchSizeOptimizer.getRecommendedBatchSize(nodeName, opName, Integer.MAX_VALUE);
				int timeSpent = random.nextInt(10 * (1 + Math.abs(batchSize - targetOptimal)));
				batchSizeOptimizer.recordBatchSizeAndTime(nodeName, opName, batchSize, rowCount, timeSpent);
			}
			((WriteBehindSortedMapStorageNode<?,?,?>) nodes.getOpPerformanceRecordNode()).flush();
			batchSizeOptimizer.computeAndCacheRecommendedBatchSize(nodeName, opName);
			System.out.println(batchSizeOptimizer.getOptimalBatchSize(nodeName, opName));
		}
	}
	
	@Test(enabled=false)
	public void testWithGetMulti(){
		String nodeName = router.testDatabean.getName();
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
						batchSizeOptimizer.getRecommendedBatchSize(nodeName, opName, keys.size())));
		for(int j = 0 ; j < 100 ; j++){
			for(int i = 0 ; i < 20 ; i ++){
				int batchSize = batchSizeOptimizer.getRecommendedBatchSize(nodeName, opName, keys.size());
				long start = System.currentTimeMillis();
				router.testDatabean.getMulti(keys, new Config().setIterateBatchSize(batchSize));
				long timeSpent = System.currentTimeMillis() - start;
				batchSizeOptimizer.recordBatchSizeAndTime(nodeName, opName, batchSize, keys.size(), timeSpent);
			}
			batchSizeOptimizer.computeAndCacheRecommendedBatchSize(nodeName, opName);
			System.out.println(batchSizeOptimizer.getOptimalBatchSize(nodeName, opName));
		}
		
	}
	
	@Test
	public void testWithPutMulti(){
		String nodeName = router.testDatabean.getName();
		String opName = "putMulti";
		for(int j = 0 ; j < 100 ; j++){
			PhaseTimer timer = new PhaseTimer();
			for(int i = 0 ; i < 20 ; i ++){
				List<TestDatabean> databeans = new ArrayList<>();
				for(int k = 0 ; k < 2000 ; k++){
					databeans.add(new TestDatabean(randomString(), randomString(), randomString()));
				}
				int batchSize = batchSizeOptimizer.getRecommendedBatchSize(nodeName, opName, databeans.size());
				long start = System.currentTimeMillis();
				for(List<TestDatabean> databeanBatch : new BatchingIterable<>(databeans, batchSize)){
					router.testDatabean.putMulti(databeanBatch, null);
				}
				long timeSpent = System.currentTimeMillis() - start;
				batchSizeOptimizer.recordBatchSizeAndTime(nodeName, opName, batchSize, databeans.size(), timeSpent);
			}
			timer.add("record");
			batchSizeOptimizer.computeAndCacheRecommendedBatchSize(nodeName, opName);
			timer.add("analyse");
			System.out.println(batchSizeOptimizer.getOptimalBatchSize(nodeName, opName) + " "
					+ batchSizeOptimizer.getCuriosity(nodeName, opName));
			System.out.println(timer.toString());
		}
	}
	
	private static final String randomString(){
		return UUID.randomUUID().toString();
	}
}