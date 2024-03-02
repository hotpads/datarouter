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
package io.datarouter.plugin.copytable.tableprocessor;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessor.DatabeanTableProcessor;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessor.PrimaryKeyTableProcessor;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.tuple.Range;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TableProcessorService{
	private static final Logger logger = LoggerFactory.getLogger(TableProcessorService.class);

	@Inject
	private DatarouterNodes nodes;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	TableProcessorSpanResult runTableProcessor(
			String nodeName,
			String fromKeyExclusiveString,
			String toKeyInclusiveString,
			int scanBatchSize,
			TableProcessor<?> tableProcessor,
			long batchId,
			long numBatches){
		@SuppressWarnings("unchecked")
		SortedMapStorageNode<PK,D,?> node = (SortedMapStorageNode<PK,D,?>) nodes.getNode(nodeName);
		Objects.requireNonNull(node, nodeName + " not found");

		PK fromKeyExclusive = PrimaryKeyPercentCodecTool.decode(
				node.getFieldInfo().getPrimaryKeySupplier(),
				fromKeyExclusiveString);
		PK toKeyInclusive = PrimaryKeyPercentCodecTool.decode(
				node.getFieldInfo().getPrimaryKeySupplier(),
				toKeyInclusiveString);
		Range<PK> range = new Range<>(fromKeyExclusive, false, toKeyInclusive, true);
		var numScanned = new AtomicLong();
		AtomicReference<PK> lastKey = new AtomicReference<>();
		try{
			if(tableProcessor instanceof DatabeanTableProcessor){
				@SuppressWarnings("unchecked")
				DatabeanTableProcessor<PK,D> processor = (DatabeanTableProcessor<PK,D>) tableProcessor;
				node.scan(range, new Config().setResponseBatchSize(scanBatchSize))
						.each($ -> Metrics.count("tableProcessor " + nodeName + " scanned"))
						.each($ -> numScanned.incrementAndGet())
						.each(databean -> lastKey.set(databean.getKey()))
						.periodic(Duration.ofSeconds(5), $ -> logProgress(
								false,
								numScanned.get(),
								batchId,
								numBatches,
								nodeName,
								lastKey.get(),
								null))
						.then(processor::accept);
			}else if(tableProcessor instanceof PrimaryKeyTableProcessor){
				@SuppressWarnings("unchecked")
				PrimaryKeyTableProcessor<PK> processor = (PrimaryKeyTableProcessor<PK>) tableProcessor;
				node.scanKeys(range, new Config().setResponseBatchSize(scanBatchSize))
						.each($ -> Metrics.count("tableProcessor " + nodeName + " scanned"))
						.each($ -> numScanned.incrementAndGet())
						.each(lastKey::set)
						.periodic(Duration.ofSeconds(5), $ -> logProgress(
								false,
								numScanned.get(),
								batchId,
								numBatches,
								nodeName,
								lastKey.get(),
								null))
						.then(processor::accept);
			}else{
				throw new RuntimeException("Not a valid Table Processor");
			}
			logProgress(
					true,
					numScanned.get(),
					batchId,
					numBatches,
					nodeName,
					lastKey.get(),
					null);
			return new TableProcessorSpanResult(true, null, numScanned.get(), null);
		}catch(Throwable e){
			PK pk = lastKey.get();
			logProgress(false, numScanned.get(), batchId, numBatches, nodeName, pk, e);
			String resumeFromKeyString = pk == null ? null : PrimaryKeyPercentCodecTool.encode(pk);
			return new TableProcessorSpanResult(false, e, numScanned.get(), resumeFromKeyString);
		}
	}

	private <
			PK extends PrimaryKey<PK>>
	void logProgress(
			boolean finished,
			long numScanned,
			long batchId,
			long numBatches,
			String sourceNodeName,
			PK lastKey,
			Throwable throwable){
		String finishedString = finished ? "finished" : "intermediate";
		logger.warn("{} scanned {} for batch {}/{} from {} through {}",
				finishedString,
				NumberFormatter.addCommas(numScanned),
				NumberFormatter.addCommas(batchId),
				NumberFormatter.addCommas(numBatches),
				sourceNodeName,
				lastKey == null ? null : PrimaryKeyPercentCodecTool.encode(lastKey),
				throwable);
	}

	public record TableProcessorSpanResult(
			boolean success,
			Throwable exception,
			long numScanned,
			String resumeFromKeyString){
	}

}
