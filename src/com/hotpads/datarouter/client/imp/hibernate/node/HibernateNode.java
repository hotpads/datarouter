package com.hotpads.datarouter.client.imp.hibernate.node;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.hotpads.datarouter.client.imp.hibernate.HibernateExecutor;
import com.hotpads.datarouter.client.imp.hibernate.HibernateTask;
import com.hotpads.datarouter.client.imp.hibernate.JdbcTool;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalIndexedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.StringTool;

public class HibernateNode<D extends Databean> 
extends HibernateReaderNode<D>
implements PhysicalIndexedStorageNode<D>
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

	@Override
	public void delete(Key<D> key, Config config) {
		//this will not clear the databean from the hibernate session
		List<Key<D>> keys = new LinkedList<Key<D>>();
		keys.add(key);
		deleteMulti(keys, config);
	}
	
	
	protected void delete(final D databean, Config config){
		final String entityName = this.getPackagedPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, null);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					session.delete(entityName, databean);
					return databean;
				}
			});
	}

	/*
	 * deleting 1000 rows from a table with no indexes takes 200ms when executed as one statement
	 *  and 600ms when executed as 1000 batch deletes in a transaction
	 * 
	 */
	
//	@Override
//	public void deleteMulti(Collection<? extends Key<D>> keys, Config config) {
//		//build query
//		if(CollectionTool.isEmpty(keys)){ return; }
//		final String tableName = this.getPhysicalName();
//		String deletePrefix = "delete from "+tableName+" where ";
//		String[] sqlStatements = new String[keys.size()];
//		int nextIndex = 0;
//		for(Key<D> key : CollectionTool.nullSafe(keys)){
//			List<String> partsOfThisKey = key.getSqlNameValuePairsEscaped();
//			String whereFields = StringTool.concatenate(partsOfThisKey, " and ");
//			String sql = deletePrefix + whereFields;
//			sqlStatements[nextIndex] = sql;
//			++nextIndex;
//		}
//		
//		//execute
//		final String[] finalSqlStatements = sqlStatements;
//		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
//		executor.executeTask(
//			new HibernateTask() {
//				public Object run(Session session) {
//					Connection conn = null;
//					try{
//						conn = session.connection();
//						conn.setAutoCommit(false);
//						int[] rowsModified = JdbcTool.bulkUpdate(conn, finalSqlStatements);
//						conn.commit();
//						return rowsModified;
//					}catch(SQLException sqle){
//						if(conn != null){ 
//							try {
//								conn.rollback();
//							} catch (SQLException rbe) {
//								throw new HibernateException(rbe);
//							}
//						}
//						throw new HibernateException(sqle);
//					}finally{
//						try {
//							conn.close();
//						} catch (SQLException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			});
//	}
	
	@Override
	public void deleteMulti(Collection<? extends Key<D>> keys, Config config) {
		//build query
		if(CollectionTool.isEmpty(keys)){ return; }
		final String tableName = this.getPhysicalName();
		StringBuilder sb = new StringBuilder("delete from "+tableName+" where ");
		int numAppended = 0;
		for(Key<D> key : CollectionTool.nullSafe(keys)){
			if(numAppended > 0){ sb.append(" or "); }
			List<String> partsOfThisKey = key.getSqlNameValuePairsEscaped();
			String keyString = "(" + StringTool.concatenate(partsOfThisKey, " and ") + ")";
			sb.append(keyString);
			++numAppended;
		}
		
		//execute
		final String finalQuery = sb.toString();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
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
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					SQLQuery query = session.createSQLQuery("delete from "+tableName);
					return query.executeUpdate();
				}
			});
	}

	
	@Override
	public void put(final D databean, Config config) {
		final String entityName = this.getPackagedPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					session.saveOrUpdate(entityName, databean);
					return databean;
				}
			});
	}

	
	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		for(D databean : CollectionTool.nullSafe(databeans)){
			put(databean, config);
		}
	}

	
}
