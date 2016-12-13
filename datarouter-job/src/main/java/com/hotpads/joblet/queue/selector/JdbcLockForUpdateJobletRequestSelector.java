package com.hotpads.joblet.queue.selector;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.jdbc.GetJobletRequest;
import com.hotpads.joblet.jdbc.JobletRequestSqlBuilder;
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
	private JdbcFieldCodecFactory jdbcFieldCodecFactory;
	@Inject
	private JobletRequestSqlBuilder jobletRequestSqlBuilder;

	@Override
	public Optional<JobletRequest> getJobletRequestForProcessing(PhaseTimer timer, JobletType<?> type,
			String reservedBy){
		while(true){
			GetJobletRequest jdbcOp = new GetJobletRequest(reservedBy, type, datarouter, jobletNodes,
					jdbcFieldCodecFactory, jobletRequestSqlBuilder);
			JobletRequest jobletRequest = datarouter.run(jdbcOp);
			timer.add("GetJobletRequest");
			if(jobletRequest == null){
				return Optional.empty();
			}
			if( ! jobletRequest.getStatus().isRunning()){
				continue;//weird flow.  it was probably just marked as timedOut, so skip it
			}
			return Optional.of(jobletRequest);
		}
	}

}
