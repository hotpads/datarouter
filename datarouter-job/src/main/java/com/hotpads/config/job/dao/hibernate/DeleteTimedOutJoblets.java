package com.hotpads.config.job.dao.hibernate;

import java.util.Collection;

import org.hibernate.SQLQuery;

import com.hotpads.config.job.enums.JobletStatus;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.job.JobRouter;

public class DeleteTimedOutJoblets extends BaseHibernateOp<Integer>{

	public DeleteTimedOutJoblets(Datarouter datarouter, JobRouter jobRouter) {
		super(datarouter, jobRouter.joblet.getMaster().getClientNames());
	}

	@Override
	public Integer mergeResults(Integer fromOnce, Collection<Integer> fromEachClient) {
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

	@Override
	public Integer runOncePerClient(Client client){
		String sql = "delete from Joblet where status='"+JobletStatus.timedOut.getPersistentString()+"'";
		SQLQuery query = getSession(client.getName()).createSQLQuery(sql);
		return query.executeUpdate();
	}

}
