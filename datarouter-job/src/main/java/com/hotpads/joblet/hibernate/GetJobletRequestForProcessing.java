package com.hotpads.joblet.hibernate;

import java.util.Collection;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletType;

public class GetJobletRequestForProcessing extends BaseHibernateOp<JobletRequest>{

	private final String tableName;
	private final Long reservationTimeout;
	private final String reservedBy;
	private final JobletType<?> jobletType;
	private final int maxRetries;

	public GetJobletRequestForProcessing(Long reservationTimeout, int maxRetries, String reservedBy,
			JobletType<?> jobletType, Datarouter datarouter, JobletNodes jobletNodes){
		super(datarouter, jobletNodes.jobletRequest().getMaster().getClientNames(), Isolation.repeatableRead, false);
		this.tableName = jobletNodes.jobletRequest().getMaster().getPhysicalNodeIfApplicable().getTableName();
		this.reservationTimeout = reservationTimeout;
		this.reservedBy = reservedBy;
		this.jobletType = jobletType;
		this.maxRetries = maxRetries;
	}

	@Override
	public JobletRequest mergeResults(JobletRequest fromOnce, Collection<JobletRequest> fromEachClient){
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

	@Override
	public JobletRequest runOncePerClient(Client client){
		Session session = getSession(client.getName());
		JobletRequest joblet = getOneJoblet(session);
		while(joblet!=null && ! joblet.getStatus().isRunning()){
			joblet = getOneJoblet(session);
		}
		return joblet;
	}

	@SuppressWarnings("unchecked")
	public JobletRequest getOneJoblet(Session session){
		String typeClause = " type='" + jobletType.getPersistentString() + "' and ";
		String projectionClause = "{j.*}";
		String statusAndRateLimitClause = "status='"+JobletStatus.created.getPersistentString()+"'";
		String jobletQueueTable = "";
		String readyClause = statusAndRateLimitClause;
		String timedOutClause = "";

		if(getReservedBeforeMs()!=null){
			timedOutClause = "(status='"+JobletStatus.running.getPersistentString()+"'"
				+" and reservedAt < "+getReservedBeforeMs().toString()
				+" and restartable=true)";

			readyClause = " ("+statusAndRateLimitClause+" or "+timedOutClause+") ";
		}

		@SuppressWarnings("unused")
		String orderByClause = " order by " + JobletRequestKey.FieldKeys.type.getColumnName()
				+ ", " + JobletRequestKey.FieldKeys.executionOrder.getColumnName()
				+ ", " + JobletRequestKey.FieldKeys.created.getColumnName()
				+ ", " + JobletRequestKey.FieldKeys.batchSequence.getColumnName()
				+ " ";

		String reserveSql = "select "+projectionClause
			+ " from " + tableName + " j" + jobletQueueTable
			+" where "
			+ typeClause
			+ readyClause
//			+ orderByClause //already ordered by PK, explicit ordering will trigger a filesort (bad)
			//apparently can't use a limit clause on a multi-table update statement, so do select first
			+ " limit 1 for update";

		SQLQuery reserveQuery = session.createSQLQuery(reserveSql);
		reserveQuery.addEntity("j", JobletRequest.class);
		JobletRequest joblet = (JobletRequest)DrCollectionTool.getFirst(reserveQuery.list());
		if(joblet == null) {
			return null;
		}

		//update the joblet
		if(joblet.getStatus().isRunning()){
			//this was a timed out joblet. increment # timeouts
			joblet.setNumTimeouts(DrNumberTool.nullSafe(joblet.getNumTimeouts())+1);
			if(joblet.getNumTimeouts()>maxRetries){
				//exceeded max retries. time out the joblet
				joblet.setStatus(JobletStatus.timedOut);
			}
		}else{
			joblet.setStatus(JobletStatus.running);
			//joblet.setReservedAt(System.currentTimeMillis());
			joblet.setReservedBy(reservedBy);
		}
		session.update(joblet);
		session.flush();

		return joblet;
	}

	private Long getReservedBeforeMs(){
		if(reservationTimeout == null) {
			return null;
		}
		return System.currentTimeMillis() - reservationTimeout;
	}

}