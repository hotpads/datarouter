package com.hotpads.config.job.dao.hibernate;

import java.util.Collection;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.hotpads.config.job.databean.Joblet;
import com.hotpads.config.job.enums.JobletType;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.job.joblet.JobletNodes;
import com.hotpads.job.joblet.JobletTypeFactory;

public class UpdateJobletAndQueue extends BaseHibernateOp<Integer>{

	private final JobletTypeFactory<?> jobletTypeFactory;
	private final Joblet joblet;
	private final boolean decrementQueueIfRateLimited;
	private final Boolean rateLimited;

	public UpdateJobletAndQueue(JobletTypeFactory<?> jobletTypeFactory, Joblet joblet,
			boolean decrementQueueIfRateLimited, Datarouter datarouter, JobletNodes jobletNodes, Boolean rateLimited){
		super(datarouter, jobletNodes.joblet().getMaster().getClientNames());
		this.jobletTypeFactory = jobletTypeFactory;
		this.joblet = joblet;
		this.decrementQueueIfRateLimited = decrementQueueIfRateLimited;
		this.rateLimited = rateLimited;
	}

	@Override
	public Integer mergeResults(Integer fromOnce, Collection<Integer> fromEachClient){
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

	@Override
	public Integer runOncePerClient(Client client){
		Session session = getSession(client.getName());

		session.update(joblet);
		JobletType<?> jobletType = jobletTypeFactory.fromJoblet(joblet);
		boolean enforceRateLimit = rateLimited && jobletType.getRateLimited();

		if(this.decrementQueueIfRateLimited && enforceRateLimit){
			String queueSql = "update JobletQueue set numTickets = numTickets - 1 where id=:id";
			SQLQuery queueQuery = session.createSQLQuery(queueSql);
			queueQuery.setParameter("id", joblet.getQueueId());
			int numQueuesUpdated = queueQuery.executeUpdate();
			if(numQueuesUpdated < 1){
				getLogger().warn("JobletQueue:" + joblet.getQueueId() + " numTickets increment unsuccessful");
			}
			return numQueuesUpdated;
		}

		return 0;
	}

}
