package com.hotpads.config.job.dao.hibernate;

import java.util.Collection;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.config.job.databean.Joblet;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.job.joblet.JobletNodes;

public class DeleteJoblet extends BaseHibernateOp<Joblet>{
	private static final Logger logger = LoggerFactory.getLogger(DeleteJoblet.class);

	private Joblet  joblet;
	private JobletNodes jobletNodes;
	private Boolean rateLimited;

	public DeleteJoblet(Datarouter datarouter, Joblet joblet, JobletNodes jobletNodes, Boolean rateLimited) {
		super(datarouter, jobletNodes.joblet().getMaster().getClientNames(), Isolation.repeatableRead, false);
		this.joblet = joblet;
		this.jobletNodes = jobletNodes;
		this.rateLimited = rateLimited;
	}

	@Override
	public Joblet runOncePerClient(Client client){
		Session session = this.getSession(client.getName());
		boolean enforceRateLimit = rateLimited && joblet.getType().getRateLimited();

		if(enforceRateLimit){
		//	if(jobRouter.jobletQueue.get(new JobletQueueKey(joblet.getQueueId()), Configs.SLAVE_OK).getNumTickets() > 0){
				GetJobletForProcessing.updateQueueTickets(session, joblet.getQueueId(), -1,
						getLogger());
			//}
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
