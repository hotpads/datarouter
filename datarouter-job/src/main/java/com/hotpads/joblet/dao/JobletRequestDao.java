package com.hotpads.joblet.dao;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Objects;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.JobletType;

@Singleton
public class JobletRequestDao{

	@Inject
	private JobletNodes jobletNodes;

	public JobletRequest getReservedRequest(JobletType<?> jobletType, String reservedBy){
		JobletRequestKey prefix = JobletRequestKey.create(jobletType, null, null, null);
		Config config = new Config()
				.setIterateBatchSize(20)//keep it small since there should not be thousands of reserved joblets
				.setIsolation(Isolation.readUncommitted);
		return jobletNodes.jobletRequest().streamWithPrefix(prefix, config)
				.filter(request -> Objects.equal(request.getReservedBy(), reservedBy))
				.findAny()
				.orElse(null);
	}

}
