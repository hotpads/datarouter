package com.hotpads.config.job.dao.hibernate;

import java.util.Collection;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.hotpads.config.job.databean.Joblet;
import com.hotpads.config.job.enums.JobletStatus;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.job.JobRouter;
import com.hotpads.util.core.DateTool;

public class TimeoutStuckRunningJoblets extends BaseHibernateOp<Integer>{

	public static final long DELETE_JOBLETS_BEFORE = System.currentTimeMillis() - (DateTool.MILLISECONDS_IN_DAY * 2);

	public TimeoutStuckRunningJoblets(Datarouter datarouter, JobRouter jobRouter) {
		super(datarouter, jobRouter.joblet.getMaster().getClientNames());
	}

	@Override
	public Integer mergeResults(Integer fromOnce, Collection<Integer> fromEachClient) {
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}

	@Override
	public Integer runOncePerClient(Client client){
		String statusTimedOutFragment = Joblet.F.status+"='"+JobletStatus.timedOut.getPersistentString()+"'";
		String statusRunningFragment = Joblet.F.status+"='"+JobletStatus.running.getPersistentString()+"'";
		String restartableFalseFragment = Joblet.F.restartable+"=false";
		String reservedAtFragment = "reservedAt<"+DELETE_JOBLETS_BEFORE;

		String sql = "update Joblet set"
				+" "+statusTimedOutFragment
				+" where "
				+statusRunningFragment
				+" and "+restartableFalseFragment
				+" and "+reservedAtFragment;

		Session session = this.getSession(client.getName());
		SQLQuery query = session.createSQLQuery(sql);
		int numTimedOut = query.executeUpdate();
		return numTimedOut;
	}

}
