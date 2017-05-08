package com.hotpads.joblet.dao;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Objects;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.type.JobletType;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.stream.StreamTool;

@Singleton
public class JobletRequestDao{

	private final SortedMapStorageNode<JobletRequestKey,JobletRequest> node;

	@Inject
	public JobletRequestDao(JobletNodes jobletNodes){
		this.node = jobletNodes.jobletRequest();
	}


	public JobletRequest getReservedRequest(JobletType<?> jobletType, String reservedBy){
		JobletRequestKey prefix = JobletRequestKey.create(jobletType, null, null, null);
		Config config = new Config()
				.setIterateBatchSize(20)//keep it small since there should not be thousands of reserved joblets
				.setIsolation(Isolation.readUncommitted);
		return node.streamWithPrefix(prefix, config)
				.filter(request -> Objects.equal(request.getReservedBy(), reservedBy))
				.findAny()
				.orElse(null);
	}

	public Stream<JobletRequest> streamType(JobletType<?> type, boolean slaveOk){
		JobletRequestKey prefix = JobletRequestKey.create(type, null, null, null);
		return node.streamWithPrefix(prefix, new Config().setSlaveOk(slaveOk));
	}

	public List<JobletRequest> getWithStatus(JobletStatus status){
		return node.stream(null, null)
				.filter(request -> status == request.getStatus())
				.collect(Collectors.toList());
	}

	/**
	 * Count JobletRequests of jobletType that have the jobletStatus and higher than the minPriority
	 * @return true count or countLimit if true count is eq/gt
	 */
	public int countRequests(JobletType<?> jobletType, JobletPriority minPriority, JobletStatus jobletStatus,
			int countLimit){
		// select * from Joblet where typeCode=$type and execOrder>=$highestPriority and execOrder<$minPriority
		JobletPriority startPriority = findHighestJobletPriority();
		JobletRequestKey startKey = new JobletRequestKey(jobletType, startPriority, null, null);
		JobletRequestKey endKey = new JobletRequestKey(jobletType, minPriority, null, null);

		if(!startPriority.isHigher(minPriority)){
			return 0;
		}
		Range<JobletRequestKey> range = new Range<>(startKey, true, endKey, false);
		long count = StreamTool.stream(node.scan(range, null))
				.filter(jobletRequest -> jobletRequest.getStatus() == jobletStatus)
				.limit(countLimit)
				.count();
		return Math.toIntExact(count);
	}

	private JobletPriority findHighestJobletPriority(){
		return JobletPriority.values()[0];
	}
}

