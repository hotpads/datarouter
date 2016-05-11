package com.hotpads.joblet.hibernate;

import java.util.Collection;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;

public class UpdateJobletRequestAndQueue extends BaseHibernateOp<Integer>{

	private final JobletTypeFactory jobletTypeFactory;
	private final JobletRequest jobletRequest;
	private final boolean decrementQueueIfRateLimited;
	private final Boolean rateLimited;

	public UpdateJobletRequestAndQueue(JobletTypeFactory jobletTypeFactory, JobletRequest jobletRequest,
			boolean decrementQueueIfRateLimited, Datarouter datarouter, JobletNodes jobletNodes, Boolean rateLimited){
		super(datarouter, jobletNodes.joblet().getMaster().getClientNames());
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletRequest = jobletRequest;
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

		session.update(jobletRequest);
		JobletType<?> jobletType = jobletTypeFactory.fromJobletRequest(jobletRequest);
		boolean enforceRateLimit = rateLimited && jobletType.getRateLimited();

		if(this.decrementQueueIfRateLimited && enforceRateLimit){
			String queueSql = "update JobletQueue set numTickets = numTickets - 1 where id=:id";
			SQLQuery queueQuery = session.createSQLQuery(queueSql);
			queueQuery.setParameter("id", jobletRequest.getQueueId());
			int numQueuesUpdated = queueQuery.executeUpdate();
			if(numQueuesUpdated < 1){
				getLogger().warn("JobletQueue:" + jobletRequest.getQueueId() + " numTickets increment unsuccessful");
			}
			return numQueuesUpdated;
		}

		return 0;
	}

}
