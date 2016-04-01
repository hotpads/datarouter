package com.hotpads.joblet.hibernate;

import java.util.Collection;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletStatus;

public class RestartJoblets extends BaseHibernateOp<Integer>{

	private final String tableName;
	private final JobletStatus currentStatus;

	public RestartJoblets(Datarouter datarouter, JobletNodes jobletNodes, JobletStatus currentStatus) {
		super(datarouter, jobletNodes.joblet().getMaster().getClientNames());
		this.tableName = jobletNodes.joblet().getMaster().getPhysicalNodeIfApplicable().getTableName();
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
			sql = "update " + tableName + " set status=:status, numFailures=0"
					+ " where status=:currentStatus and type != 'FeedImport'";
		}else{
			sql = "update " + tableName + " set status=:status, numFailures=0 where status=:currentStatus";
		}

		Session session = this.getSession(client.getName());

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("status", JobletStatus.created.getPersistentString());
		query.setParameter("currentStatus", currentStatus.getPersistentString());
		int numReserved = query.executeUpdate();
		return numReserved;
	}

}
