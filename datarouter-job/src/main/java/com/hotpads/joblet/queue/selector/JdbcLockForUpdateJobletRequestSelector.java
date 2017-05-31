package com.hotpads.joblet.queue.selector;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.imp.mysql.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.joblet.JobletCounters;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.jdbc.GetJobletRequest;
import com.hotpads.joblet.jdbc.JobletRequestSqlBuilder;
import com.hotpads.joblet.queue.JobletRequestQueueKey;
import com.hotpads.joblet.queue.JobletRequestQueueManager;
import com.hotpads.joblet.queue.JobletRequestSelector;
import com.hotpads.joblet.type.JobletType;
import com.hotpads.util.core.profile.PhaseTimer;

@Singleton
public class JdbcLockForUpdateJobletRequestSelector implements JobletRequestSelector{

	@Inject
	private Datarouter datarouter;
	@Inject
	private JobletNodes jobletNodes;
	@Inject
	private JdbcFieldCodecFactory mysqlFieldCodecFactory;
	@Inject
	private JobletRequestSqlBuilder jobletRequestSqlBuilder;
	@Inject
	private JobletRequestQueueManager jobletRequestQueueManager;
	@Inject
	private JobletCounters jobletCounters;


	@Override
	public Optional<JobletRequest> getJobletRequestForProcessing(PhaseTimer timer, JobletType<?> type,
			String reservedBy){
		while(true){
			GetJobletRequest mysqlOp = new GetJobletRequest(reservedBy, type, datarouter, jobletNodes,
					mysqlFieldCodecFactory, jobletRequestSqlBuilder);
			JobletRequest jobletRequest = datarouter.run(mysqlOp);
			timer.add("GetJobletRequest");
			if(jobletRequest == null){
				jobletRequestQueueManager.onJobletRequestMissForAllPriorities(type);//for back-off
				return Optional.empty();
			}
			if(!jobletRequest.getStatus().isRunning()){
				continue;// weird flow. it was probably just marked as timedOut, so skip it
			}
			JobletRequestQueueKey queueKey = new JobletRequestQueueKey(type, jobletRequest.getKey().getPriority());
			jobletCounters.incQueueHit(queueKey.getQueueName());
			return Optional.of(jobletRequest);
		}
	}

}
