package com.hotpads.joblet.hibernate;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletStatus;

public class TimeoutStuckRunningJobletRequests extends BaseJdbcOp<Integer>{

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

		return JdbcTool.update(getConnection(client.getName()), sql);
	}

}
