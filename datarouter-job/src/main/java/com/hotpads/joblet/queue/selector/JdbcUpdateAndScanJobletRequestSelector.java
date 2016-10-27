package com.hotpads.joblet.queue.selector;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.dao.JobletRequestDao;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.jdbc.JobletRequestSqlBuilder;
import com.hotpads.joblet.jdbc.ReserveJobletRequest;
import com.hotpads.joblet.queue.JobletRequestSelector;

@Singleton
public class JdbcUpdateAndScanJobletRequestSelector implements JobletRequestSelector{

	@Inject
	private Datarouter datarouter;
	@Inject
	private JobletNodes jobletNodes;
	@Inject
	private JobletRequestDao jobletRequestDao;
	@Inject
	private JobletRequestSqlBuilder jobletRequestSqlBuilder;

	@Override
	public Optional<JobletRequest> getJobletRequestForProcessing(JobletType<?> type, String reservedBy){
		ReserveJobletRequest jdbcOp = new ReserveJobletRequest(reservedBy, type, datarouter, jobletNodes,
				jobletRequestSqlBuilder);
		while(datarouter.run(jdbcOp)){//returns false if no joblet found
			JobletRequest jobletRequest = jobletRequestDao.getReservedRequest(type, reservedBy);
			if(JobletStatus.created == jobletRequest.getStatus()){
				jobletRequest.setStatus(JobletStatus.running);
				jobletNodes.jobletRequest().put(jobletRequest, null);
				return Optional.of(jobletRequest);
			}

			//we got a previously timed-out joblet
			jobletRequest.incrementNumTimeouts();
			if(jobletRequest.getNumTimeouts() <= JobletService.MAX_JOBLET_RETRIES){
				jobletNodes.jobletRequest().put(jobletRequest, null);
				return Optional.of(jobletRequest);
			}

			jobletRequest.setStatus(JobletStatus.timedOut);
			jobletNodes.jobletRequest().put(jobletRequest, null);
			//loop around for another
		}
		return Optional.empty();
	}

}
