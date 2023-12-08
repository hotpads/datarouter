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
package io.datarouter.loadtest.util;

import java.util.List;
import java.util.stream.IntStream;

import io.datarouter.util.number.RandomTool;

public class LoadTestTool{

	public static List<Integer> makeRandomIdBatch(int totalRows, int maxId, int targetBatchSize, int batchId){
		int thisBatchSize = adjustedBatchSize(totalRows, targetBatchSize, batchId);
		return IntStream.range(0, thisBatchSize)
				.mapToObj($ -> RandomTool.nextPositiveInt(maxId))
				.toList();
	}

	public static List<Integer> makePredictableIdBatch(int totalRows, int targetBatchSize, int batchId){
		int numBatches = numBatches(totalRows, targetBatchSize);
		int thisBatchSize = adjustedBatchSize(totalRows, targetBatchSize, batchId);
		return IntStream.range(0, thisBatchSize)
				.mapToObj(i -> i * numBatches + batchId)
				.toList();
	}

	public static int adjustedBatchSize(int totalRows, int targetBatchSize, int batchId){
		int numBatches = numBatches(totalRows, targetBatchSize);
		int numLeftover = totalRows % numBatches;
		if(numLeftover == 0){
			return targetBatchSize;
		}
		int numFullBatches = numLeftover;
		boolean isFullBatch = batchId < numFullBatches;
		return isFullBatch ? targetBatchSize : targetBatchSize - 1;
	}

	public static int numBatches(int totalRows, int targetBatchSize){
		int numFullBatches = totalRows / targetBatchSize;
		boolean anyLeftover = totalRows % targetBatchSize > 0;
		return anyLeftover ? numFullBatches + 1 : numFullBatches;
	}

}
