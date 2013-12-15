package com.hotpads.datarouter.app.client.parallel.jdbc.base;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.imp.jdbc.ddl.op.BaseJdbcTxnOp;
import com.hotpads.datarouter.client.type.HibernateClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.TxnOp;
import com.hotpads.datarouter.op.aware.SessionAware;
import com.hotpads.datarouter.routing.DataRouterContext;

//TODO rename BaseHibernateTxnOp and move to hibernate module
public abstract class BaseParallelHibernateTxnApp<T>
extends BaseJdbcTxnOp<T> 
implements TxnOp<T>, SessionAware<T> {

	private DataRouterContext drContext;
	private List<String> clientNames;
	private Isolation isolation;
	private boolean autoCommit;
	
	public BaseParallelHibernateTxnApp(DataRouterContext drContext, List<String> clientNames) {
		this(drContext, clientNames, Isolation.DEFAULT, false);
	}

	public BaseParallelHibernateTxnApp(DataRouterContext drContext, List<String> clientNames, Isolation isolation,
			boolean autoCommit) {
		super(drContext, clientNames, isolation, autoCommit);
		this.drContext = drContext;
		this.clientNames = clientNames;
		this.isolation = isolation;
		this.autoCommit = autoCommit;
	}

	@Override
	public Session getSession(String clientName){
		Client client = drContext.getClientPool().getClient(clientName);
		if(client==null){ return null; }
		if(client instanceof HibernateClient){
			HibernateClient hibernateSessionClient = (HibernateClient)client;
			Session session = hibernateSessionClient.getExistingSession();
			return session;
		}
		return null;
	}
	
	
	/****************** abstract methods default to no-op ******************/
	
	@Override
	public T runOnce(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public T runOncePerClient(Client client){
		// TODO Auto-generated method stub
		return null;
	}
	
	public T mergeResults(T fromOnce, Collection<T> fromEachClient){
		return fromOnce;
	}
	
	
	/**************** get *******************************************/
	
//	@Override
//	public DataRouterContext getDataRouterContext(){
//		return drContext;
//	}

	@Override
	public List<String> getClientNames(){
		return clientNames;
	}

	@Override
	public Isolation getIsolation(){
		return isolation;
	}

	@Override
	public boolean isAutoCommit(){
		return autoCommit;
	}

	
}
