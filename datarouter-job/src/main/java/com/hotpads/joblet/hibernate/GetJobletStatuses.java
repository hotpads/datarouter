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
import com.hotpads.joblet.dto.JobletSummary;

@Deprecated
public class GetJobletStatuses extends BaseHibernateOp<List<JobletSummary>>{

	private final String whereStatus;
	private final boolean includeQueueId;

	public GetJobletStatuses(String whereStatus, boolean includeQueueId, Datarouter datarouter,
			JobletNodes jobletNodes){
		super(datarouter, jobletNodes.joblet().getMaster().getClientNames(), Isolation.readUncommitted, false);
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

		String sql = "select " + JobletRequest.F.executionOrder + ", " + JobletRequest.F.status + ", " + JobletRequest.F.type + ", "
				+ JobletRequest.F.numFailures + ", count(" + JobletRequest.F.type + ")" + ", sum(" + JobletRequest.F.numItems + "), avg("
				+ JobletRequest.F.numItems + "), sum(" + JobletRequest.F.numTasks + "), avg(" + JobletRequest.F.numTasks + "), min("
				+ JobletRequest.F.created + "), min(" + JobletRequest.F.reservedAt + ")";
		if(includeQueueId) {
			sql = sql + ", " + JobletRequest.F.queueId;
		}
		sql = sql + " from Joblet " + where + " group by " + JobletRequest.F.status + ", " + JobletRequest.F.type + ", "
				+ JobletRequest.F.executionOrder + ", ";
		if(includeQueueId){
			sql = sql + JobletRequest.F.queueId + ", ";
		}
		sql = sql + JobletRequest.F.numFailures + " order by " + JobletRequest.F.status + ", " + JobletRequest.F.type + ", "
				+ JobletRequest.F.executionOrder + ", ";
		if(includeQueueId){
			sql = sql + JobletRequest.F.queueId + ", ";
		}
		sql = sql + JobletRequest.F.numFailures;
		SQLQuery sqlQuery = getSession(client.getName()).createSQLQuery(sql);
		List<Object[]> rows = sqlQuery.list();

		List<JobletSummary> summaries = new ArrayList<>();
		for(Object[] row : DrCollectionTool.nullSafe(rows)){
			summaries.add(new JobletSummary(row));
		}

		return summaries;
	}


	@Override
	public List<JobletSummary> mergeResults(
			List<JobletSummary> fromOnce,
			Collection<List<JobletSummary>> fromEachClient) {
		return ResultMergeTool.append(fromOnce, fromEachClient);
	}

}
