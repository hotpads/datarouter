/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.batchsizeoptimizer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.batchsizeoptimizer.math.PolynomialRegressionOptimumFinder;
import io.datarouter.batchsizeoptimizer.math.PolynomialRegressionOptimumFinderPoint;
import io.datarouter.batchsizeoptimizer.storage.optimizedbatch.DatarouterOpOptimizedBatchSizeDao;
import io.datarouter.batchsizeoptimizer.storage.optimizedbatch.OpOptimizedBatchSize;
import io.datarouter.batchsizeoptimizer.storage.optimizedbatch.OpOptimizedBatchSizeKey;
import io.datarouter.batchsizeoptimizer.storage.performancerecord.DatarouterOpPerformanceRecordDao;
import io.datarouter.batchsizeoptimizer.storage.performancerecord.OpPerformanceRecord;

@Singleton
public class BatchSizeOptimizer{
	private static final Logger logger = LoggerFactory.getLogger(BatchSizeOptimizer.class);

	public static final int DEFAULT_BATCH_SIZE = 1000;
	public static final double DEFAULT_CURIOSITY = 0.1;

	private static final int MAX_BATCH_SIZE = 10000;
	private static final int MIN_BATCH_SIZE = 1;
	private static final double STEP = 0.1;

	private final DatarouterOpPerformanceRecordDao opPerformanceRecordDao;
	private final DatarouterOpOptimizedBatchSizeDao opOptimizedBatchSizeDao;

	private final Map<OpOptimizedBatchSizeKey,CachedOpOptimizedBatchSize> cachedOpOptimizedBatchSize;
	private final Random random;

	@Inject
	public BatchSizeOptimizer(
			DatarouterOpPerformanceRecordDao opPerformanceRecordDao,
			DatarouterOpOptimizedBatchSizeDao opOptimizedBatchSizeDao){
		this.opPerformanceRecordDao = opPerformanceRecordDao;
		this.opOptimizedBatchSizeDao = opOptimizedBatchSizeDao;
		this.cachedOpOptimizedBatchSize = new ConcurrentHashMap<>();
		this.random = new Random();
	}

	public int getRecommendedBatchSize(String opName){
		return getRecommendedBatchSize(opName, Integer.MAX_VALUE);
	}

	public int getRecommendedBatchSize(String opName, int totalSize){
		int batchSize = getOptimalBatchSize(opName);
		int radiusRange = Math.max(1, (int) (batchSize * getCuriosity(opName)));
		if(totalSize <= batchSize - radiusRange){
			return totalSize;
		}
		int radius = random.nextInt(1 + radiusRange * 2) - radiusRange;
		return Math.max(1, batchSize + radius);
	}

	public void recordBatchSizeAndTime(String opName, int batchSize, long rowCount, long timeSpent){
		opPerformanceRecordDao.put(new OpPerformanceRecord(opName, batchSize, rowCount, timeSpent));
	}

	public void computeAndSaveOptimalBatchSizeForAllOps(){
		List<Optional<OpOptimizedBatchSize>> computedBatchSizes = new ArrayList<>();
		SortedMap<Integer,NodePerformanceStats> statsPerBatchSize = new TreeMap<>();
		String opName = null;
		OpPerformanceRecord lastRecord = null;
		for(OpPerformanceRecord record : opPerformanceRecordDao.scan().iterable()){
			if(!record.getKey().getOpName().equals(opName)){
				if(opName != null){
					computedBatchSizes.add(computeOptimalBatchSizeAndCuriosityForOp(opName, statsPerBatchSize, record));
					statsPerBatchSize = new TreeMap<>();
				}
				opName = record.getKey().getOpName();
			}
			statsPerBatchSize.computeIfAbsent(record.getBatchSize(), key -> new NodePerformanceStats()).addRecord(
					record);
			lastRecord = record;
		}
		if(opName != null){
			computedBatchSizes.add(computeOptimalBatchSizeAndCuriosityForOp(opName, statsPerBatchSize, lastRecord));
		}
		opOptimizedBatchSizeDao.putMulti(computedBatchSizes.stream()
				.flatMap(Optional::stream)
				.peek(opOptimizedBatchSize -> logger.info("saving opName={} batchSize={} curiosity={}",
						opOptimizedBatchSize.getKey().getOpName(),
						opOptimizedBatchSize.getBatchSize(),
						opOptimizedBatchSize.getCuriosity()))
				.collect(Collectors.toList()));
	}

	private double getCuriosity(String opName){
		return getOpOptimizedBatchSizeFromCache(opName).getCuriosity();
	}

	private int getOptimalBatchSize(String opName){
		return getOpOptimizedBatchSizeFromCache(opName).getBatchSize();
	}

	private OpOptimizedBatchSize getOpOptimizedBatchSizeFromCache(String opName){
		return cachedOpOptimizedBatchSize.computeIfAbsent(new OpOptimizedBatchSizeKey(opName),
				key -> new CachedOpOptimizedBatchSize(opOptimizedBatchSizeDao, key)).get();
	}

	private Optional<OpOptimizedBatchSize> computeOptimalBatchSizeAndCuriosityForOp(
			String opName,
			SortedMap<Integer,NodePerformanceStats> statsPerBatchSize,
			OpPerformanceRecord mostRecentRecord){
		if(Instant.ofEpochMilli(mostRecentRecord.getKey().getTimestamp()).plusSeconds(30).isBefore(Instant.now())
				|| statsPerBatchSize.size() < 3){
			return Optional.empty();
		}
		Integer newBatchSize = computeOptimalBatchSizeForOp(opName, statsPerBatchSize);
		Double updatedCuriosity = DEFAULT_CURIOSITY;
		if(newBatchSize.equals(getOptimalBatchSize(opName))){
			updatedCuriosity = getCuriosity(opName) + 0.01;
		}
		return Optional.of(new OpOptimizedBatchSize(opName, newBatchSize, updatedCuriosity));
	}

	private int computeOptimalBatchSizeForOp(
			String opName,
			SortedMap<Integer,NodePerformanceStats> statsPerBatchSize){
		List<PolynomialRegressionOptimumFinderPoint> points = statsPerBatchSize.entrySet()
				.stream()
				.map(entry -> new PolynomialRegressionOptimumFinderPoint(entry.getKey(), entry.getValue().getMean()))
				.collect(Collectors.toList());
		var optimumFinder = new PolynomialRegressionOptimumFinder(points);
		int optimumAbscissa = (int) Math.ceil(optimumFinder.getOptimumAbscissa());
		if(optimumFinder.optimumIsMaximum()){
			return limitBatchIfNeeded(optimumAbscissa);
		}
		int previousOptimalBatchSize = getOptimalBatchSize(opName);
		if(optimumAbscissa < previousOptimalBatchSize){
			return limitBatchIfNeeded((int)(previousOptimalBatchSize * (1 + STEP)));
		}
		return limitBatchIfNeeded((int)(previousOptimalBatchSize * (1 - STEP)));
	}

	private static int limitBatchIfNeeded(int batch){
		return Math.min(MAX_BATCH_SIZE, Math.max(batch, MIN_BATCH_SIZE));
	}

	public static class NodePerformanceStats{

		private long count;
		private long speedSum;

		public NodePerformanceStats(){
			this.count = 0;
			this.speedSum = 0;
		}

		public void addRecord(OpPerformanceRecord record){
			long factor = record.getRowCount() / record.getBatchSize();
			count += factor;
			speedSum += factor * record.getRowsPerSeconds();
		}

		public int getMean(){
			if(count == 0){
				return 0;
			}
			return (int)(speedSum / count);
		}

	}

}