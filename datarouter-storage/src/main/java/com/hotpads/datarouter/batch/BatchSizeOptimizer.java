package com.hotpads.datarouter.batch;

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

import com.hotpads.datarouter.batch.databean.OpOptimizedBatchSize;
import com.hotpads.datarouter.batch.databean.OpOptimizedBatchSizeKey;
import com.hotpads.datarouter.batch.databean.OpPerformanceRecord;
import com.hotpads.datarouter.batch.math.PolynomialRegressionOptimumFinder;
import com.hotpads.datarouter.batch.math.PolynomialRegressionOptimumFinderPoint;

@Singleton
public class BatchSizeOptimizer{

	public static final int DEFAULT_BATCH_SIZE = 1000;
	public static final double DEFAULT_CURIOSITY = 0.1;

	private static final int MAX_BATCH_SIZE = 10000;
	private static final int MIN_BATCH_SIZE = 1;
	private static final double STEP = 0.1;

	private final Map<OpOptimizedBatchSizeKey,CachedOpOptimizedBatchSize> cachedOpOptimizedBatchSize;
	private final Random random;
	private final BatchSizeOptimizerNodes nodes;

	@Inject
	public BatchSizeOptimizer(BatchSizeOptimizerNodes nodes){
		this.nodes = nodes;
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
		nodes.getOpPerformanceRecord().put(new OpPerformanceRecord(opName, batchSize, rowCount, timeSpent), null);
	}

	public void computeAndSaveOptimalBatchSizeForAllOps(){
		List<Optional<OpOptimizedBatchSize>> computedBatchSizes = new ArrayList<>();
		SortedMap<Integer,NodePerformanceStats> statsPerBatchSize = new TreeMap<>();
		String opName = null;
		OpPerformanceRecord lastRecord = null;
		for(OpPerformanceRecord record : nodes.getOpPerformanceRecord().scan(null, null)){
			if(!record.getOpName().equals(opName)){
				if(opName != null){
					computedBatchSizes.add(computeOptimalBatchSizeAndCuriosityForOp(opName, statsPerBatchSize, record));
					statsPerBatchSize = new TreeMap<>();
				}
				opName = record.getOpName();
			}
			statsPerBatchSize.computeIfAbsent(record.getBatchSize(), key->new NodePerformanceStats()).addRecord(record);
			lastRecord = record;
		}
		if(opName != null){
			computedBatchSizes.add(computeOptimalBatchSizeAndCuriosityForOp(opName, statsPerBatchSize, lastRecord));
		}
		nodes.getOpOptimizedBatchSize().putMulti(computedBatchSizes.stream()
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList()), null);
	}

	private double getCuriosity(String opName){
		return getOpOptimizedBatchSizeFromCache(opName).getCuriosity();
	}

	private int getOptimalBatchSize(String opName){
		return getOpOptimizedBatchSizeFromCache(opName).getBatchSize();
	}

	private OpOptimizedBatchSize getOpOptimizedBatchSizeFromCache(String opName){
		return cachedOpOptimizedBatchSize.computeIfAbsent(new OpOptimizedBatchSizeKey(opName),
				key -> new CachedOpOptimizedBatchSize(nodes, key)).get();
	}

	private Optional<OpOptimizedBatchSize> computeOptimalBatchSizeAndCuriosityForOp(String opName,
			SortedMap<Integer,NodePerformanceStats> statsPerBatchSize, OpPerformanceRecord mostRecentRecord){
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

	private int computeOptimalBatchSizeForOp(String opName,
			SortedMap<Integer,NodePerformanceStats> statsPerBatchSize){
		List<PolynomialRegressionOptimumFinderPoint> points = statsPerBatchSize.entrySet()
				.stream()
				.map(entry -> new PolynomialRegressionOptimumFinderPoint(entry.getKey(), entry.getValue().getMean()))
				.collect(Collectors.toList());
		PolynomialRegressionOptimumFinder optimumFinder = new PolynomialRegressionOptimumFinder(points);
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
			long factor = record.getRowCount()/record.getBatchSize();
			count += factor;
			speedSum += factor*record.getRowsPerSeconds();
		}

		public int getMean(){
			if(count == 0){
				return 0;
			}
			return (int) (speedSum/count);
		}
	}

}