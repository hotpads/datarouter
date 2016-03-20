package com.hotpads.config.job.dao.hibernate;

import java.util.Collection;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.hotpads.config.job.enums.JobletStatus;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.job.JobRouter;

public class RestartJoblets extends BaseHibernateOp<Integer>{

	private JobletStatus currentStatus;

	public RestartJoblets(Datarouter datarouter, JobRouter jobRouter, JobletStatus currentStatus) {
		super(datarouter, jobRouter.joblet.getMaster().getClientNames());
		this.currentStatus = currentStatus;
	}

	@Override
	public Integer mergeResults(Integer fromOnce, Collection<Integer> fromEachClient){
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

	@Override
	public Integer runOncePerClient(Client client){
		String sql;
		if(currentStatus.getPersistentString().equals(JobletStatus.timedOut.getPersistentString())){
			sql = "update Joblet set status=:status, numFailures=0 where status=:currentStatus and type != 'FeedImport'";
		}
		else{
			sql = "update Joblet set status=:status, numFailures=0 where status=:currentStatus";
		}

		Session session = this.getSession(client.getName());

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("status", JobletStatus.created.getPersistentString());
		query.setParameter("currentStatus", currentStatus.getPersistentString());
		int numReserved = query.executeUpdate();
		return numReserved;
	}

}
