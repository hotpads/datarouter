package com.hotpads.datarouter.client.imp.hibernate.node;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.hotpads.datarouter.app.client.parallel.jdbc.base.BaseParallelHibernateTxnApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.trace.TraceContext;

public class HibernatePutOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseParallelHibernateTxnApp<Void>{
	private static Logger logger = Logger.getLogger(HibernatePutOp.class);
	
	private HibernateNode<PK,D,F> node;
	private D databean;
	private Config config;
	
	public HibernatePutOp(HibernateNode<PK,D,F> node, D databean, Config config) {
		super(node.getDataRouterContext(), node.getClientNames());
		this.node = node;
		this.databean = databean;
		this.config = config;
	}
	
	@Override
	public Void runOncePerClient(Client client){
		if(databean==null){ return null; }
		TraceContext.startSpan(node.getName()+" put");
		Session session = getSession(client.getName());
		final String entityName = node.getPackagedTableName();
		boolean disableAutoCommit = node.shouldDisableAutoCommit(config, HibernateNode.DEFAULT_PUT_METHOD);
		//TODO actually disable auto-commit.  was previously passed to HibernateExecutor
		if(node.getFieldInfo().getFieldAware()){
			node.jdbcPutUsingMethod(session.connection(), entityName, databean, config, HibernateNode.DEFAULT_PUT_METHOD);
		}else{
			node.hibernatePutUsingMethod(session, entityName, databean, config, HibernateNode.DEFAULT_PUT_METHOD);
		}
		TraceContext.finishSpan();
		return null;
	}
}
