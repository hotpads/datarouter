package com.hotpads.config.job.dao.hibernate;

import java.util.Collection;

import org.hibernate.SQLQuery;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.job.joblet.JobletNodes;

public class ResetJobletQueueTickets extends BaseHibernateOp<Integer>{

	public ResetJobletQueueTickets(Datarouter datarouter, JobletNodes jobletNodes) {
		super(datarouter, jobletNodes.joblet().getMaster().getClientNames());
	}

	@Override
	public Integer mergeResults(Integer fromOnce, Collection<Integer> fromEachClient) {
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

	@Override
	public Integer runOncePerClient(Client client){
		String sql = "update JobletQueue set numTickets=0 where numTickets!=0";
		SQLQuery query = getSession(client.getName()).createSQLQuery(sql);
		return query.executeUpdate();
	}

}
