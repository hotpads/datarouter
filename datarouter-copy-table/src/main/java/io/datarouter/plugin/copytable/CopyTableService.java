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
package io.datarouter.plugin.copytable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.plugin.copytable.config.DatarouterCopyTableExecutors.DatarouterCopyTablePutMultiExecutor;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.tuple.Range;

@Singleton
public class CopyTableService{
	private static final Logger logger = LoggerFactory.getLogger(CopyTableService.class);

	private static final Config SCAN_CONFIG = new Config().setResponseBatchSize(1_000);

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private PutMultiScannerContext putMultiScannerContext;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	CopyTableSpanResult copyTableSpan(
			String sourceNodeName,
			String targetNodeName,
			String fromKeyExclusiveString,
			String toKeyInclusiveString,
			int numThreads,
			int batchSize,
			long batchId,
			long numBatches){
		@SuppressWarnings("unchecked")
		SortedMapStorageNode<PK,D,?> sourceNode = (SortedMapStorageNode<PK,D,?>) nodes.getNode(sourceNodeName);
		Objects.requireNonNull(sourceNode, sourceNodeName + " not found");
		@SuppressWarnings("unchecked")
		SortedMapStorageNode<PK,D,?> targetNode = (SortedMapStorageNode<PK,D,?>) nodes.getNode(targetNodeName);
		Objects.requireNonNull(targetNode, targetNodeName + " not found");

		PK fromKeyExclusive = PrimaryKeyPercentCodecTool.decode(sourceNode.getFieldInfo().getPrimaryKeySupplier(),
				fromKeyExclusiveString);
		PK toKeyInclusive = PrimaryKeyPercentCodecTool.decode(sourceNode.getFieldInfo().getPrimaryKeySupplier(),
				toKeyInclusiveString);
		// hbase and bigtable put config
		Config putConfig = new Config();
				//.setPersistentPut(persistentPut)
				//.setIgnoreNullFields(true);//could be configurable
		var numSkipped = new AtomicLong();
		Range<PK> range = new Range<>(fromKeyExclusive, false, toKeyInclusive, true);
		var numScanned = new AtomicLong();
		var numCopied = new AtomicLong();
		AtomicReference<PK> lastKey = new AtomicReference<>();
		try{
			sourceNode.scan(range, SCAN_CONFIG)
					.each($ -> numScanned.incrementAndGet())
					.each($ -> Counters.inc("copyTable " + sourceNodeName + " read"))
					.batch(batchSize)
					.parallel(putMultiScannerContext.get(numThreads))
					.each(batch -> targetNode.putMulti(batch, putConfig))
					.each($ -> Counters.inc("copyTable " + sourceNodeName + " write"))
					.each(batch -> numCopied.addAndGet(batch.size()))
					.each(batch -> lastKey.set(ListTool.getLast(batch).getKey()))
					.sample(10, true)
					.forEach(batch -> logProgress(
							false,
							numSkipped.get(),
							numScanned.get(),
							numCopied.get(),
							batchId,
							numBatches,
							sourceNodeName,
							targetNodeName,
							lastKey.get(),
							null));
			logProgress(
					true,
					numSkipped.get(),
					numScanned.get(),
					numCopied.get(),
					batchId,
					numBatches,
					sourceNodeName,
					targetNodeName,
					lastKey.get(),
					null);
			return new CopyTableSpanResult(true, null, numCopied.get(), null);
		}catch(Throwable e){
			PK pk = lastKey.get();
			logProgress(false, numSkipped.get(), numScanned.get(), numCopied.get(), batchId, numBatches,
					sourceNodeName, targetNodeName, pk, e);
			String resumeFromKeyString = pk == null
					? null
					: PrimaryKeyPercentCodecTool.encode(pk);
			return new CopyTableSpanResult(false, e, numCopied.get(), resumeFromKeyString);
		}
	}

	private <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	void logProgress(
			boolean finished,
			long numSkipped,
			long numScanned,
			long numCopied,
			long batchId,
			long numBatches,
			String sourceNodeName,
			String targetNodeName,
			PK lastKey,
			Throwable throwable){
		String finishedString = finished ? "finished" : "intermediate";
		logger.warn("{} skipped {} scanned {} copied {} for batch {}/{} from {} to {} through {}",
				finishedString,
				NumberFormatter.addCommas(numSkipped),
				NumberFormatter.addCommas(numScanned),
				NumberFormatter.addCommas(numCopied),
				NumberFormatter.addCommas(batchId),
				NumberFormatter.addCommas(numBatches),
				sourceNodeName,
				targetNodeName,
				lastKey == null ? null : PrimaryKeyPercentCodecTool.encode(lastKey),
				throwable);
	}

	@Singleton
	public static class PutMultiScannerContext{

		@Inject
		private DatarouterCopyTablePutMultiExecutor executor;

		public ParallelScannerContext get(int numThreads){
			return new ParallelScannerContext(
					executor,
					numThreads,
					false,//keep ordering for reliable lastKey tracking
					numThreads > 1);
		}

	}

	public static class CopyTableSpanResult{

		public final boolean success;
		public final Throwable exception;
		public final long numCopied;
		public final String resumeFromKeyString;

		public CopyTableSpanResult(boolean success, Throwable exception, long numCopied,
				String resumeFromKeyString){
			this.success = success;
			this.exception = exception;
			this.numCopied = numCopied;
			this.resumeFromKeyString = resumeFromKeyString;
		}

	}

}
