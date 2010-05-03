package com.hotpads.datarouter.client.imp.hibernate.node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.google.inject.internal.Preconditions;
import com.hotpads.datarouter.client.imp.hibernate.HibernateExecutor;
import com.hotpads.datarouter.client.imp.hibernate.HibernateTask;
import com.hotpads.datarouter.client.imp.hibernate.SessionTool;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalIndexedSortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.lookup.Lookup;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.StringTool;

public class HibernateNode<D extends Databean> 
extends HibernateReaderNode<D>
implements PhysicalIndexedSortedStorageNode<D>
{
	
	public HibernateNode(Class<D> databeanClass, DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HibernateNode(Class<D> databeanClass, DataRouter router, String clientName) {
		super(databeanClass, router, clientName);
	}
	
	@Override
	public Node<D> getMaster() {
		return this;
	}
	
	
	/************************************ MapStorageWriter methods ****************************/
	
	public static final PutMethod DEFAULT_PUT_METHOD = PutMethod.SELECT_FIRST_OR_LOOK_AT_PRIMARY_KEY;

	@Override
	public void delete(Key<D> key, Config config) {
		//this will not clear the databean from the hibernate session
		List<Key<D>> keys = new LinkedList<Key<D>>();
		keys.add(key);
		deleteMulti(keys, config);
	}
	
	
	protected void delete(final D databean, Config config){
		if(databean==null){ return; }
		final String entityName = this.getPackagedPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					session.delete(entityName, databean);
					return databean;
				}
			});
	}

	/*
	 * deleting 1000 rows by PK from a table with no indexes takes 200ms when executed as one statement
	 *  and 600ms when executed as 1000 batch deletes in a transaction
	 *  
	 * make sure MySQL's max packet size is big.  it may default to 1MB... set to like 64MB
	 * 
	 */
	@Override
	public void deleteMulti(Collection<? extends Key<D>> keys, Config config) {
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
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					SQLQuery query = session.createSQLQuery(finalQuery);
					return query.executeUpdate();
				}
			});
	}
	
	
	@Override
	public void deleteAll(Config config) {
		final String tableName = this.getPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					SQLQuery query = session.createSQLQuery("delete from "+tableName);
					return query.executeUpdate();
				}
			});
	}

	
	@Override
	public void put(final D databean, final Config config) {
		if(databean==null){ return; }
		final String entityName = this.getPackagedPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					SessionTool.putUsingMethod(session, entityName, databean, config, DEFAULT_PUT_METHOD);
					return databean;
				}
			});
	}

	
	@Override
	public void putMulti(Collection<D> databeans, final Config config) {
		final String entityName = this.getPackagedPhysicalName();
		final Collection<D> finalDatabeans = databeans;
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					for(D databean : CollectionTool.nullSafe(finalDatabeans)){
						if(databean==null){ continue; }
						SessionTool.putUsingMethod(session, entityName, databean, config, DEFAULT_PUT_METHOD);
					}
					return finalDatabeans;
				}
			});
	}

	@Override
	public void delete(final Lookup<D> lookup, final Config config) {
		if(lookup==null){ return; }
		final String tableName = this.getPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config);
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
	}


	@Override
	public void deleteRangeWithPrefix(final Key<D> prefix, final boolean wildcardLastField, final Config config) {
		if(prefix==null){ return; }
		final String tableName = this.getPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config);
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
					for(Field field : CollectionTool.nullSafe(prefix.getFields())){
						if(numFullFieldsFinished < numNonNullFields){
							if(numFullFieldsFinished > 0){
								sql.append(" and ");
							}
							boolean lastNonNullField = numFullFieldsFinished == numNonNullFields - 1;
							boolean stringField = field.getValue() instanceof String;
							
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
	}
	
	
	

	
}
