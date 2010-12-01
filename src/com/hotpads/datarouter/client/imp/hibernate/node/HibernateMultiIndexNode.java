package com.hotpads.datarouter.client.imp.hibernate.node;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.hibernate.HibernateExecutor;
import com.hotpads.datarouter.client.imp.hibernate.HibernateTask;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.physical.BasePhysicalNode;
import com.hotpads.datarouter.node.op.index.MultiIndexReader;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class HibernateMultiIndexNode<
				PK extends PrimaryKey<PK>,
				D extends Databean<PK>,
				IK extends PrimaryKey<IK>> 
extends BasePhysicalNode<PK,D>
implements //MapStorageReader<IK,IE>,
//		SortedStorageReader<IK,IE>,
		MultiIndexReader<PK,D,IK>{
	
	protected Logger logger = Logger.getLogger(getClass());
	
	/******************************* constructors ************************************/

	public HibernateMultiIndexNode(Class<D> databeanClass, 
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HibernateMultiIndexNode(Class<D> databeanClass,
			DataRouter router, String clientName) {
		super(databeanClass, router, clientName);
	}

	public HibernateMultiIndexNode(Class<? super D> baseDatabeanClass, Class<D> databeanClass, 
			DataRouter router, String clientName){
		super(baseDatabeanClass, databeanClass, router, clientName);
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public HibernateClientImp getClient(){
		return (HibernateClientImp)this.router.getClient(getClientName());
	}
	
	@Override
	public void clearThreadSpecificState(){
		//TODO maybe clear the hibernate session here through the client??
	}

	
	/************************************ MapStorageReader methods ****************************/
	
	public static final int DEFAULT_ITERATE_BATCH_SIZE = 1000;

	/************************************ MapReader methods ****************************/

	
	/************************************ IndexReader methods ****************************/

	public List<D> lookupMulti(final IK indexKey, final Config config){
		if(indexKey==null){ return new LinkedList<D>(); }
		TraceContext.startSpan(getName()+" lookupMulti");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					//TODO undefined behavior on trailing nulls
					if(fieldAware){
						String sql = SqlBuilder.getMulti(config, tableName, fields, ListTool.wrap(indexKey));
						List<D> result = JdbcTool.selectDatabeans(session, databeanClass, fields, sql);
						return result;
					}else{
						Criteria criteria = getCriteriaForConfig(config, session);
						for(Field<?> field : CollectionTool.nullSafe(indexKey.getFields())){
							criteria.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
						}
						List<D> result = criteria.list();
						Collections.sort(result);//todo, make sure the datastore scans in order so we don't need to sort here
						return result;
					}
				}
			});
		TraceContext.finishSpan();
		return (List<D>)result;
	};
	
	
	/********************************* hibernate helpers ***********************************************/
	
	protected void addPrimaryKeyOrderToCriteria(Criteria criteria){
		for(Field<?> field : this.primaryKeyFields){
			criteria.addOrder(Order.asc(field.getPrefixedName()));
		}
	}
	
	
	protected Criteria getCriteriaForConfig(Config config, Session session){
		final String entityName = this.getPackagedTableName();
		Criteria criteria = session.createCriteria(entityName);
		
		if(config == null){
			return criteria;
		}
		if(config.getLimit()!=null){
			criteria.setMaxResults(config.getLimit());
		}
		if(config.getOffset()!=null){
			criteria.setFirstResult(config.getOffset());
		}
		return criteria;
	}

	

}
