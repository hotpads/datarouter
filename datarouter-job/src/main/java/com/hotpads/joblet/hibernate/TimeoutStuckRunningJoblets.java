package com.hotpads.joblet.hibernate;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletStatus;
import com.hotpads.joblet.databean.Joblet;

public class TimeoutStuckRunningJoblets extends BaseHibernateOp<Integer>{

	//injected
	private final JobletNodes jobletNodes;

	private final long deleteJobletsBefore;

	public TimeoutStuckRunningJoblets(Datarouter datarouter, JobletNodes jobletNodes) {
		super(datarouter, jobletNodes.joblet().getMaster().getClientNames());
		this.jobletNodes = jobletNodes;
		this.deleteJobletsBefore = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2);
	}

	@Override
	public Integer mergeResults(Integer fromOnce, Collection<Integer> fromEachClient) {
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

	@Override
	public Integer runOncePerClient(Client client){
		String tableName = jobletNodes.joblet().getMaster().getPhysicalNodeIfApplicable().getTableName();
		String statusTimedOutFragment = Joblet.F.status + "='" + JobletStatus.timedOut.getPersistentString() + "'";
		String statusRunningFragment = Joblet.F.status + "='" + JobletStatus.running.getPersistentString() + "'";
		String restartableFalseFragment = Joblet.F.restartable + "=false";
		String reservedAtFragment = Joblet.F.reservedAt + "<" + deleteJobletsBefore;

		String sql = "update " + tableName + " set"
				+ " " + statusTimedOutFragment
				+ " where "
				+ statusRunningFragment
				+ " and " + restartableFalseFragment
				+ " and " + reservedAtFragment;

		Session session = getSession(client.getName());
		SQLQuery query = session.createSQLQuery(sql);
		int numTimedOut = query.executeUpdate();
		return numTimedOut;
	}

}