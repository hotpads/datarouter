package com.hotpads.config.job.dao.hibernate;

import java.util.Collection;
import java.util.List;

import org.hibernate.SQLQuery;

import com.hotpads.config.job.databean.Joblet;
import com.hotpads.config.job.dto.JobletSummary;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.job.JobRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.StringTool;

@Deprecated
public class GetJobletStatuses extends BaseHibernateOp<List<JobletSummary>>{

	private final String whereStatus;
	private final boolean includeQueueId;

	public GetJobletStatuses(String whereStatus, boolean includeQueueId, Datarouter datarouter, JobRouter jobRouter) {
		super(datarouter, jobRouter.joblet.getMaster().getClientNames(), Isolation.readUncommitted, false);
		this.whereStatus = whereStatus;
		this.includeQueueId = includeQueueId;
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<JobletSummary> runOncePerClient(Client client){

		String where = "";
		if(StringTool.notEmpty(whereStatus)){
			where += " status='"+whereStatus+"'";
		}
		if(StringTool.notEmpty(where)){
			where = " where "+where;
		}

		String sql = "select " + Joblet.F.executionOrder + ", "+Joblet.F.status+", "+Joblet.F.type+", " + Joblet.F.numFailures
				+  ", count("+Joblet.F.type+")" + ", sum("+Joblet.F.numItems+"), avg("+Joblet.F.numItems+"), sum("
				+Joblet.F.numTasks+"), avg("+Joblet.F.numTasks+"), min("+Joblet.F.created+"), min("+Joblet.F.reservedAt+")";
		if(includeQueueId){
			sql = sql + ", " + Joblet.F.queueId;
		}
		sql = sql	+ " from Joblet " + where + " group by "+Joblet.F.status+", "+Joblet.F.type+", "+ Joblet.F.executionOrder +", ";
		if(includeQueueId){
			sql = sql + Joblet.F.queueId + ", ";
		}
		sql = sql + Joblet.F.numFailures + " order by "+ Joblet.F.status + ", "+ Joblet.F.type + ", " + Joblet.F.executionOrder + ", ";
		if(includeQueueId){
			sql = sql + Joblet.F.queueId + ", ";
		}
		sql = sql + Joblet.F.numFailures;
		SQLQuery sqlQuery = getSession(client.getName()).createSQLQuery(sql);
		List<Object[]> rows = sqlQuery.list();

		List<JobletSummary> summaries = ListTool.create();
		for(Object[] row : CollectionTool.nullSafe(rows)){
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
