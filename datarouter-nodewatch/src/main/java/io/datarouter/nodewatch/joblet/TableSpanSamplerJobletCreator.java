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
package io.datarouter.nodewatch.joblet;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.model.JobletPackage;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.nodewatch.job.TableSamplerJob;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJoblet.TableSpanSamplerJobletParams;
import io.datarouter.nodewatch.storage.tablesample.DatarouterTableSampleDao;
import io.datarouter.nodewatch.storage.tablesample.TableSample;
import io.datarouter.nodewatch.storage.tablesample.TableSampleKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.storage.node.tableconfig.ClientTableEntityPrefixNameWrapper;
import io.datarouter.types.MilliTime;
import io.datarouter.util.DateTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.timer.PhaseTimer;

public class TableSpanSamplerJobletCreator<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(TableSpanSamplerJobletCreator.class);

	private static final long RESAMPLE_PERIOD_MS = Duration.ofDays(7).toMillis();
	private static final Duration AGGRESSIVE_RESAMPLE_WAIT = Duration.ofMinutes(5);
	private static final int BATCH_SIZE = 1000;

	private final DatarouterTableSampleDao tableSampleDao;
	private final JobletService jobletService;

	private final SortedStorageReader<PK,D> node;
	private final int sampleSize;
	private final int batchSize;
	private final boolean forceCounting;
	private final boolean submitJoblets;
	private final long samplerStartMs;

	private final ClientTableEntityPrefixNameWrapper nodeNames;
	private final long offsetInPeriodMs;
	private final long periodStartMs;

	private final List<JobletPackage> jobletPackages = new ArrayList<>();
	private int jobletBatchSequence = RandomTool.nextPositiveInt();//in case multiple threads start in the same millisec
	private int numExisting = 0;
	private int numMerged = 0;
	private int numConsidered = 0;
	private int numIncluded = 0;
	private int numSkippedAlreadyScheduled = 0;
	private int numSkippedSkipThisPeriod = 0;
	private int numSkippedAlreadyCounted = 0;
	private int numSkippedAwaitingTimeSlice = 0;

	public TableSpanSamplerJobletCreator(
			DatarouterTableSampleDao tableSampleDao,
			JobletService jobletService,
			PhysicalSortedStorageReaderNode<PK,D,F> node,
			int sampleSize,
			int batchSize,
			boolean forceCounting,
			boolean submitJoblets,
			long samplerStartMs){
		this.tableSampleDao = tableSampleDao;
		this.jobletService = jobletService;
		this.submitJoblets = submitJoblets;

		this.node = node;
		this.sampleSize = sampleSize;
		this.batchSize = batchSize;
		this.forceCounting = forceCounting;
		this.samplerStartMs = samplerStartMs;

		this.nodeNames = new ClientTableEntityPrefixNameWrapper(node);
		this.offsetInPeriodMs = samplerStartMs % RESAMPLE_PERIOD_MS;
		this.periodStartMs = calcPeriodStartMs(samplerStartMs);
	}

	public List<JobletPackage> createJoblets(){
		var timer = new PhaseTimer(nodeNames.toString());
		var existingSamples = new PeekingIterator<>(tableSampleDao.streamForNode(nodeNames).iterator());
		if(!existingSamples.hasNext()){
			handleNoExistingSamples();
		}else{
			TableSample previous = null;
			while(existingSamples.hasNext()){
				TableSample current = existingSamples.next();
				++numExisting;
				boolean isLastSample = !existingSamples.hasNext();
				repairLastSampleIfNecessary(current, isLastSample);
				TableSample next = existingSamples.peek();
				if(tryMergeFirstIntoSecond(current, next)){
					continue;
				}
				considerCreatingJoblet(previous, current, isLastSample);
				resetInterruptedFlagIfNecessary(previous);
				previous = current;
			}
		}
		//TODO check if first sample was considered or merged
		logSummary();
		timer.add("scan");
		if(!jobletPackages.isEmpty() && submitJoblets){
			jobletService.submitJobletPackages(jobletPackages);
		}
		timer.add("submitJoblets");
		logger.debug("{}", timer);
		if(timer.getElapsedTimeBetweenFirstAndLastEvent() > Duration.ofSeconds(5).toMillis()){
			logger.warn("{}", timer);
		}
		return jobletPackages;
	}

	private void handleNoExistingSamples(){
		//quick check if the table is empty, to avoid creating the joblet for simpler monitoring and debugging
		Optional<PK> optPk = node.scanKeys(new Config().setLimit(1))
				.findFirst();
		if(optPk.isPresent()){
			// insert dummy sample to prevent future runs from creating duplicate joblets
			long samplerId = RandomTool.nextPositiveLong();
			var sample = new TableSample(nodeNames, optPk.get().getFields(), 1L, MilliTime.now(), 1L, false, true);
			sample.setScheduleFields(samplerId, MilliTime.now());
			tableSampleDao.put(sample);
			jobletPackages.add(createJobletPackage(JobletPriority.DEFAULT, null, sample, samplerId, true));
		}else{
			logger.info("no sampler joblets created because no rows found in {}", nodeNames);
		}
	}

	public boolean tryMergeFirstIntoSecond(TableSample current, TableSample next){
		if(next == null){
			return false;
		}
		if(current.isInterrupted()
				|| current.isScheduledForRecount()
				|| next.isScheduledForRecount()){//wait till a better time
			return false;
		}
		if(current.getNumRows() + next.getNumRows() <= sampleSize){
			logger.info("merging {} into {}", current, next);
			next.addNumRowsAndCountTimeMsFromOther(current);
			tableSampleDao.put(next); // save the entry with updated count
			tableSampleDao.delete(current.getKey());
			++numMerged;
			return true;
		}
		//TODO merge if previous sample is incomplete
		return false;
	}

	private void considerCreatingJoblet(TableSample start, TableSample end, boolean isLastSample){
		numConsidered++;
		ShouldCountAndPriority shouldCountAndPriority = shouldCount(end, isLastSample);
		boolean shouldCount = shouldCountAndPriority.shouldCount();
		JobletPriority jobletPriority = shouldCountAndPriority.priority();
		if(!shouldCount){
			return;
		}
		long samplerId = RandomTool.nextPositiveLong();
		numIncluded++;
		jobletPackages.add(createJobletPackage(jobletPriority, start, end, samplerId, isLastSample));
		if(jobletPackages.size() > BATCH_SIZE && submitJoblets){
			jobletService.submitJobletPackages(jobletPackages);
			//TODO clearing violates the return value that integration tests are expecting
			jobletPackages.clear();
		}
		end.setScheduleFields(samplerId, MilliTime.now());
		tableSampleDao.put(end);
	}

	private void logSummary(){
		double pctThroughPeriod = 100 * (double)offsetInPeriodMs / RESAMPLE_PERIOD_MS;
		String pctThroughPeriodString = NumberFormatter.format(pctThroughPeriod, 2);
		String log = "week starting " + DateTool.getYyyyMmDdHhMmSsMmmWithPunctuationNoSpaces(periodStartMs);
		log += " (" + pctThroughPeriodString + "%)";
		log += ", existing " + numExisting;
		log += ", merged " + numMerged;
		log += ", considered " + numConsidered;
		log += ", including " + numIncluded;
		log += " for " + nodeNames;
		log += ", alreadyScheduled=" + numSkippedAlreadyScheduled;
		log += ", skipThisPeriod=" + numSkippedSkipThisPeriod;
		log += ", alreadyCounted=" + numSkippedAlreadyCounted;
		log += ", awaitingTimeSlice=" + numSkippedAwaitingTimeSlice;
		logger.info(log);
	}

	/*------------------- repair ---------------------*/

	private void resetInterruptedFlagIfNecessary(TableSample sample){
		if(sample != null && sample.isInterrupted()){
			sample.setInterrupted(false);
			tableSampleDao.put(sample);
		}
	}

	private void repairLastSampleIfNecessary(TableSample sample, boolean actualLastSample){
		// fix rows that shouldn't have lastSpan=true
		if(!actualLastSample && sample.isLastSpan()){
			sample.setLastSpan(false);
			tableSampleDao.put(sample);
		}else if(actualLastSample && !sample.isLastSpan()){
			sample.setLastSpan(true);
			tableSampleDao.put(sample);
		}
	}

	/*--------------- scheduling and rate limiting -----------------*/

	private ShouldCountAndPriority shouldCount(TableSample end, boolean isLastSample){
		if(forceCounting){
			return new ShouldCountAndPriority(true, JobletPriority.DEFAULT);
		}
		if(end.isInterrupted()){
			return new ShouldCountAndPriority(true, JobletPriority.DEFAULT);
		}
		if(end.hasExceededMaxTimeInQueue()){
			return new ShouldCountAndPriority(true, JobletPriority.DEFAULT);
		}
		if(end.isScheduledForRecount()){
			++numSkippedAlreadyScheduled;
			return new ShouldCountAndPriority(false, JobletPriority.DEFAULT);
		}
		if(shouldDoAggressiveCount(end, isLastSample)){
			logger.info("queueing aggressive sampling for {}, lastUpdated {}",
					end.getKey(),
					DateTool.getAgoString(end.getDateUpdated().toInstant()));
			return new ShouldCountAndPriority(true, JobletPriority.HIGH);
		}
		if(alreadyCountedThisPeriod(end)){
			++numSkippedAlreadyCounted;
			return new ShouldCountAndPriority(false, JobletPriority.DEFAULT);
		}
		if(!haveReachedThisPeriodsTimeSlice(end)){
			++numSkippedAwaitingTimeSlice;
			return new ShouldCountAndPriority(false, JobletPriority.DEFAULT);
		}
		return new ShouldCountAndPriority(true, JobletPriority.DEFAULT);
	}

	private boolean shouldDoAggressiveCount(TableSample sample, boolean isLastSample){
		if(!isLastSample){
			return false;
		}
		long msSinceLastSampling = MilliTime.now().minus(sample.getDateUpdated()).toEpochMilli();
		return msSinceLastSampling > AGGRESSIVE_RESAMPLE_WAIT.toMillis();
	}

	private boolean alreadyCountedThisPeriod(TableSample sample){
		return sample.getDateUpdated().toEpochMilli() > periodStartMs;
	}

	private boolean haveReachedThisPeriodsTimeSlice(TableSample sample){
		long hash = sample.getKey().positiveLongHashCode();
		long sampleOffsetInPeriodMs = hash % RESAMPLE_PERIOD_MS;
		logger.debug(
				"tableName={}, sampleOffsetInPeriodHours={} stringKey={}",
				sample.getKey().getTableName(),
				Duration.ofMillis(sampleOffsetInPeriodMs).toHours(),
				sample.getStringKey());
		//the clock has ticked past this sample's start time
		return offsetInPeriodMs + TableSamplerJob.SCHEDULING_INTERVAL.toMillis() > sampleOffsetInPeriodMs;
	}

	private long calcPeriodStartMs(long timeMs){
		long offsetInPeriodMs = timeMs % RESAMPLE_PERIOD_MS;
		return timeMs - offsetInPeriodMs;
	}

	/*--------------------- create --------------------*/

	private JobletPackage createJobletPackage(
			JobletPriority jobletPriority,
			TableSample start,
			TableSample end,
			long samplerId,
			boolean scanUntilEnd){
		Objects.requireNonNull(end);
		//we want all joblets created by the parent job to have the same creation time so none have execution priority
		Instant jobletCreationDate = Instant.ofEpochMilli(samplerStartMs);
		int batchSequence = jobletBatchSequence++;
		TableSampleKey startSampleKey = Optional.ofNullable(start)
				.map(TableSample::getKey)
				.orElse(null);
		var params = new TableSpanSamplerJobletParams(
				scanUntilEnd,
				samplerStartMs,
				sampleSize,
				batchSize,
				startSampleKey,
				end,
				nodeNames,
				samplerId);
		String queueId = String.join(
				"-",
				params.nodeNames().getClientName(),
				params.nodeNames().getTableName());
		String groupId = null;// set to some timestamp?
		return JobletPackage.createDetailed(
				TableSpanSamplerJoblet.JOBLET_TYPE,
				jobletPriority,
				jobletCreationDate,
				batchSequence,
				false,
				queueId,
				groupId,
				params);
	}

	private record ShouldCountAndPriority(
			boolean shouldCount,
			JobletPriority priority){
	}

}
