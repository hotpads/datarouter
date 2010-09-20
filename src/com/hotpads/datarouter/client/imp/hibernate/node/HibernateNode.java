package com.hotpads.datarouter.client.imp.hibernate.node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;

import org.hibernate.Session;

import com.hotpads.datarouter.client.imp.hibernate.HibernateExecutor;
import com.hotpads.datarouter.client.imp.hibernate.HibernateTask;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalIndexedSortedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class HibernateNode<PK extends PrimaryKey<PK>,D extends Databean<PK>> 
extends HibernateReaderNode<PK,D>
implements PhysicalIndexedSortedStorageNode<PK,D>
{
	
	public HibernateNode(Class<D> databeanClass, 
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HibernateNode(Class<D> databeanClass, 
			DataRouter router, String clientName) {
		super(databeanClass, router, clientName);
	}
	
	public HibernateNode(Class<? super D> baseDatabeanClass, Class<D> databeanClass, 
			DataRouter router, String clientName){
		super(baseDatabeanClass, databeanClass, router, clientName);
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return this;
	}
	
	
	/************************************ MapStorageWriter methods ****************************/
	
	public static final PutMethod DEFAULT_PUT_METHOD = PutMethod.SELECT_FIRST_OR_LOOK_AT_PRIMARY_KEY;
	
	@Override
	public void put(final D databean, final Config config) {
		TraceContext.startSpan(getName()+" put");
		if(databean==null){ return; }
		final String entityName = this.getPackagedTableName();
		boolean disableAutoCommit = this.shouldDisableAutoCommit(config, DEFAULT_PUT_METHOD);
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, disableAutoCommit);
		executor.executeTask(
			new HibernateTask() {
				@SuppressWarnings("deprecation")
				public Object run(Session session) {
					if(fieldAware){
						jdbcPutUsingMethod(session.connection(), entityName, databean, config, DEFAULT_PUT_METHOD);
					}else{
						hibernatePutUsingMethod(session, entityName, databean, config, DEFAULT_PUT_METHOD);
					}
					return databean;
				}
			});
		TraceContext.finishSpan();
	}

	
	@Override
	public void putMulti(Collection<D> databeans, final Config config) {
		TraceContext.startSpan(getName()+" putMulti");
		final String entityName = this.getPackagedTableName();
		final Collection<D> finalDatabeans = databeans;
		boolean disableAutoCommit = CollectionTool.size(databeans) > 1
				|| this.shouldDisableAutoCommit(config, DEFAULT_PUT_METHOD);
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, disableAutoCommit);
		executor.executeTask(
			new HibernateTask() {
				@SuppressWarnings("deprecation")
				public Object run(Session session) {
					for(D databean : CollectionTool.nullSafe(finalDatabeans)){
						if(databean==null){ continue; }
						if(fieldAware){
							jdbcPutUsingMethod(session.connection(), entityName, databean, config, DEFAULT_PUT_METHOD);
						}else{
							hibernatePutUsingMethod(session, entityName, databean, config, DEFAULT_PUT_METHOD);
						}
					}
					return finalDatabeans;
				}
			});
		TraceContext.finishSpan();
	}
	
	@Override
	public void deleteAll(final Config config) {
		TraceContext.startSpan(getName()+" deleteAll");
		final String tableName = this.getTableName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					String sql = SqlBuilder.deleteAll(config, tableName);
					int numModified = JdbcTool.update(session, sql.toString());
					return numModified;
				}
			});
		TraceContext.finishSpan();
	}

	@Override
	public void delete(PK key, Config config) {
		TraceContext.startSpan(getName()+" delete");
		//this will not clear the databean from the hibernate session
		deleteMulti(ListTool.wrap(key), config);
		TraceContext.finishSpan();
	}

	/*
	 * deleting 1000 rows by PK from a table with no indexes takes 200ms when executed as one statement
	 *  and 600ms when executed as 1000 batch deletes in a transaction
	 *  
	 * make sure MySQL's max packet size is big.  it may default to 1MB... set to like 64MB
	 * 
	 */
	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config){
		TraceContext.startSpan(getName()+" deleteMulti");
		//build query
		if(CollectionTool.isEmpty(keys)){ return; }
		final String tableName = this.getTableName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					String sql = SqlBuilder.deleteMulti(config, tableName, keys);
					int numModified = JdbcTool.update(session, sql.toString());
					return numModified;
				}
			}
		);
		TraceContext.finishSpan();
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config) {
		TraceContext.startSpan(getName()+" deleteUnique");
		//this will not clear the databean from the hibernate session
		deleteMultiUnique(ListTool.wrap(uniqueKey), config);
		TraceContext.finishSpan();
	}
	
	@Override
	public void deleteMultiUnique(final Collection<? extends UniqueKey<PK>> uniqueKeys, final Config config){
		TraceContext.startSpan(getName()+" deleteMultiUnique");
		//build query
		if(CollectionTool.isEmpty(uniqueKeys)){ return; }
		final String tableName = this.getTableName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					String sql = SqlBuilder.deleteMulti(config, tableName, uniqueKeys);
					int numModified = JdbcTool.update(session, sql.toString());
					return numModified;
				}
			}
		);
		TraceContext.finishSpan();
	}

	@Override
	public void deleteRangeWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		TraceContext.startSpan(getName()+" deleteRangeWithPrefix");
		if(prefix==null){ return; }
		final String tableName = this.getTableName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					String sql = SqlBuilder.deleteWithPrefixes(config, tableName, 
							ListTool.wrap(prefix), wildcardLastField);
					int numModified = JdbcTool.update(session, sql.toString());
					return numModified;
				}
			});
		TraceContext.finishSpan();
	}
	
	@Override
	public void delete(final Lookup<PK> lookup, final Config config) {
		TraceContext.startSpan(getName()+" deleteLookup");
		if(lookup==null){ return; }
		final String tableName = this.getTableName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, false);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					String sql = SqlBuilder.deleteMulti(config, tableName, ListTool.wrap(lookup));
					int numModified = JdbcTool.update(session, sql.toString());
					return numModified;
				}
			});
		TraceContext.finishSpan();
	}

	
	
	/******************** private **********************************************/
	
	protected void hibernatePutUsingMethod(Session session, String entityName, Databean<PK> databean, 
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
	
	protected void jdbcPutUsingMethod(Connection connection, String entityName, Databean<PK> databean,
			final Config config, PutMethod defaultPutMethod){
		PutMethod putMethod = defaultPutMethod;
		if(config!=null && config.getPutMethod()!=null){
			putMethod = config.getPutMethod();
		}
		if(PutMethod.INSERT_OR_BUST == putMethod){
			jdbcInsert(connection, entityName, databean);
		}else if(PutMethod.UPDATE_OR_BUST == putMethod){
			jdbcUpdate(connection, entityName, databean);
		}else if(PutMethod.INSERT_OR_UPDATE == putMethod){
			try{
				jdbcInsert(connection, entityName, databean);
			}catch(Exception e){  
				jdbcUpdate(connection, entityName, databean);
			}
		}else if(PutMethod.UPDATE_OR_INSERT == putMethod){
			try{
				jdbcUpdate(connection, entityName, databean);
			}catch(Exception e){
				jdbcInsert(connection, entityName, databean);
			}
		}else if(PutMethod.MERGE == putMethod){
			//not really a jdbc concept, but usually an update (?)
			try{
				jdbcUpdate(connection, entityName, databean);
			}catch(Exception e){
				jdbcInsert(connection, entityName, databean);
			}
		}else{
			//TODO handle server-generated primary keys
			if(this.exists(databean.getKey(), null)){//select before update like hibernate's saveOrUpdate
				jdbcUpdate(connection, entityName, databean);
			}else{
				jdbcInsert(connection, entityName, databean);
			}
		}
	}
	
	protected void jdbcInsert(Connection connection, String entityName, Databean<PK> databean){
//		logger.warn("JDBC Insert");
		StringBuilder sb = new StringBuilder();
		sb.append("insert into "+tableName+" (");
		//TODO handle server-generated primary keys
		FieldTool.appendCsvNames(sb, fields);
		sb.append(") values (");
		JdbcTool.appendCsvQuestionMarks(sb, CollectionTool.size(fields));
		sb.append(")");
		try{
			PreparedStatement ps = connection.prepareStatement(sb.toString());
			int parameterIndex = 1;//one based
			for(Field<?> field : databean.getFields()){
				field.setPreparedStatementValue(ps, parameterIndex);
				++parameterIndex;
			}
			ps.execute();
		}catch(Exception e){
			throw new DataAccessException(e);
		}
	}
	
	protected void jdbcUpdate(Connection connection, String entityName, Databean<PK> databean){
//		logger.warn("JDBC update");
		StringBuilder sb = new StringBuilder();
		sb.append("update "+tableName+" set ");
		FieldTool.appendSqlUpdateClauses(sb, nonKeyFields);
		sb.append(" where ");
		sb.append(FieldTool.getSqlNameValuePairsEscapedConjunction(databean.getKeyFields()));
		int numUpdated;
		try{
			PreparedStatement ps = connection.prepareStatement(sb.toString());
			int parameterIndex = 1;
			for(Field<?> field : databean.getNonKeyFields()){
				field.setPreparedStatementValue(ps, parameterIndex);
				++parameterIndex;
			}
			numUpdated = ps.executeUpdate();
		}catch(Exception e){
			throw new DataAccessException(e);
		}
		if(numUpdated!=1){
			throw new DataAccessException("row "+databean.getKey().toString()+" not found so could not be updated");
		}
	}
	
	/*
	 * mirror of of above "putUsingMethod" above
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
