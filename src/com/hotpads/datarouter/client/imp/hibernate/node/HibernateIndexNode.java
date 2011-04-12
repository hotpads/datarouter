package com.hotpads.datarouter.client.imp.hibernate.node;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.hibernate.HibernateExecutor;
import com.hotpads.datarouter.client.imp.hibernate.HibernateTask;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.op.index.IndexReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.BatchTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class HibernateIndexNode<
				PK extends PrimaryKey<PK>,
				D extends Databean<PK,D>,
				IK extends PrimaryKey<IK>,
				F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements IndexReader<PK,D,IK>{
	
	protected Logger logger = Logger.getLogger(getClass());
	
	/******************************* constructors ************************************/

	public HibernateIndexNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, fielderClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HibernateIndexNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName) {
		super(databeanClass, fielderClass, router, clientName);
	}

	public HibernateIndexNode(Class<? super D> baseDatabeanClass, Class<D> databeanClass, 
			Class<F> fielderClass, DataRouter router, String clientName){
		super(baseDatabeanClass, databeanClass, fielderClass, router, clientName);
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

	@Override
	@SuppressWarnings("unchecked")
	public D lookupUnique(final IK uniqueKey, final Config config){
		//basically copied from "get" for HibernateNode
		if(uniqueKey==null){ return null; }
		TraceContext.startSpan(getName()+" lookupUnique");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					if(fieldInfo.getFieldAware()){
						String sql = SqlBuilder.getMulti(config, tableName, fieldInfo.getFields(), ListTool.wrap(uniqueKey));
						List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
						if(CollectionTool.size(result) > 1){ throw new DataAccessException("found >1 databeans with PK="+uniqueKey); }
						return CollectionTool.getFirst(result);
					}else{
						Criteria criteria = getCriteriaForConfig(config, session);
						List<Field<?>> fields = uniqueKey.getFields();
						for(Field<?> field : fields){
							criteria.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
						}
						D result = (D)criteria.uniqueResult();
						return result;
					}
				}
			});
		TraceContext.finishSpan();
		return (D)result;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<D> lookupMultiUnique( final Collection<IK> uniqueKeys, final Config config){
		//basically copied from "getMulti" for HibernateNode
		TraceContext.startSpan(getName()+" lookupMultiUnique");	
		if(CollectionTool.isEmpty(uniqueKeys)){ return new LinkedList<D>(); }
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					int batchSize = DEFAULT_ITERATE_BATCH_SIZE;
					if(config!=null && config.getIterateBatchSize()!=null){
						batchSize = config.getIterateBatchSize();
					}
					List<IK> sortedKeys = ListTool.createArrayList(uniqueKeys);
					Collections.sort(sortedKeys);
					int numBatches = BatchTool.getNumBatches(sortedKeys.size(), batchSize);
					List<D> all = ListTool.createArrayList(uniqueKeys.size());
					for(int batchNum=0; batchNum < numBatches; ++batchNum){
						List<IK> keyBatch = BatchTool.getBatch(sortedKeys, batchSize, batchNum);
						List<D> batch;
						if(fieldInfo.getFieldAware()){
							String sql = SqlBuilder.getMulti(config, tableName, fieldInfo.getFields(), uniqueKeys);
							List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
							//maybe verify if the keys were in fact unique?
							return result;
						}else{
							Criteria criteria = getCriteriaForConfig(config, session);
							Disjunction orSeparatedIds = Restrictions.disjunction();
							for(IK key : CollectionTool.nullSafe(keyBatch)){
								Conjunction possiblyCompoundId = Restrictions.conjunction();
								List<Field<?>> fields = key.getFields();
								for(Field<?> field : fields){
									possiblyCompoundId.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
								}
								orSeparatedIds.add(possiblyCompoundId);
							}
							criteria.add(orSeparatedIds);
							batch = criteria.list();
						}
						ListTool.nullSafeArrayAddAll(all, batch);
					}
					return all;
				}
			});
		TraceContext.finishSpan();
		return (List<D>)result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<D> lookupMulti(final IK indexKey, final boolean wildcardLastField, final Config config){
		if(indexKey==null){ return new LinkedList<D>(); }
		TraceContext.startSpan(getName()+" lookupMulti");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					//TODO undefined behavior on trailing nulls
					if(fieldInfo.getFieldAware()){
						String sql = SqlBuilder.getMulti(config, tableName, fieldInfo.getFields(), ListTool.wrap(indexKey));
						List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
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
	
	@Override
	public List<D> lookupMultiMulti(final Collection<IK> indexKeys, final boolean wildcardLastField, final Config config){
		if(CollectionTool.isEmpty(indexKeys)){ return new LinkedList<D>(); }
		TraceContext.startSpan(getName()+" lookupMultiMulti");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					//TODO undefined behavior on trailing nulls
					if(fieldInfo.getFieldAware()){
						String sql = SqlBuilder.getMulti(config, tableName, fieldInfo.getFields(), indexKeys);
						List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
						return result;
					}else{
						Criteria criteria = getCriteriaForConfig(config, session);
						Disjunction orSeparatedIndexEntries = Restrictions.disjunction();
						for(IK indexKey : IterableTool.nullSafe(indexKeys)){
							Conjunction andSeparatedFields = Restrictions.conjunction();
							for(Field<?> field : CollectionTool.nullSafe(indexKey.getFields())){
								andSeparatedFields.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
							}
							orSeparatedIndexEntries.add(andSeparatedFields);
						}
						criteria.add(orSeparatedIndexEntries);
						List<D> result = criteria.list();
						Collections.sort(result);//todo, make sure the datastore scans in order so we don't need to sort here
						return result;
					}
				}
			});
		TraceContext.finishSpan();
		return (List<D>)result;
	}
	
	/********************************* hibernate helpers ***********************************************/
	
	protected void addPrimaryKeyOrderToCriteria(Criteria criteria){
		for(Field<?> field : fieldInfo.getPrimaryKeyFields()){
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
