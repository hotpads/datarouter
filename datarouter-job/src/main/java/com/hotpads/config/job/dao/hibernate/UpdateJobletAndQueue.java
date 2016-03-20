package com.hotpads.config.job.dao.hibernate;

import java.util.Collection;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.hotpads.config.job.databean.Joblet;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.job.JobRouter;

public class UpdateJobletAndQueue extends BaseHibernateOp<Integer>{

	private Joblet joblet;
	private boolean decrementQueueIfRateLimited = true;
	private Boolean rateLimited;

	public UpdateJobletAndQueue(Joblet joblet, boolean decrementQueueIfRateLimited, Datarouter datarouter,
			JobRouter jobRouter, Boolean rateLimited){
		super(datarouter, jobRouter.joblet.getMaster().getClientNames());
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
		boolean enforceRateLimit = rateLimited && joblet.getType().getRateLimited();

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
