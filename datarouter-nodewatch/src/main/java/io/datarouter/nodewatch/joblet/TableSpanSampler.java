/**
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
package io.datarouter.nodewatch.joblet;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.nodewatch.TableSamplerCounters;
import io.datarouter.nodewatch.storage.tablesample.DatarouterTableSampleDao;
import io.datarouter.nodewatch.storage.tablesample.TableSample;
import io.datarouter.nodewatch.storage.tablesample.TableSampleKey;
import io.datarouter.nodewatch.util.TableSamplerTool;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import io.datarouter.storage.node.tableconfig.ClientTableEntityPrefixNameWrapper;
import io.datarouter.storage.op.scan.stride.StrideScanner.StrideScannerBuilder;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;
import io.datarouter.util.DateTool;
import io.datarouter.util.Require;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.tuple.Range;

/**
 * Scan keys from the start to the end of the range, saving a sample every N keys.  If isLastSpan=true, then update
 * the end span to the last row of the table.
 */
public class TableSpanSampler<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
implements Callable<List<TableSample>>{
	private static final Logger logger = LoggerFactory.getLogger(TableSpanSampler.class);

	//with target of 100, we hope for 99% of counting without scanKeys
	private static final int STRIDES_PER_SAMPLE = 100;

	private final SortedStorageReaderNode<PK,D,F> node;
	private final DatarouterTableSampleDao tableSampleDao;
	private final long samplerId;
	private final ClientTableEntityPrefixNameWrapper nodeNames;
	private final Range<PK> pkRange;
	private final TableSample endSample;
	private final long sampleSize;
	private final boolean enableOffsetting;
	private final int batchSize;
	private final int stride;
	private final Instant createdAt;
	private final boolean scanUntilEnd;
	private final Instant deadline;

	private final TableSamplerCounters counters;
	private final List<TableSample> samples = new ArrayList<>();//don't rely on for logic as it gets flushed

	//mutable fields shared between methods
	private Instant startedAt;
	private long totalRows = 0;
	private long numSinceLastMarker = 0;
	private PK latestPk = null;
	private Instant latestSpanStartedAt = Instant.now();
	private boolean wasInterrupted = false;

	public TableSpanSampler(
			SortedStorageReaderNode<PK,D,F> node,
			DatarouterTableSampleDao tableSampleDao,
			long samplerId,
			ClientTableEntityPrefixNameWrapper nodeNames,
			TableSampleKey startSampleKey,
			TableSample endSample,
			int sampleSize,
			boolean enableOffsetting,
			int batchSize,
			Instant createdAt,
			boolean scanUntilEnd,
			Instant deadline){
		this.node = node;
		this.tableSampleDao = tableSampleDao;
		this.samplerId = samplerId;
		this.nodeNames = nodeNames;
		this.pkRange = getPkRangeFromSamples(startSampleKey, endSample);
		this.endSample = endSample;
		this.sampleSize = sampleSize;
		this.enableOffsetting = enableOffsetting;

		this.batchSize = batchSize;
		Require.isTrue(
				sampleSize % batchSize == 0,
				String.format("sampleSize=%s should be an even multiple of batchSize=%s", sampleSize, batchSize));
		if(enableOffsetting){
			this.stride = Math.max(sampleSize / STRIDES_PER_SAMPLE, batchSize);
		}else{
			//if offsetting isn't supported clients can bring back keys for the full stride, so limit to batchSize
			this.stride = batchSize;
		}
		Require.isTrue(
				sampleSize % stride == 0,
				String.format("sampleSize=%s should be an even multiple of stride=%s", sampleSize, stride));

		this.createdAt = createdAt;
		this.scanUntilEnd = scanUntilEnd;
		this.deadline = deadline;
		this.counters = new TableSamplerCounters(nodeNames);
	}

	@Override
	public List<TableSample> call(){
		startedAt = Instant.now();
		logger.warn("starting " + this);
		scanThroughRange();
		if(totalRows == 0){
			handleNoRowsScanned();
		}else if(wasInterrupted){
			if(scanUntilEnd){
				if(endSample == null){
					handleNewEndOfTableOnInterrupt();
				}else{
					handleEndOfTableOnInterrupt();
				}
			}else{
				handleEndOfIntermediateSpanOnInterrupt();
			}
		}else{
			if(scanUntilEnd){
				if(endSample == null){
					handleNewEndOfTable();
				}else{
					boolean endSampleMoved = ObjectTool.notEquals(latestPk, pkRange.getEnd());
					if(endSampleMoved){
						handleMovedEndOfTable();
					}else{
						handleStationaryEndOfTable();
					}
				}
			}else{
				handleEndOfIntermediateSpan();
			}
		}
		return samples;
	}

	/*---------------- scanning --------------*/

	private void scanThroughRange(){
		Range<PK> posiblyOpenEndedPkRange = pkRange.clone();
		posiblyOpenEndedPkRange.setEnd(scanUntilEnd ? null : pkRange.getEnd());
		if(enableOffsetting){
			new StrideScannerBuilder<>(node)
					.withRange(posiblyOpenEndedPkRange)
					.withShouldStop(this::shouldInterrupt)
					.withStride(stride)
					.build()
					.forEach(stride -> {
						counters.incrementRpcs(stride.numRpcs);
						counters.incrementKeys(stride.numKeysTransferred);
						counters.incrementRows(stride.sampleCount);
						latestPk = stride.lastSeenKey;
						totalRows += stride.sampleCount;
						numSinceLastMarker += stride.sampleCount;
						wasInterrupted = stride.interrupted;
						if(stride.isLast){
							return; // other logic handles the last marker
						}
						if(wasInterrupted || numSinceLastMarker == sampleSize){
							insertIntermediateSample();
						}
					});
		}else{ //TODO remove after validating offsetting
			Config scanConfig = new Config()
					.setScannerCaching(false)
					.setOutputBatchSize(batchSize)
					.anyDelay();
			Iterator<PK> iterator = node.scanKeys(posiblyOpenEndedPkRange, scanConfig).iterator();
			while(iterator.hasNext()){
				counters.incrementRows(1);
				counters.incrementKeys(1);
				latestPk = iterator.next();
				++totalRows;
				if(totalRows % batchSize == 1){
					counters.incrementRpcs(1);
				}
				++numSinceLastMarker;
				if(iterator.hasNext()){//avoid treating the final key as intermediate; let the end-logic handle it
					if(shouldInterrupt()){
						wasInterrupted = true;
						insertIntermediateSample();
						break;
					}
					if(numSinceLastMarker == sampleSize){
						insertIntermediateSample();
					}
				}
			}
		}
	}

	private void insertIntermediateSample(){
		TableSample sample = makeSample("insertIntermediateSample", latestPk, null, false, false);
		putAndKeepSample(sample);
		numSinceLastMarker = 0;
		latestSpanStartedAt = Instant.now();
	}

	/*---------------- post-scan logic ----------------*/

	private void handleNoRowsScanned(){
		deleteEndSample("noRowsScanned");
	}

	private void handleEndOfIntermediateSpanOnInterrupt(){
		TableSample sample = makeSample("interruptedIntermediate", pkRange.getEnd(), createdAt, true, false);
		updateEndSampleOnInterrupt(sample);
		putAndKeepSample(sample);
	}

	private void handleEndOfIntermediateSpan(){
		TableSample sample = makeSample("intermediate", pkRange.getEnd(), createdAt, false, false);
		updateStableCountsIfStable(sample);
		putAndKeepSample(sample);
	}

	private void handleNewEndOfTableOnInterrupt(){
		TableSample sample = makeSample("interruptedNewEnd", latestPk, null, true, true);
		putAndKeepSample(sample);
	}

	private void handleNewEndOfTable(){
		TableSample sample = makeSample("newEnd", latestPk, null, false, true);
		putAndKeepSample(sample);
	}

	private void handleEndOfTableOnInterrupt(){
		TableSample sample = makeSample("interruptedEnd", pkRange.getEnd(), createdAt, true, true);
		updateEndSampleOnInterrupt(sample);
		putAndKeepSample(sample);
	}

	private void handleMovedEndOfTable(){
		deleteEndSample("moved");
		TableSample sample = makeSample("movedEnd", latestPk, null, false, true);
		putAndKeepSample(sample);
	}

	private void handleStationaryEndOfTable(){
		TableSample sample = makeSample("stationaryEnd", pkRange.getEnd(), createdAt, false, true);
		updateStableCountsIfStable(sample);
		putAndKeepSample(sample);
	}

	/*---------------- sample handling -----------------*/

	//helpful to enforce the key is PK vs TableSampleKey
	private TableSample makeSample(
			String reason,
			PK pk,
			Instant forceCreatedAt,
			boolean markInterrupted,
			boolean isLastSpan){
		String logCreatedAt = Optional.ofNullable(forceCreatedAt)
				.map(Object::toString)
				.orElse("now");
		String log = "makeSample reason=" + reason
				+ ", createdAt=" + logCreatedAt
				+ ", markInterrupted=" + markInterrupted
				+ ", isLastSpan=" + isLastSpan
				+ ", " + this;
		logger.info(log);

		Date sampleDateCreated = Optional.ofNullable(forceCreatedAt)
				.map(Date::from)
				.orElseGet(Date::new);
		var sample = new TableSample(
				nodeNames,
				pk.getFields(),
				numSinceLastMarker,
				sampleDateCreated,
				getLatestSpanCountTime().toMillis(),
				markInterrupted,
				isLastSpan);
		return sample;
	}

	private void putAndKeepSample(TableSample sample){
		try{
			tableSampleDao.put(sample);
			samples.add(sample);
		}catch(DataAccessException e){
			Throwable cause = e.getCause();
			if(cause != null && cause instanceof MysqlDataTruncation){
				logger.warn("let's continue and try to get to a sample we can record", e);
				return;
			}
			throw e;
		}
	}

	private void deleteEndSample(String reason){
		if(endSample == null){
			return;
		}
		String log = "deleteEndSample "
				+ ", reason=" + reason
				+ ", " + this;
		logger.warn(log);
		tableSampleDao.delete(endSample.getKey());
	}

	//endSample numRows and countTime have probably decreased, having no other knowledge, but it's only an estimate
	private void updateEndSampleOnInterrupt(TableSample newEndSample){
		long estRemainingRows = endSample.getNumRows() - totalRows;
		newEndSample.setNumRows(Math.max(1, estRemainingRows));
		long estRemainingCountTimeMs = endSample.getCountTimeMs() - getTotalCountTime().toMillis();
		newEndSample.setCountTimeMs(Math.max(1, estRemainingCountTimeMs));
	}

	private void updateStableCountsIfStable(TableSample newEndSample){
		if(endSample == null){
			return;
		}
		boolean sameKey = Objects.equals(endSample.getKey(), newEndSample.getKey());
		boolean sameNumRows = Objects.equals(endSample.getNumRows(), totalRows);
		if(sameKey && sameNumRows){
			newEndSample.incrementStableCounts();
			logger.warn("incremented numStableCounts: " + newEndSample);
		}
	}

	/*------------------- interrupt -------------*/

	private boolean shouldInterrupt(){
		if(Thread.currentThread().isInterrupted()){
			logger.warn("interrupted due to thread interrupted, {}", this);
			return true;
		}
		if(deadline != null && Instant.now().isAfter(deadline)){
			if(totalRows < sampleSize){//bad situation, we're making slow progress
				logger.warn("deadline reached before first sample, {}", this);
			}
			logger.warn("interrupted at deadline={}, {}", deadline, this);
			return true;
		}
		return false;
	}

	/*-------------------- Object --------------------*/

	@Override
	public String toString(){
		return "samplerId=" + samplerId
				+ ", node=" + nodeNames
				+ ", pkRange=" + getStringPkRange()
				+ ", scanUntilEnd=" + scanUntilEnd
				+ ", deadline=" + getTimeUntilDeadlineString()
				+ ", totalRows=" + NumberFormatter.addCommas(totalRows)
				+ ", numSinceLastMarker=" + NumberFormatter.addCommas(numSinceLastMarker)
				+ ", wasInterrupted=" + wasInterrupted
				+ ", latestPk=" + pkToString(latestPk);
	}

	/*-------------------- PKs ---------------------*/

	private Range<PK> getPkRangeFromSamples(TableSampleKey startSampleKey, TableSample endSample){
		PK startKey = Optional.ofNullable(startSampleKey)
				.map(this::sampleKeyToPk)
				.orElse(null);
		PK endKey = Optional.ofNullable(endSample)
				.map(Databean::getKey)
				.map(this::sampleKeyToPk)
				.orElse(null);
		return new Range<>(startKey, false, endKey, true);
	}

	private PK sampleKeyToPk(TableSampleKey sampleKey){
		return TableSamplerTool.extractPrimaryKeyFromSampleKey(node, sampleKey);
	}

	//TODO remove when default pk.toString() uses PrimaryKeyPercentCodec
	private String pkToString(PK pk){
		return Optional.ofNullable(pk)
				.map(PrimaryKeyPercentCodecTool::encode)
				.orElse(null);
	}

	private Range<String> getStringPkRange(){
		return new Range<>(
				pkToString(pkRange.getStart()),
				pkRange.getStartInclusive(),
				pkToString(pkRange.getEnd()),
				pkRange.getEndInclusive());
	}

	/*--------------- timing ----------------------*/

	private Duration getLatestSpanCountTime(){
		return Duration.between(latestSpanStartedAt, Instant.now()).plusMillis(1);//avoid zero
	}

	private Duration getTotalCountTime(){
		return Duration.between(startedAt, Instant.now()).plusMillis(1);//avoid zero
	}

	private String getTimeUntilDeadlineString(){
		return Optional.ofNullable(deadline)
				.map(Instant::toEpochMilli)
				.map(DateTool::getAgoString)
				.orElse("none");
	}

}
