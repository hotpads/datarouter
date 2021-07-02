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
package io.datarouter.joblet.storage.jobletrequest;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest.JobletRequestFielder;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.ConfigValue;
import io.datarouter.storage.config.Configs;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.IndexingNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.node.type.index.UniqueIndexNode;
import io.datarouter.util.tuple.Range;
import io.datarouter.virtualnode.redundant.RedundantIndexedSortedMapStorageNode;

@Singleton
public class DatarouterJobletRequestDao extends BaseDao{

	public static class DatarouterJobletRequestDaoParams extends BaseRedundantDaoParams{

		public DatarouterJobletRequestDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final IndexedSortedMapStorageNode<JobletRequestKey,JobletRequest,JobletRequestFielder> node;
	private final UniqueIndexNode<
			JobletRequestKey,
			JobletRequest,
			JobletRequestByTypeAndDataSignatureKey,
			FieldlessIndexEntry<
					JobletRequestByTypeAndDataSignatureKey,
					JobletRequestKey,
					JobletRequest>> byTypeAndDataSignature;

	@Inject
	public DatarouterJobletRequestDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterJobletRequestDaoParams params,
			IndexingNodeFactory indexingNodeFactory){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					IndexedSortedMapStorageNode<JobletRequestKey,JobletRequest,JobletRequestFielder> node =
							nodeFactory.create(clientId, JobletRequest::new, JobletRequestFielder::new)
						.disableNodewatchPercentageAlert()
						.disableNodewatchThresholdAlert()
						.withIsSystemTable(true)
						.build();
					return node;
				})
				.listTo(RedundantIndexedSortedMapStorageNode::new);
		byTypeAndDataSignature = indexingNodeFactory.createKeyOnlyManagedIndex(
				JobletRequestByTypeAndDataSignatureKey.class,
				node)
				.build();
		datarouter.register(node);
	}

	public Scanner<JobletRequest> scan(){
		return node.scan();
	}

	public Scanner<JobletRequest> scan(Range<JobletRequestKey> range){
		return node.scan(range);
	}

	public Scanner<JobletRequest> scan(Range<JobletRequestKey> range, int outputBatchSize){
		return node.scan(range, new Config().setOutputBatchSize(outputBatchSize));
	}

	public Scanner<JobletRequest> scanAnyDelay(){
		return node.scan(Configs.anyDelay());
	}

	public String getName(){
		return node.getName();
	}

	public Scanner<JobletRequest> scanWithPrefix(JobletRequestKey prefix){
		return node.scanWithPrefix(prefix);
	}

	public void deleteMulti(Collection<JobletRequestKey> keys){
		node.deleteMulti(keys);
	}

	public void putMulti(Collection<JobletRequest> databeans){
		node.putMulti(databeans);
	}

	public void put(JobletRequest databean){
		node.put(databean);
	}

	public SortedMapStorageNode<JobletRequestKey,JobletRequest,JobletRequestFielder> getNode(){
		return node;
	}

	public boolean exists(JobletRequestKey key){
		return node.exists(key);
	}

	public void delete(JobletRequestKey key){
		node.delete(key);
	}

	public void updateOrBust(JobletRequest databean){
		node.put(databean, new Config().setPutMethod(PutMethod.UPDATE_OR_BUST));
	}

	public void putMultiOrBust(Collection<JobletRequest> databeans){
		node.putMulti(databeans, Configs.insertOrBust());
	}

	public JobletRequest getReservedRequest(JobletType<?> jobletType, String reservedBy, ConfigValue<?> option){
		var prefix = JobletRequestKey.create(jobletType, null, null, null);
		var config = new Config()
				.setOutputBatchSize(20)//keep it small since there should not be thousands of reserved joblets
				.addOption(option);
		return node.scanWithPrefix(prefix, config)
				.include(request -> Objects.equals(request.getReservedBy(), reservedBy))
				.findFirst()
				.orElse(null);
	}

	public Scanner<JobletRequest> scanType(JobletType<?> type, boolean anyDelay){
		var prefix = JobletRequestKey.create(type, null, null, null);
		var config = new Config().setAnyDelay(anyDelay);
		return node.scanWithPrefix(prefix, config);
	}

	public Scanner<JobletRequest> scanTypePriority(JobletType<?> type, JobletPriority priority, boolean anyDelay){
		var prefix = JobletRequestKey.create(type, priority.getExecutionOrder(), null, null);
		var config = new Config().setAnyDelay(anyDelay);
		return node.scanWithPrefix(prefix, config);
	}

	public List<JobletRequest> getWithStatus(JobletStatus status){
		return node.scan()
				.include(request -> status == request.getStatus())
				.list();
	}

	public boolean anyExistOfType(JobletType<?> type){
		var prefix = new JobletRequestKey(type.getPersistentString(), null, null, null);
		var config = new Config().setLimit(1);
		return node.scanWithPrefix(prefix, config).hasAny();
	}

	/**
	 * Scan JobletRequests that have
	 *
	 * @param jobletType the given jobletType
	 * @param minPriority higher than or equal to the minPriority
	 * @param jobletStatus the given jobletStatus
	 */
	public Scanner<JobletRequest> scanJobletRequestsWithHigherOrEqualPriority(
			JobletType<?> jobletType,
			JobletPriority minPriority,
			JobletStatus jobletStatus){
		var startPriority = JobletPriority.getHighestPriority();
		var startKey = new JobletRequestKey(jobletType, startPriority, null, null);
		var endKey = new JobletRequestKey(jobletType, minPriority, Long.MAX_VALUE, Integer.MAX_VALUE);
		var jobletRequestKeyRange = new Range<>(startKey, true, endKey, true);
		return node.scan(jobletRequestKeyRange)
				.include(jobletRequest -> jobletRequest.getStatus() == jobletStatus);
	}

	/**
	 * Count JobletRequests that have
	 *
	 * @param jobletType the given jobletType
	 * @param minPriority higher than or equal to the minPriority
	 * @param jobletStatus the given jobletStatus
	 * @param countLimit countLimit
	 *
	 * @return true count or countLimit if true count is eq/gt
	 */
	public long countJobletRequestsWithHigherOrEqualPriority(
			JobletType<?> jobletType,
			JobletPriority minPriority,
			JobletStatus jobletStatus,
			long countLimit){
		return scanJobletRequestsWithHigherOrEqualPriority(jobletType, minPriority, jobletStatus)
				.limit(countLimit)
				.count();
	}

	public long countGroup(JobletType<?> type, JobletPriority priority, String groupId, boolean anyDelay){
		return scanTypePriority(type, priority, anyDelay)
				.include(jobletRequest -> Objects.equals(jobletRequest.getGroupId(), groupId))
				.count();
	}

	public Scanner<JobletRequest> scanFailedJoblets(){
		return node.scan()
				.include(JobletRequest::hasReachedMaxFailures);
	}

	public boolean isDataAlreadyInQueue(JobletRequest jobletRequest){
		return byTypeAndDataSignature.scanKeysWithPrefix(
				new JobletRequestByTypeAndDataSignatureKey(
						jobletRequest.getKey().getType(),
						jobletRequest.getDataSignature(),
						null,
						null,
						null),
				new Config().setLimit(1))
			.hasAny();
	}

	public List<JobletRequest> filterForDataNotAlreadyInQueue(List<JobletRequest> jobletRequests){
		String type = assertSameType(jobletRequests);
		Collection<Long> alreadyInQueue = Scanner.of(jobletRequests)
				.map(JobletRequest::getDataSignature)
				.map(dataSignature -> new JobletRequestByTypeAndDataSignatureKey(
						type,
						dataSignature,
						null,
						null,
						null))
				.listTo(byTypeAndDataSignature::scanKeysWithPrefixes)
				.map(JobletRequestByTypeAndDataSignatureKey::getDataSignature)
				.collect(HashSet::new);
		return Scanner.of(jobletRequests)
				.exclude(jobletRequest -> alreadyInQueue.contains(jobletRequest.getDataSignature()))
				.list();
	}

	private String assertSameType(List<JobletRequest> jobletRequests){
		String firstType = jobletRequests.get(0).getKey().getType();
		for(JobletRequest jobletRequest : jobletRequests){
			String type = jobletRequest.getKey().getType();
			if(!type.equals(firstType)){
				throw new RuntimeException(type + " is not a " + firstType);
			}
		}
		return firstType;
	}

}
