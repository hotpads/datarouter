package com.hotpads.joblet.hibernate;

import java.util.Collection;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletTypeFactory;

public class DeleteJobletRequest extends BaseHibernateOp<JobletRequest>{

	private final JobletRequest  jobletRequest;
	private final JobletNodes jobletNodes;

	public DeleteJobletRequest(Datarouter datarouter, JobletTypeFactory jobletTypeFactory, JobletRequest jobletRequest,
			JobletNodes jobletNodes) {
		super(datarouter, jobletNodes.jobletRequest().getMaster().getClientNames(), Isolation.repeatableRead, false);
		this.jobletRequest = jobletRequest;
		this.jobletNodes = jobletNodes;
	}

	@Override
	public JobletRequest runOncePerClient(Client client){
		jobletNodes.jobletRequest().delete(jobletRequest.getKey(), null);
		jobletNodes.jobletData().delete(jobletRequest.getJobletDataKey(), null);
		return null;
	}

	@Override
	public JobletRequest mergeResults(JobletRequest fromOnce, Collection<JobletRequest> fromEachClient) {
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

}
