package com.hotpads.datarouter.client.imp.hibernate.op.write;

import java.util.Collection;

import org.hibernate.Session;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class HibernatePutOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseHibernateOp<Void>{
	
	public static final PutMethod DEFAULT_PUT_METHOD = PutMethod.SELECT_FIRST_OR_LOOK_AT_PRIMARY_KEY;
	
	private final HibernateNode<PK,D,F> node;
	private final Collection<D> databeans;
	private final Config config;
	
	public HibernatePutOp(HibernateNode<PK,D,F> node, Collection<D> databeans, Config config) {
		super(node.getDatarouterContext(), node.getClientNames(), getIsolation(config), 
				shouldAutoCommit(databeans, config));
		this.node = node;
		this.databeans = databeans;
		this.config = config;
	}
	
	@Override
	public Void runOnce(){
		Session session = getSession(node.getClientName());
		final String entityName = node.getPackagedTableName();
		for(D databean : DrCollectionTool.nullSafe(databeans)){
			hibernatePutUsingMethod(session, entityName, databean, config, DEFAULT_PUT_METHOD);
		}
		return null;
	}
	

	
	/******************** private **********************************************/
	
	private static Isolation getIsolation(Config config){
		if(config==null){ return Config.DEFAULT_ISOLATION; }
		return config.getIsolationOrUse(Config.DEFAULT_ISOLATION);
	}
	
	/*
	 * mirror of of above "putUsingMethod"
	 */
	private static boolean shouldAutoCommit(Collection<? extends Databean<?,?>> databeans, final Config config){
		if(DrCollectionTool.size(databeans) > 1){ return false; }
		PutMethod putMethod = DEFAULT_PUT_METHOD;
		if(config!=null && config.getPutMethod()!=null){
			putMethod = config.getPutMethod();
		}
		if(PutMethod.INSERT_OR_BUST == putMethod){
			return true;
		}else if(PutMethod.UPDATE_OR_BUST == putMethod){
			return true;
		}else if(PutMethod.INSERT_OR_UPDATE == putMethod){
			return false;
		}else if(PutMethod.UPDATE_OR_INSERT == putMethod){
			return false;
		}else if(PutMethod.MERGE == putMethod){
			return false;
		}else{
			return false;
		}
	}
	
	private void hibernatePutUsingMethod(Session session, String entityName, Databean<PK,D> databean, 
			final Config config, PutMethod defaultPutMethod){
		
		PutMethod putMethod = defaultPutMethod;
		if(config!=null && config.getPutMethod()!=null){
			putMethod = config.getPutMethod();
		}
		if(PutMethod.INSERT_OR_BUST == putMethod){
			session.save(entityName, databean);
		}else if(PutMethod.UPDATE_OR_BUST == putMethod){
			session.update(entityName, databean);
		}else if(PutMethod.INSERT_OR_UPDATE == putMethod){
			try{
				session.save(entityName, databean);
				session.flush();//seems like it tries to save 3 times before throwing an exception
			}catch(RuntimeException e){  
				session.evict(databean);  //must evict or it will ignore future actions for the databean?
				session.update(entityName, databean);
			}
		}else if(PutMethod.UPDATE_OR_INSERT == putMethod){
			try{
				session.update(entityName, databean);
				session.flush();
			}catch(RuntimeException e){
				session.evict(databean);  //must evict or it will ignore future actions for the databean?
				session.save(entityName, databean);
			}
		}else if(PutMethod.MERGE == putMethod){
			session.merge(entityName, databean);
		}else if(PutMethod.INSERT_ON_DUPLICATE_UPDATE == putMethod){
			session.saveOrUpdate(entityName, databean);
		}else{
			session.saveOrUpdate(entityName, databean);
		}
	}
}