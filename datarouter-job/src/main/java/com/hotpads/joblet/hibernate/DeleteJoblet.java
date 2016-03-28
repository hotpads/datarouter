package com.hotpads.joblet.hibernate;

import java.util.Collection;

import org.hibernate.Session;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletType;
import com.hotpads.joblet.JobletTypeFactory;
import com.hotpads.joblet.databean.Joblet;

public class DeleteJoblet extends BaseHibernateOp<Joblet>{

	private final JobletTypeFactory jobletTypeFactory;
	private final Joblet  joblet;
	private final JobletNodes jobletNodes;
	private final Boolean rateLimited;

	public DeleteJoblet(Datarouter datarouter, JobletTypeFactory jobletTypeFactory, Joblet joblet,
			JobletNodes jobletNodes, Boolean rateLimited) {
		super(datarouter, jobletNodes.joblet().getMaster().getClientNames(), Isolation.repeatableRead, false);
		this.jobletTypeFactory = jobletTypeFactory;
		this.joblet = joblet;
		this.jobletNodes = jobletNodes;
		this.rateLimited = rateLimited;
	}

	@Override
	public Joblet runOncePerClient(Client client){
		Session session = this.getSession(client.getName());
		JobletType<?> jobletType = jobletTypeFactory.fromJoblet(joblet);
		boolean enforceRateLimit = rateLimited && jobletType.getRateLimited();

		if(enforceRateLimit) {
			GetJobletForProcessing.updateQueueTickets(session, joblet.getQueueId(), -1, getLogger());
		}

		jobletNodes.joblet().delete(joblet.getKey(), null);
		jobletNodes.jobletData().delete(joblet.getJobletDataKey(), null);

		return null;
	}

	@Override
	public Joblet mergeResults(Joblet fromOnce, Collection<Joblet> fromEachClient) {
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

}
