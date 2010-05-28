package com.hotpads.datarouter.client.imp.hibernate.node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.hotpads.datarouter.client.imp.hibernate.HibernateExecutor;
import com.hotpads.datarouter.client.imp.hibernate.HibernateTask;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalIndexedSortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.StringTool;

public class HibernateNode<D extends Databean,PK extends PrimaryKey<D>> 
extends HibernateReaderNode<D,PK>
implements PhysicalIndexedSortedStorageNode<D,PK>
{
	
	public HibernateNode(Class<PK> primaryKeyClass, 
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(primaryKeyClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HibernateNode(Class<D> databeanClass, Class<PK> primaryKeyClass, 
			DataRouter router, String clientName) {
		super(databeanClass, primaryKeyClass, router, clientName);
	}
	
	public HibernateNode(Class<PK> primaryKeyClass, 
			DataRouter router, String clientName) {
		super(primaryKeyClass, router, clientName);
	}
	
	@Override
	public Node<D,PK> getMaster() {
		return this;
	}
	
	
	/************************************ MapStorageWriter methods ****************************/
	
	public static final PutMethod DEFAULT_PUT_METHOD = PutMethod.SELECT_FIRST_OR_LOOK_AT_PRIMARY_KEY;

	@Override
	public void delete(UniqueKey<D> key, Config config) {
		TraceContext.startSpan(getName()+" delete");
		//this will not clear the databean from the hibernate session
		List<UniqueKey<D>> keys = new LinkedList<UniqueKey<D>>();
		keys.add(key);
		deleteMulti(keys, config);
		TraceContext.finishSpan();
	}
	
	
//	private void delete(final D databean, Config config){
//		TraceContext.startSpan(getName()+" deleteDatabean");
//		if(databean==null){ return; }
//		final String entityName = this.getPackagedPhysicalName();
//		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, false);
//		executor.executeTask(
//			new HibernateTask() {
//				public Object run(Session session) {
//					session.delete(entityName, databean);
//					return databean;
//				}
//			});
//		TraceContext.finishSpan();
//	}

	/*
	 * deleting 1000 rows by PK from a table with no indexes takes 200ms when executed as one statement
	 *  and 600ms when executed as 1000 batch deletes in a transaction
	 *  
	 * make sure MySQL's max packet size is big.  it may default to 1MB... set to like 64MB
	 * 
	 */
	@Override
	public void deleteMulti(Collection<? extends UniqueKey<D>> keys, Config config) {
		TraceContext.startSpan(getName()+" deleteMulti");
		//build query
		if(CollectionTool.isEmpty(keys)){ return; }
		final String tableName = this.getPhysicalName();
		StringBuilder sb = new StringBuilder("delete from "+tableName+" where ");
		int numAppended = 0;
		for(Key<D> key : CollectionTool.nullSafe(keys)){
			if(key==null){ continue; }
			if(numAppended > 0){ sb.append(" or "); }
			//TODO SQL injection prevention
			List<String> partsOfThisKey = key.getSqlNameValuePairsEscaped();
			String keyString = "(" + StringTool.concatenate(partsOfThisKey, " and ") + ")";
			sb.append(keyString);
			++numAppended;
		}
		
		//execute
		final String finalQuery = sb.toString();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					SQLQuery query = session.createSQLQuery(finalQuery);
					return query.executeUpdate();
				}
			});
		TraceContext.finishSpan();
	}
	
	
	@Override
	public void deleteAll(Config config) {
		TraceContext.startSpan(getName()+" deleteAll");
		final String tableName = this.getPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					SQLQuery query = session.createSQLQuery("delete from "+tableName);
					return query.executeUpdate();
				}
			});
		TraceContext.finishSpan();
	}

	
	@Override
	public void put(final D databean, final Config config) {
		TraceContext.startSpan(getName()+" put");
		if(databean==null){ return; }
		final String entityName = this.getPackagedPhysicalName();
		boolean disableAutoCommit = this.shouldDisableAutoCommit(config, DEFAULT_PUT_METHOD);
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, disableAutoCommit);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					putUsingMethod(session, entityName, databean, config, DEFAULT_PUT_METHOD);
					return databean;
				}
			});
		TraceContext.finishSpan();
	}

	
	@Override
	public void putMulti(Collection<D> databeans, final Config config) {
		TraceContext.startSpan(getName()+" putMulti");
		final String entityName = this.getPackagedPhysicalName();
		final Collection<D> finalDatabeans = databeans;
		boolean disableAutoCommit = CollectionTool.size(databeans) > 1
				|| this.shouldDisableAutoCommit(config, DEFAULT_PUT_METHOD);
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, disableAutoCommit);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					for(D databean : CollectionTool.nullSafe(finalDatabeans)){
						if(databean==null){ continue; }
						putUsingMethod(session, entityName, databean, config, DEFAULT_PUT_METHOD);
					}
					return finalDatabeans;
				}
			});
		TraceContext.finishSpan();
	}

	@Override
	public void delete(final Lookup<D> lookup, final Config config) {
		TraceContext.startSpan(getName()+" delete");
		if(lookup==null){ return; }
		final String tableName = this.getPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, false);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					String prefix = "delete from "+tableName+" where ";
					List<String> fieldSqls = lookup.getSqlNameValuePairsEscaped();
					String limit = "";
					if(config != null && config.getLimit() != null){
						limit = " "+config.getLimit().toString();
					}
					//TODO SQL injection prevention
					String sql = prefix + StringTool.concatenate(fieldSqls, " and ") + limit;
					SQLQuery query = session.createSQLQuery(sql);
					int numDeleted = query.executeUpdate();
					return numDeleted;
				}
			});
		TraceContext.finishSpan();
	}


	@Override
	public void deleteRangeWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		TraceContext.startSpan(getName()+" deleteRangeWithPrefix");
		if(prefix==null){ return; }
		final String tableName = this.getPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					
					int numNonNullFields = 0;
					for(Comparable<?> value : CollectionTool.nullSafe(prefix.getFieldValues())){
						if(value != null){
							++numNonNullFields;
						}
					}
					
					StringBuilder sql = new StringBuilder();
					sql.append("delete from "+tableName+" where ");
					int numFullFieldsFinished = 0;
					for(Field<?> field : CollectionTool.nullSafe(prefix.getFields())){
						if(numFullFieldsFinished < numNonNullFields){
							if(numFullFieldsFinished > 0){
								sql.append(" and ");
							}
							boolean lastNonNullField = numFullFieldsFinished == numNonNullFields - 1;
							boolean stringField = !(field instanceof PrimitiveField<?>);
							
							boolean canDoPrefixMatchOnField = wildcardLastField && lastNonNullField && stringField;
							
							//TODO SQL injection prevention
							if(canDoPrefixMatchOnField){
								String value = field.getSqlEscaped();
								if(value.endsWith("'")){
									value = value.substring(0, value.length()-1) + "%'";
								}
								sql.append(field.getName()+" like "+value);
							}else{
								sql.append(field.getSqlNameValuePairEscaped());
							}
							++numFullFieldsFinished;
						}
					}
					if(config != null && config.getLimit() != null){
						sql.append(" limit "+config.getLimit());
					}
					SQLQuery query = session.createSQLQuery(sql.toString());
					int numDeleted = query.executeUpdate();
					return numDeleted;
				}
			});
		TraceContext.finishSpan();
	}
	
	
	/******************** private **********************************************/
	
	protected void putUsingMethod(Session session, String entityName, Databean databean, 
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
			}catch(Exception e){  
				session.evict(databean);  //must evict or it will ignore future actions for the databean?
				session.update(entityName, databean);
			}
		}else if(PutMethod.UPDATE_OR_INSERT == putMethod){
			try{
				session.update(entityName, databean);
				session.flush();
			}catch(Exception e){
				session.evict(databean);  //must evict or it will ignore future actions for the databean?
				session.save(entityName, databean);
			}
		}else if(PutMethod.MERGE == putMethod){
			session.merge(entityName, databean);
		}else{
			session.saveOrUpdate(entityName, databean);
		}
	}
	
	/*
	 * mirrof of above "putUsingMethod" above
	 */
	protected boolean shouldDisableAutoCommit(final Config config, PutMethod defaultPutMethod){
		
		PutMethod putMethod = defaultPutMethod;
		if(config!=null && config.getPutMethod()!=null){
			putMethod = config.getPutMethod();
		}
		if(PutMethod.INSERT_OR_BUST == putMethod){
			return false;
		}else if(PutMethod.UPDATE_OR_BUST == putMethod){
			return false;
		}else if(PutMethod.INSERT_OR_UPDATE == putMethod){
			return true;
		}else if(PutMethod.UPDATE_OR_INSERT == putMethod){
			return true;
		}else if(PutMethod.MERGE == putMethod){
			return true;
		}else{
			return true;
		}
	}
	
}
