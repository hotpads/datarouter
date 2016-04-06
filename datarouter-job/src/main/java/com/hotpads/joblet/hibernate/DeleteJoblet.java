package com.hotpads.joblet.hibernate;

import java.util.Collection;

import org.hibernate.Session;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;

public class DeleteJoblet extends BaseHibernateOp<JobletRequest>{

	private final JobletTypeFactory jobletTypeFactory;
	private final JobletRequest  joblet;
	private final JobletNodes jobletNodes;
	private final Boolean rateLimited;

	public DeleteJoblet(Datarouter datarouter, JobletTypeFactory jobletTypeFactory, JobletRequest joblet,
			JobletNodes jobletNodes, Boolean rateLimited) {
		super(datarouter, jobletNodes.joblet().getMaster().getClientNames(), Isolation.repeatableRead, false);
		this.jobletTypeFactory = jobletTypeFactory;
		this.joblet = joblet;
		this.jobletNodes = jobletNodes;
		this.rateLimited = rateLimited;
	}

	@Override
	public JobletRequest runOncePerClient(Client client){
		Session session = this.getSession(client.getName());
		JobletType<?> jobletType = jobletTypeFactory.fromJobletRequest(joblet);
		boolean enforceRateLimit = rateLimited && jobletType.getRateLimited();

		if(enforceRateLimit) {
			GetJobletForProcessing.updateQueueTickets(session, joblet.getQueueId(), -1, getLogger());
		}

		jobletNodes.joblet().delete(joblet.getKey(), null);
		jobletNodes.jobletData().delete(joblet.getJobletDataKey(), null);

		return null;
	}

	@Override
	public JobletRequest mergeResults(JobletRequest fromOnce, Collection<JobletRequest> fromEachClient) {
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

}
