package com.hotpads.joblet.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.SQLQuery;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.dto.JobletSummary;

@Deprecated
public class GetJobletRequestStatuses extends BaseHibernateOp<List<JobletSummary>>{

	private final String tableName;
	private final String whereStatus;
	private final boolean includeQueueId;

	public GetJobletRequestStatuses(String whereStatus, boolean includeQueueId, Datarouter datarouter,
			JobletNodes jobletNodes){
		super(datarouter, jobletNodes.jobletRequest().getMaster().getClientNames(), Isolation.readUncommitted, false);
		this.tableName = jobletNodes.jobletRequest().getMaster().getPhysicalNodeIfApplicable().getTableName();
		this.whereStatus = whereStatus;
		this.includeQueueId = includeQueueId;
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<JobletSummary> runOncePerClient(Client client){

		String where = "";
		if(DrStringTool.notEmpty(whereStatus)){
			where += " status='"+whereStatus+"'";
		}
		if(DrStringTool.notEmpty(where)){
			where = " where "+where;
		}

		String sql = "select " + JobletRequestKey.FieldKeys.executionOrder.getColumnName()
				+ ", " + JobletRequest.FieldKeys.status.getColumnName()
				+ ", " + JobletRequestKey.FieldKeys.type.getColumnName()
				+ ", " + JobletRequest.FieldKeys.numFailures.getColumnName()
				+ ", count(" + JobletRequestKey.FieldKeys.type.getColumnName() + ")"
				+ ", sum(" + JobletRequest.FieldKeys.numItems.getColumnName() + ")"
				+ ", avg(" + JobletRequest.FieldKeys.numItems.getColumnName() + ")"
				+ ", sum(" + JobletRequest.FieldKeys.numTasks.getColumnName() + ")"
				+ ", avg(" + JobletRequest.FieldKeys.numTasks.getColumnName() + ")"
				+ ", min(" + JobletRequestKey.FieldKeys.created.getColumnName() + ")"
				+ ", min(" + JobletRequest.FieldKeys.reservedAt.getColumnName() + ")";
		if(includeQueueId) {
			sql = sql + ", " + JobletRequest.FieldKeys.queueId.getColumnName();
		}
		sql = sql + " from " + tableName
				+ " " + where
				+ " group by " + JobletRequest.FieldKeys.status.getColumnName()
				+ ", " + JobletRequestKey.FieldKeys.type.getColumnName()
				+ ", " + JobletRequestKey.FieldKeys.executionOrder.getColumnName()
				+ ", ";
		if(includeQueueId){
			sql = sql + JobletRequest.FieldKeys.queueId.getColumnName()
					+ ", ";
		}
		sql = sql + JobletRequest.FieldKeys.numFailures
				+ " order by " + JobletRequest.FieldKeys.status.getColumnName()
				+ ", " + JobletRequestKey.FieldKeys.type.getColumnName()
				+ ", " + JobletRequestKey.FieldKeys.executionOrder.getColumnName()
				+ ", ";
		if(includeQueueId){
			sql = sql + JobletRequest.FieldKeys.queueId.getColumnName() + ", ";
		}
		sql = sql + JobletRequest.FieldKeys.numFailures.getColumnName();
		SQLQuery sqlQuery = getSession(client.getName()).createSQLQuery(sql);
		List<Object[]> rows = sqlQuery.list();

		List<JobletSummary> summaries = new ArrayList<>();
		for(Object[] row : DrCollectionTool.nullSafe(rows)){
			summaries.add(new JobletSummary(row));
		}

		return summaries;
	}


	@Override
	public List<JobletSummary> mergeResults(List<JobletSummary> fromOnce,
			Collection<List<JobletSummary>> fromEachClient){
		return ResultMergeTool.append(fromOnce, fromEachClient);
	}

}
