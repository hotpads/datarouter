package com.hotpads.datarouter.client.imp.hibernate.node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import com.hotpads.datarouter.client.imp.hibernate.HibernateExecutor;
import com.hotpads.datarouter.client.imp.hibernate.HibernateTask;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.java.ReflectionTool;

public class HibernateNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends HibernateReaderNode<PK,D,F>
implements PhysicalIndexedSortedMapStorageNode<PK,D>
{
	
	public HibernateNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, fielderClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HibernateNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName) {
		super(databeanClass, fielderClass, router, clientName);
	}
	
	public HibernateNode(Class<? super D> baseDatabeanClass, Class<D> databeanClass, 
			Class<F> fielderClass, DataRouter router, String clientName){
		super(baseDatabeanClass, databeanClass, fielderClass, router, clientName);
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return this;
	}
	
	
	/************************************ MapStorageWriter methods ****************************/

	@Override
	public void put(final D databean, final Config config) {
		HibernatePutOp<PK,D,F> op = new HibernatePutOp<PK,D,F>(this, ListTool.wrap(databean), config);
		op.call();
	}

	
	@Override
	public void putMulti(Collection<D> databeans, final Config config) {
		HibernatePutOp<PK,D,F> op = new HibernatePutOp<PK,D,F>(this, databeans, config);
		op.call();
	}
	
	@Override
	public void deleteAll(final Config config) {
		TraceContext.startSpan(getName()+" deleteAll");
		final String tableName = this.getTableName();
		HibernateExecutor executor = HibernateExecutor.create("deleteAll", getClient(), this, config, false);
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
		HibernateExecutor executor = HibernateExecutor.create("deleteMulti", getClient(), this, config, false);
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
		HibernateExecutor executor = HibernateExecutor.create("deleteMultiUnique", getClient(), this, config, false);
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
		HibernateExecutor executor = HibernateExecutor.create("deleteRangeWithPrefix", getClient(), this, config, false);
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
		HibernateExecutor executor = HibernateExecutor.create("deleteLookup", getClient(), this, config, false);
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

	
	
}
