package com.hotpads.joblet.hibernate;

import java.util.Collection;

import org.hibernate.SQLQuery;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.enums.JobletStatus;

public class DeleteTimedOutJobletRequests extends BaseHibernateOp<Integer>{

	private final String tableName;

	public DeleteTimedOutJobletRequests(Datarouter datarouter, JobletNodes jobletNodes) {
		super(datarouter, jobletNodes.jobletRequest().getMaster().getClientNames());
		this.tableName = jobletNodes.jobletRequest().getMaster().getPhysicalNodeIfApplicable().getTableName();
	}

	@Override
	public Integer mergeResults(Integer fromOnce, Collection<Integer> fromEachClient) {
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

	@Override
	public Integer runOncePerClient(Client client){
		String sql = "delete from " + tableName + " where status='" + JobletStatus.timedOut.getPersistentString() + "'";
		SQLQuery query = getSession(client.getName()).createSQLQuery(sql);
		return query.executeUpdate();
	}

}
