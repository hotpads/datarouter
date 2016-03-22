package com.hotpads.config.job.dao.hibernate;

import java.util.Collection;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;

import com.hotpads.config.job.databean.Joblet;
import com.hotpads.config.job.databean.JobletQueue;
import com.hotpads.config.job.enums.JobletStatus;
import com.hotpads.config.job.enums.JobletType;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.util.ResultMergeTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.job.joblet.JobletNodes;

public class GetJobletForProcessing extends BaseHibernateOp<Joblet>{

	private final Long reservationTimeout;
	private final String reservedBy;
	private final JobletType<?> jobletType;
	private final int maxRetries;
	private final boolean rateLimited;

	public GetJobletForProcessing(Long reservationTimeout, int maxRetries, String reservedBy, JobletType<?> jobletType,
			Datarouter datarouter, JobletNodes jobletNodes, boolean rateLimited) {
		super(datarouter, jobletNodes.joblet().getMaster().getClientNames(), Isolation.repeatableRead, false);
		this.reservationTimeout = reservationTimeout;
		this.reservedBy = reservedBy;
		this.jobletType = jobletType;
		this.maxRetries = maxRetries;
		this.rateLimited = rateLimited;
	}

	@Override
	public Joblet mergeResults(Joblet fromOnce,
			Collection<Joblet> fromEachClient) {
		return ResultMergeTool.first(fromOnce, fromEachClient);
	}


	@Override
	public Joblet runOncePerClient(Client client){
		boolean enforceRateLimit = rateLimited && jobletType.getRateLimited();
		Session session = getSession(client.getName());

		Joblet joblet = getOneJoblet(enforceRateLimit, session);
		while(joblet!=null && ! joblet.getStatus().isRunning()){
			joblet = getOneJoblet(enforceRateLimit,session);

			//TODO could subtract from queues on timed out joblets here
		}

		if(joblet==null){
			return null;
		}

		boolean alreadyRunning = DrNumberTool.nullSafe(joblet.getNumTimeouts())>0;
		// increment the numTickets on the JobletQueue
		if( ! alreadyRunning && enforceRateLimit && DrStringTool.notEmpty(joblet.getQueueId())){
			updateQueueTickets(session, joblet.getQueueId(), 1, getLogger());
		}
		return joblet;
	}

	@SuppressWarnings("unchecked")
	public Joblet getOneJoblet(boolean rateLimit, Session session){

		//reserve a joblet:  (type AND ((status AND rateLimit) OR timedOut))
		String typeClause = " type='" + jobletType.getVarName() + "' and ";

		String projectionClause = "{j.*}";
		if(rateLimit){
			projectionClause += ",{q.*}";
		}

		String statusAndRateLimitClause = "status='"+JobletStatus.created.getPersistentString()+"'";
		String jobletQueueTable = "";
		if(rateLimit){
			jobletQueueTable = ", JobletQueue q ";
			statusAndRateLimitClause = "("+statusAndRateLimitClause
					+" and j.queueId=q.id "
					+"and q.numTickets < q.maxTickets)";
		}

		String readyClause = statusAndRateLimitClause;
		String timedOutClause = "";
		if(getReservedBeforeMs()!=null){
			timedOutClause = "(status='"+JobletStatus.running.getPersistentString()+"'"
				+" and reservedAt < "+getReservedBeforeMs().toString()
				+" and restartable=true)";

			readyClause = " ("+statusAndRateLimitClause+" or "+timedOutClause+") ";
		}

		@SuppressWarnings("unused")
		String orderByClause = " order by "+Joblet.F.type+", "+Joblet.F.executionOrder
			+", "+Joblet.F.created+", "+Joblet.F.batchSequence+" ";

		String reserveSql = "select "+projectionClause
			+" from Joblet j"+ jobletQueueTable
			+" where "
			+ typeClause
			+ readyClause
//			+ orderByClause //already ordered by PK, explicit ordering will trigger a filesort (bad)
			+ " limit 1 for update";//apparently can't use a limit clause on a multi-table update statement, so do select first

		SQLQuery reserveQuery = session.createSQLQuery(reserveSql);
		reserveQuery.addEntity("j", Joblet.class);
		if(rateLimit){
			reserveQuery.addEntity("q", JobletQueue.class);//don't need the data in this one, but need to aquire the "for update" lock
		}
		Joblet joblet;
		if(rateLimit){//will return a list of rows, where each row is an Object[], and each array element is an entity
			Object[] row = (Object[])DrCollectionTool.getFirst(reserveQuery.list());
			if(row==null){ return null; }
			joblet = (Joblet)row[0];
		}else{
			joblet = (Joblet)DrCollectionTool.getFirst(reserveQuery.list());
			if(joblet==null){ return null; }
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

	public static int updateQueueTickets(Session session, String queueId, int delta, Logger logger){
		String deltaString = delta>=0 ? "+"+delta : ""+delta;//the negative will already have a minus sign
		String queueSql = "update JobletQueue set "+JobletQueue.COL_numTickets+"="
				+JobletQueue.COL_numTickets+" "+deltaString+" where id=:id";
		SQLQuery queueQuery = session.createSQLQuery(queueSql);
		queueQuery.setParameter("id", queueId);
		int numQueuesUpdated = queueQuery.executeUpdate();
		if(numQueuesUpdated < 1){
			logger.warn("JobletQueue:"+queueId+" numTickets increment unsuccessful");
		}
		return numQueuesUpdated;
	}

	protected Long getReservedBeforeMs(){
		if(reservationTimeout==null){ return null; }
		return System.currentTimeMillis() - reservationTimeout;
	}
}











