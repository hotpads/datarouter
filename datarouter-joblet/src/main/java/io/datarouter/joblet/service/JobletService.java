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
package io.datarouter.joblet.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.joblet.DatarouterJobletCounters;
import io.datarouter.joblet.JobletExceptionCategory;
import io.datarouter.joblet.enums.JobletQueueMechanism;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.model.JobletPackage;
import io.datarouter.joblet.queue.JobletRequestQueueManager;
import io.datarouter.joblet.queue.JobletRequestSelector;
import io.datarouter.joblet.queue.JobletRequestSelectorFactory;
import io.datarouter.joblet.setting.DatarouterJobletSettingRoot;
import io.datarouter.joblet.storage.jobletdata.DatarouterJobletDataDao;
import io.datarouter.joblet.storage.jobletdata.JobletData;
import io.datarouter.joblet.storage.jobletdata.JobletDataKey;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.joblet.storage.jobletrequestqueue.DatarouterJobletQueueDao;
import io.datarouter.joblet.storage.jobletrequestqueue.JobletRequestQueueKey;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.model.databean.Databean;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.util.HashMethods;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.util.tuple.Range;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.webappinstance.service.CachedWebappInstancesOfThisServerType;

@Singleton
public class JobletService{
	private static final Logger logger = LoggerFactory.getLogger(JobletService.class);

	public static final int MAX_JOBLET_RETRIES = 10;

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private JobletRequestQueueManager jobletRequestQueueManager;
	@Inject
	private DatarouterJobletDataDao jobletDataDao;
	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;
	@Inject
	private ExceptionRecorder exceptionRecorder;
	@Inject
	private DatarouterJobletSettingRoot jobletSettings;
	@Inject
	private JobletRequestSelectorFactory jobletRequestSelectorFactory;
	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private DatarouterJobletCounters datarouterJobletCounters;
	@Inject
	private CachedWebappInstancesOfThisServerType cachedWebAppInstancesOfThisServerType;
	@Inject
	private DatarouterJobletQueueDao jobletQueueDao;

	/*--------------------- create ------------------------*/

	public void submitJobletPackages(Collection<JobletPackage> jobletPackages){
		Scanner.of(jobletPackages)
				.groupBy(jobletPackage -> jobletPackage.getJobletRequest().getKey().getType())
				.values()
				.forEach(this::submitJobletPackagesOfSameType);
	}

	private void submitJobletPackagesOfSameType(Collection<JobletPackage> jobletPackages){
		List<? extends JobletType<?>> jobletTypes = Scanner.of(jobletPackages)
				.map(jobletTypeFactory::fromJobletPackage)
				.list();
		JobletType.assertAllSameShortQueueName(jobletTypes);
		JobletType<?> jobletType = jobletTypes.iterator().next();
		for(List<JobletPackage> batch : Scanner.of(jobletPackages).batch(100).iterable()){
			var timer = new PhaseTimer("insert " + batch.size() + " " + jobletType);
			jobletDataDao.putMultiOrBust(JobletPackage.getJobletDatas(batch));
			timer.add("inserted JobletData");
			batch.forEach(JobletPackage::updateJobletDataIdReference);
			List<JobletRequest> jobletRequests = JobletPackage.getJobletRequests(batch);
			jobletRequestDao.putMultiOrBust(jobletRequests);
			datarouterJobletCounters.incNumJobletsInserted(jobletRequests.size());
			datarouterJobletCounters.incNumJobletsInserted(jobletType, jobletRequests.size());
			jobletRequests.stream()
					.map(JobletRequest::getQueueId)
					.forEach(queueId -> datarouterJobletCounters.incNumJobletsInserted(jobletType, queueId));
			timer.add("inserted JobletRequest");
			if(Objects.equals(jobletSettings.queueMechanism.get(), JobletQueueMechanism.SQS.getPersistentString())){
				Map<JobletRequestQueueKey,List<JobletRequest>> requestsByQueueKey = jobletRequests.stream()
						.collect(Collectors.groupingBy(jobletRequestQueueManager::getQueueKey, Collectors.toList()));
				for(Entry<JobletRequestQueueKey,List<JobletRequest>> queueAndRequests : requestsByQueueKey.entrySet()){
					jobletQueueDao.getQueue(queueAndRequests.getKey()).putMulti(queueAndRequests.getValue());
				}
				timer.add("queued JobletRequests");
			}
			if(timer.getElapsedTimeBetweenFirstAndLastEvent() > 200){
				logger.warn("slow insert joblets:{}", timer);
			}
		}
	}

	/*---------------------- read ---------------------------*/

	public List<JobletPackage> getJobletPackagesOfType(JobletType<?> jobletType){
		return scanJobletRequestsForType(jobletType)
				.batch(100)
				.map(this::getJobletPackagesForJobletRequests)
				.concat(Scanner::of)
				.list();
	}

	public JobletPackage getJobletPackageForJobletRequest(JobletRequest jobletRequest){
		List<JobletPackage> jobletPackages = getJobletPackagesForJobletRequests(Arrays.asList(jobletRequest));
		return jobletPackages.stream().findFirst().orElse(null);
	}

	public boolean jobletRequestExistsWithTypeAndStatus(JobletType<?> jobletType, JobletStatus jobletStatus){
		JobletRequestKey key = JobletRequestKey.create(jobletType, null, null, null);
		var range = new Range<>(key, true, key, true);
		return jobletRequestDao.scan(range, 50)
				.anyMatch(jobletRequest -> jobletRequest.getStatus() == jobletStatus);
	}

	public JobletData getJobletDataForJobletRequest(JobletRequest joblet){
		return jobletDataDao.get(joblet.getJobletDataKey());
	}

	public Scanner<JobletRequest> scanJobletRequestsForType(JobletType<?> jobletType){
		JobletRequestKey prefix = JobletRequestKey.create(jobletType, null, null, null);
		return jobletRequestDao.scanWithPrefix(prefix);
	}

	private List<JobletPackage> getJobletPackagesForJobletRequests(Collection<JobletRequest> jobletRequests){
		List<JobletDataKey> keys = Scanner.of(jobletRequests).map(JobletRequest::getJobletDataKey).list();
		Map<Long,JobletData> dataKeyToJobletData = jobletDataDao.getMulti(keys).stream()
				.collect(Collectors.toMap(jobletData -> jobletData.getKey().getId(), Function.identity()));
		return jobletRequests.stream()
				.map(jobletRequest -> {
					Long dataKey = jobletRequest.getJobletDataId();
					JobletData jobletData = dataKeyToJobletData.get(dataKey);
					return new JobletPackage(jobletRequest, jobletData);
				})
				.collect(Collectors.toList());
	}

	/*------------ queue -------------------*/

	public Optional<JobletRequest> getJobletRequestForProcessing(PhaseTimer timer, JobletType<?> type,
			String reservedBy){
		long startMs = System.currentTimeMillis();
		JobletRequestSelector selector = jobletRequestSelectorFactory.create();
		Optional<JobletRequest> jobletRequest = selector.getJobletRequestForProcessing(timer, type, reservedBy);
		long durationMs = System.currentTimeMillis() - startMs;
		if(durationMs > 1000){
			String message = jobletRequest.map(Databean::getKey).map(Object::toString).orElse("none");
			logger.warn("slow get joblet type={}, durationMs={}, got {}", type, durationMs, message);
		}
		return jobletRequest;
	}

	private void ack(JobletRequest jobletRequest){
		JobletRequestQueueKey queueKey = jobletRequestQueueManager.getQueueKey(jobletRequest);
		jobletQueueDao.getQueue(queueKey).ack(jobletRequest.getQueueMessageKey());
	}

	/*------------------- update ----------------------------*/

	public void updateStatusToRunning(JobletRequest jobletRequest, String reservedBy){
		if(jobletRequest.getStatus().isRunning()){
			//this was a timed out joblet. increment # timeouts
			jobletRequest.incrementNumTimeouts();
			if(jobletRequest.getNumTimeouts() > JobletService.MAX_JOBLET_RETRIES){
				//exceeded max retries. time out the joblet
				jobletRequest.setStatus(JobletStatus.TIMED_OUT);
			}
		}else{
			jobletRequest.setStatus(JobletStatus.RUNNING);
			jobletRequest.setReservedBy(reservedBy);
			jobletRequest.setReservedAt(System.currentTimeMillis());
		}
		jobletRequestDao.put(jobletRequest);
	}

	public void markRunningAsInterruptedOnServer(JobletType<?> jobletType, String serverName){
		Scanner<JobletRequest> jobletRequests = jobletRequestDao.scan();
		String serverNamePrefix = serverName + "_";//don't want joblet1 to include joblet10
		List<JobletRequest> jobletRequestsToReset = JobletRequest.filterByTypeStatusReservedByPrefix(jobletRequests
				.iterable(), jobletType, JobletStatus.RUNNING, serverNamePrefix);
		logger.warn("found " + jobletRequestsToReset.size() + " jobletRequests to reset");
		for(JobletRequest jobletRequest : jobletRequestsToReset){
			handleJobletInterruption(new PhaseTimer("setJobletRequestsRunningOnServerToCreated " + jobletRequest
					.toString()), jobletRequest);
		}
	}

	public long restartJoblets(JobletType<?> jobletType, JobletStatus jobletStatus){
		var numRestarted = new AtomicLong();
		jobletRequestDao.scanType(jobletType, false)
				.include(request -> request.getStatus() == jobletStatus)
				.forEach(request -> {
					request.setStatus(JobletStatus.CREATED);
					request.setNumFailures(0);
					jobletRequestDao.put(request);
					if(Objects.equals(jobletSettings.queueMechanism.get(), JobletQueueMechanism.SQS
							.getPersistentString())){
						JobletRequestQueueKey queueKey = jobletRequestQueueManager.getQueueKey(request);
						jobletQueueDao.getQueue(queueKey).put(request);
					}
					numRestarted.incrementAndGet();
					logger.warn("restarted {}", numRestarted.get());
		});
		return numRestarted.get();
	}

	/*--------------------- delete -----------------------*/

	public void deleteJobletRequestAndData(JobletRequest request){
		jobletRequestDao.delete(request.getKey());
		jobletDataDao.delete(request.getJobletDataKey());
	}

	public void deleteJobletDatasForJobletRequests(Collection<JobletRequest> jobletRequests){
		Scanner.of(jobletRequests)
				.map(JobletRequest::getJobletDataKey)
				.flush(jobletDataDao::deleteMulti);
	}

	public void deleteJoblets(Collection<JobletRequest> jobletRequests){
		deleteJobletDatasForJobletRequests(jobletRequests);
		Scanner.of(jobletRequests).map(Databean::getKey).flush(jobletRequestDao::deleteMulti);
	}

	public void deleteJobletsOfType(JobletType<?> jobletType){
		jobletRequestDao.scanType(jobletType, false)
				.batch(100)
				.forEach(this::deleteJoblets);
	}

	/*--------------------- lifecycle events -----------------------*/

	public void handleMissingJobletData(JobletRequest jobletRequest){
		jobletRequestDao.delete(jobletRequest.getKey());
		ack(jobletRequest);
		logger.warn("deleted {} due to missing JobletData", jobletRequest.getKey());
	}

	public void handleJobletInterruption(PhaseTimer timer, JobletRequest jobletRequest){
		jobletRequest.setReservedBy(null);
		jobletRequest.setReservedAt(null);
		JobletStatus setStatusTo = jobletRequest.getRestartable() ? JobletStatus.CREATED : JobletStatus.INTERRUPTED;
		jobletRequest.setStatus(setStatusTo);
		jobletRequestDao.updateOrBust(jobletRequest);
		timer.add("update JobletRequest");
		if(jobletRequest.getRestartable()){
			requeueJobletRequest(timer, jobletRequest);
		}
		logger.warn("interrupted {} set status={}, reservedBy=null, reservedAt=null", jobletRequest.getKey(),
				setStatusTo);
	}

	public void handleJobletError(PhaseTimer timer, JobletRequest jobletRequest, Exception exception, String location){
		exceptionRecorder.tryRecordException(exception, location, JobletExceptionCategory.JOBLET)
				.map(ex -> ex.id)
				.ifPresent(jobletRequest::setExceptionRecordId);
		jobletRequest.setReservedBy(null);
		jobletRequest.setReservedAt(null);
		jobletRequest.incrementNumFailures();
		boolean willRetry = jobletRequest.getRestartable() && !jobletRequest.hasReachedMaxFailures();
		JobletStatus setStatusTo = willRetry ? JobletStatus.CREATED : JobletStatus.FAILED;
		jobletRequest.setStatus(setStatusTo);
		jobletRequestDao.updateOrBust(jobletRequest);
		timer.add("update JobletRequest");
		if(willRetry){
			requeueJobletRequest(timer, jobletRequest);
		}
		logger.warn("errored {} set status={}, reservedBy=null, reservedAt=null numFailure={}", jobletRequest.getKey(),
				setStatusTo, jobletRequest.getNumFailures());
	}

	public void handleJobletCompletion(PhaseTimer timer, JobletRequest jobletRequest){
		if(jobletRequest.getQueueMessageKey() != null){
			ack(jobletRequest);
			timer.add("ack");
		}
		deleteJobletRequestAndData(jobletRequest);
		timer.add("deleteJobletRequestAndData");
	}

	private void requeueJobletRequest(PhaseTimer timer, JobletRequest jobletRequest){
		if(jobletRequest.getQueueMessageKey() == null){
			return;
		}
		// rather than ack/put, is there an ack(false) mechanism?
		// maybe https://docs.aws.amazon.com/AWSSimpleQueueService/latest/APIReference/API_ChangeMessageVisibility.html
		ack(jobletRequest);
		timer.add("requeue ack");
		JobletRequestQueueKey queueKey = jobletRequestQueueManager.getQueueKey(jobletRequest);
		jobletQueueDao.getQueue(queueKey).put(jobletRequest, new Config().setPutMethod(PutMethod.UPDATE_OR_BUST));
		timer.add("requeue put");
	}

	/*------------------------- threads --------------------------------*/

	public JobletServiceThreadCountResponse getThreadCountInfoForThisInstance(JobletType<?> jobletType){
		//get cached inputs
		List<String> serverNames = cachedWebAppInstancesOfThisServerType.getSortedServerNamesForThisWebApp();
		int clusterLimit = jobletSettings.getClusterThreadCountForJobletType(jobletType);
		int instanceLimit = jobletSettings.getThreadCountForJobletType(jobletType);
		//calculate intermediate things
		int numInstances = serverNames.size();
		if(numInstances == 0){
			return new JobletServiceThreadCountResponse(jobletType, clusterLimit, instanceLimit, 0, 0, 0, false, 0);
		}
		int minThreadsPerInstance = clusterLimit / numInstances;//round down
		int numExtraThreads = clusterLimit % numInstances;
		long jobletTypeHash = HashMethods.longDjbHash(jobletType.getPersistentString());
		double hashFractionOfOne = (double)jobletTypeHash / (double)Long.MAX_VALUE;
		int firstExtraInstanceIdx = (int)Math.floor(hashFractionOfOne * numInstances);
		//calculate effective limit
		int effectiveLimit = minThreadsPerInstance;
		boolean runExtraThread = false;
		if(minThreadsPerInstance >= instanceLimit){
			effectiveLimit = instanceLimit;
		}else{
			String thisServerName = datarouterProperties.getServerName();
			runExtraThread = IntStream.range(0, numExtraThreads)
					.mapToObj(threadIdx -> (firstExtraInstanceIdx + threadIdx) % numInstances)
					.map(serverNames::get)
					.filter(thisServerName::equals)
					.findAny()
					.isPresent();
			if(runExtraThread){
				++effectiveLimit;
			}
		}
		return new JobletServiceThreadCountResponse(jobletType, clusterLimit, instanceLimit, minThreadsPerInstance,
				numExtraThreads, firstExtraInstanceIdx, runExtraThread, effectiveLimit);
	}

	public static class JobletServiceThreadCountResponse{

		public final JobletType<?> jobletType;
		public final int clusterLimit;
		public final int instanceLimit;
		public final int minThreadsPerInstance;
		public final int numExtraThreads;
		public final int firstExtraInstanceIdxInclusive;
		public final boolean runExtraThread;
		public final int effectiveLimit;

		public JobletServiceThreadCountResponse(
				JobletType<?> jobletType,
				int clusterLimit,
				int instanceLimit,
				int minThreadsPerInstance,
				int numExtraThreads,
				int firstExtraInstanceIdxInclusive,
				boolean runExtraThread,
				int effectiveLimit){
			this.jobletType = jobletType;
			this.clusterLimit = clusterLimit;
			this.instanceLimit = instanceLimit;
			this.minThreadsPerInstance = minThreadsPerInstance;
			this.numExtraThreads = numExtraThreads;
			this.firstExtraInstanceIdxInclusive = firstExtraInstanceIdxInclusive;
			this.runExtraThread = runExtraThread;
			this.effectiveLimit = effectiveLimit;
		}

	}

}
