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
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletStatus;

public class TimeoutStuckRunningJobletRequests extends BaseHibernateOp<Integer>{

	//injected
	private final JobletNodes jobletNodes;

	private final long deleteJobletsBefore;

	public TimeoutStuckRunningJobletRequests(Datarouter datarouter, JobletNodes jobletNodes) {
		super(datarouter, jobletNodes.jobletRequest().getMaster().getClientNames());
		this.jobletNodes = jobletNodes;
		this.deleteJobletsBefore = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2);
	}

	@Override
	public Integer mergeResults(Integer fromOnce, Collection<Integer> fromEachClient) {
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

	@Override
	public Integer runOncePerClient(Client client){
		String tableName = jobletNodes.jobletRequest().getMaster().getPhysicalNodeIfApplicable().getTableName();
		String statusTimedOutFragment = JobletRequest.FieldKeys.status.getColumnName()
				+ "='" + JobletStatus.timedOut.getPersistentString() + "'";
		String statusRunningFragment = JobletRequest.FieldKeys.status.getColumnName()
				+ "='" + JobletStatus.running.getPersistentString() + "'";
		String restartableFalseFragment = JobletRequest.FieldKeys.restartable.getColumnName() + "=false";
		String reservedAtFragment = JobletRequest.FieldKeys.reservedAt.getColumnName() + "<" + deleteJobletsBefore;

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