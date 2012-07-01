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
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.hibernate.HibernateExecutor;
import com.hotpads.datarouter.client.imp.hibernate.HibernateTask;
import com.hotpads.datarouter.client.imp.hibernate.util.CriteriaTool;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.hibernate.util.SqlBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.scanner.Scanner;
import com.hotpads.datarouter.node.scanner.primarykey.PrimaryKeyScanner;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.BatchTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.exception.NotImplementedException;
import com.hotpads.util.core.iterable.PeekableIterable;

public class HibernateReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements MapStorageReader<PK,D>,
		SortedStorageReader<PK,D>,
		IndexedStorageReader<PK,D>{
	
	protected Logger logger = Logger.getLogger(getClass());
	
	/******************************* constructors ************************************/

	public HibernateReaderNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, fielderClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HibernateReaderNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName) {
		super(databeanClass, fielderClass, router, clientName);
	}

	public HibernateReaderNode(Class<? super D> baseDatabeanClass, Class<D> databeanClass, 
			Class<F> fielderClass,
			DataRouter router, String clientName){
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
	
	@Override
	public boolean exists(PK key, Config config) {
		return this.get(key, config) != null;
	}


	@SuppressWarnings("unchecked")
	@Override
	public D get(final PK key, final Config config){
		if(key==null){ return null; }
		TraceContext.startSpan(getName()+" get");
		final String tableName = this.getTableName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					if(fieldInfo.getFieldAware()){
						String sql = SqlBuilder.getMulti(config, tableName, fieldInfo.getFields(), ListTool.wrap(key));
						List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
						if(CollectionTool.size(result) > 1){ throw new DataAccessException("found >1 databeans with PK="+key); }
						return CollectionTool.getFirst(result);
					}else{
						Criteria criteria = getCriteriaForConfig(config, session);
						List<Field<?>> fields = FieldTool.prependPrefixes(fieldInfo.getKeyFieldName(), key.getFields());
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
	public List<D> getAll(final Config config) {
		TraceContext.startSpan(getName()+" getAll");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					if(fieldInfo.getFieldAware()){
						DRCounters.incPrefixClientNode("jdbc get", clientName, name);
						String sql = SqlBuilder.getAll(config, tableName, fieldInfo.getFields());
						List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
						return result;
					}else{
						DRCounters.incPrefixClientNode("hibernate get", clientName, name);
						Criteria criteria = getCriteriaForConfig(config, session);
						Object listOfDatabeans = criteria.list();
						return listOfDatabeans;//todo, make sure the datastore scans in order so we don't need to sort here
					}
				}
			});
		TraceContext.finishSpan();
		return (List<D>)result;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public List<D> getMulti(final Collection<PK> keys, final Config config) {	
		TraceContext.startSpan(getName()+" getMulti");	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<D>(); }
//		final Class<? extends Databean> persistentClass = CollectionTool.getFirst(keys).getDatabeanClass();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		List<D> result = (List<D>)executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					int batchSize = DEFAULT_ITERATE_BATCH_SIZE;
					if(config!=null && config.getIterateBatchSize()!=null){
						batchSize = config.getIterateBatchSize();
					}
					List<? extends Key<PK>> sortedKeys = ListTool.createArrayList(keys);
					Collections.sort(sortedKeys);
					int numBatches = BatchTool.getNumBatches(sortedKeys.size(), batchSize);
					List<D> all = ListTool.createArrayList(keys.size());
					for(int batchNum=0; batchNum < numBatches; ++batchNum){
						List<? extends Key<PK>> keyBatch = BatchTool.getBatch(sortedKeys, batchSize, batchNum);
						List<D> batch;
						if(fieldInfo.getFieldAware()){
							String sql = SqlBuilder.getMulti(config, tableName, fieldInfo.getFields(), keyBatch);
							batch = JdbcTool.selectDatabeans(session, fieldInfo, sql);
							DRCounters.incPrefixClientNode("jdbc getMulti", clientName, name);
							DRCounters.incPrefixClientNode("jdbc getMulti rows", clientName, name, 
									CollectionTool.size(keys));
						}else{
							Criteria criteria = getCriteriaForConfig(config, session);
							Disjunction orSeparatedIds = Restrictions.disjunction();
							for(Key<PK> key : CollectionTool.nullSafe(keyBatch)){
								Conjunction possiblyCompoundId = Restrictions.conjunction();
								List<Field<?>> fields = FieldTool.prependPrefixes(fieldInfo.getKeyFieldName(), key.getFields());
								for(Field<?> field : fields){
									possiblyCompoundId.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
								}
								orSeparatedIds.add(possiblyCompoundId);
							}
							criteria.add(orSeparatedIds);
							batch = criteria.list();
							DRCounters.incPrefixClientNode("hibernate getMulti", clientName, name);
							DRCounters.incPrefixClientNode("hibernate getMulti rows", clientName, name, 
									CollectionTool.size(keys));
						}
						Collections.sort(batch);//can sort here because batches were already sorted
						ListTool.nullSafeArrayAddAll(all, batch);
					}
					return all;
				}
			});
		TraceContext.appendToSpanInfo("[got "+CollectionTool.size(result)+"/"+CollectionTool.size(keys)+"]");
		TraceContext.finishSpan();
		return result;
	}
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config config) {
		if(fieldInfo.getFieldAware()){ throw new NotImplementedException(); }
		TraceContext.startSpan(getName()+" getKeys");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(			
			new HibernateTask() {
				public Object run(Session session) {
					int batchSize = DEFAULT_ITERATE_BATCH_SIZE;
					if(config!=null && config.getIterateBatchSize()!=null){
						batchSize = config.getIterateBatchSize();
					}
					List<? extends Key<PK>> sortedKeys = ListTool.createArrayList(keys);
					Collections.sort(sortedKeys);
					int numBatches = BatchTool.getNumBatches(sortedKeys.size(), batchSize);
					List<PK> all = ListTool.createArrayList(keys.size());
					for(int batchNum=0; batchNum < numBatches; ++batchNum){
						List<? extends Key<PK>> keyBatch = BatchTool.getBatch(sortedKeys, batchSize, batchNum);
						Criteria criteria = getCriteriaForConfig(config, session);
						//projection list
						ProjectionList projectionList = Projections.projectionList();
						int numFields = 0;
						for(Field<?> field : fieldInfo.getPrefixedPrimaryKeyFields()){
							projectionList.add(Projections.property(field.getPrefixedName()));
							++numFields;
						}
						criteria.setProjection(projectionList);
						//where clause
						Disjunction orSeparatedIds = Restrictions.disjunction();
						for(Key<PK> key : CollectionTool.nullSafe(keyBatch)){
							Conjunction possiblyCompoundId = Restrictions.conjunction();
							List<Field<?>> fields = FieldTool.prependPrefixes(fieldInfo.getKeyFieldName(), key.getFields());
							for(Field<?> field : fields){
								possiblyCompoundId.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
							}
							orSeparatedIds.add(possiblyCompoundId);
						}
						criteria.add(orSeparatedIds);
						List<Object[]> rows = criteria.list();
						for(Object[] row : IterableTool.nullSafe(rows)){
							all.add(FieldSetTool.fieldSetFromHibernateResultUsingReflection(
									fieldInfo.getPrimaryKeyClass(), fieldInfo.getPrimaryKeyFields(), row));
						}
					}
					return all;
				}
			});
		TraceContext.finishSpan();
		return (List<PK>)result;
	}

	
	
	/************************************ IndexedStorageReader methods ****************************/
	
	@Override
	public Long count(final Lookup<PK> lookup, final Config config) {
		if(lookup==null){ return 0l; }
		TraceContext.startSpan(getName()+" count");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					if(fieldInfo.getFieldAware()){
						String sql = SqlBuilder.getCount(config, tableName, fieldInfo.getFields(), ListTool.wrap(lookup));
						return JdbcTool.count(session, sql);
					}else{
						Criteria criteria = getCriteriaForConfig(config, session);
						criteria.setProjection(Projections.rowCount());

						for(Field<?> field : CollectionTool.nullSafe(lookup.getFields())){
							criteria.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
						}
						
						Number n = (Number)criteria.uniqueResult();						
						return n.longValue();
					}
				}
			});
		TraceContext.finishSpan();
		return (Long)result;
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public D lookupUnique(final UniqueKey<PK> uniqueKey, final Config config){
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
	public List<D> lookupMultiUnique( final Collection<? extends UniqueKey<PK>> uniqueKeys, final Config config){
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
					List<? extends UniqueKey<PK>> sortedKeys = ListTool.createArrayList(uniqueKeys);
					Collections.sort(sortedKeys);
					int numBatches = BatchTool.getNumBatches(sortedKeys.size(), batchSize);
					List<D> all = ListTool.createArrayList(uniqueKeys.size());
					for(int batchNum=0; batchNum < numBatches; ++batchNum){
						List<? extends Key<PK>> keyBatch = BatchTool.getBatch(sortedKeys, batchSize, batchNum);
						List<D> batch;
						if(fieldInfo.getFieldAware()){
							String sql = SqlBuilder.getMulti(config, tableName, fieldInfo.getFields(), uniqueKeys);
							List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
							//maybe verify if the keys were in fact unique?
							return result;
						}else{
							Criteria criteria = getCriteriaForConfig(config, session);
							Disjunction orSeparatedIds = Restrictions.disjunction();
							for(Key<PK> key : CollectionTool.nullSafe(keyBatch)){
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
	
	@Override
	@SuppressWarnings("unchecked")
	//TODO pay attention to wildcardLastField
	public List<D> lookup(final Lookup<PK> lookup, final boolean wildcardLastField, final Config config) {
		if(lookup==null){ return new LinkedList<D>(); }
		TraceContext.startSpan(getName()+" lookup");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					//TODO undefined behavior on trailing nulls
					if(fieldInfo.getFieldAware()){
//						String sql = SqlBuilder.getMulti(config, tableName, fieldInfo.getFields(), ListTool.wrap(lookup));
						String sql = SqlBuilder.getWithPrefixes(config, tableName, fieldInfo.getFields(), 
								ListTool.wrap(lookup), wildcardLastField);
						List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
						return result;
					}else{
						Criteria criteria = getCriteriaForConfig(config, session);
						Conjunction prefixConjunction = getPrefixConjunction(false, lookup, wildcardLastField);
						if(prefixConjunction==null){
							throw new IllegalArgumentException("Lookup with all null fields would return entire table.  "
									+"Please use getAll() instead.");
						}
						criteria.add(prefixConjunction);
						List<D> result = criteria.list();
						Collections.sort(result);//todo, make sure the datastore scans in order so we don't need to sort here
						return result;
					}
				}
			});
		TraceContext.finishSpan();
		return (List<D>)result;
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public List<D> lookup(final Collection<? extends Lookup<PK>> lookups, final Config config) {
		TraceContext.startSpan(getName()+" multiLookup");
		if(CollectionTool.isEmpty(lookups)){ return new LinkedList<D>(); }
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					//TODO undefined behavior on trailing nulls
					if(fieldInfo.getFieldAware()){
						String sql = SqlBuilder.getMulti(config, tableName, fieldInfo.getFields(), lookups);
						List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
						return result;
					}else{
						Criteria criteria = getCriteriaForConfig(config, session);
						Disjunction or = Restrictions.disjunction();
						for(Lookup<PK> lookup : lookups){
							Conjunction and = Restrictions.conjunction();
							for(Field<?> field : CollectionTool.nullSafe(lookup.getFields())){
								and.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
							}
							or.add(and);
						}
						criteria.add(or);
						List<D> result = criteria.list();
						Collections.sort(result);//todo, make sure the datastore scans in order so we don't need to sort here
						return result;
					}
				}
			});
		TraceContext.finishSpan();
		return (List<D>)result;
	}
	
	
	/************************************ SortedStorageReader methods ****************************/

	@Override
	public D getFirst(final Config config) {
		TraceContext.startSpan(getName()+" getFirst");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					if(fieldInfo.getFieldAware()){
						Config nullSafeConfig = Config.nullSafe(config);
						nullSafeConfig.setLimit(1);
						String sql = SqlBuilder.getAll(config, tableName, fieldInfo.getFields());
						List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
						return CollectionTool.getFirst(result);
					}else{
						Criteria criteria = getCriteriaForConfig(config, session);
						criteria.setMaxResults(1);
						Object result = criteria.uniqueResult();
						return result;
					}
				}
			});
		TraceContext.finishSpan();
		@SuppressWarnings("unchecked")
		D databean = (D)result;
		return databean;
	}

	
	@Override
	public PK getFirstKey(final Config config) {
		TraceContext.startSpan(getName()+" getFirstKey");
		final String entityName = this.getPackagedTableName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					if(fieldInfo.getFieldAware()){
						Config nullSafeConfig = Config.nullSafe(config);
						nullSafeConfig.setLimit(1);
						String sql = SqlBuilder.getAll(config, tableName, fieldInfo.getPrimaryKeyFields());
						List<PK> result = JdbcTool.selectPrimaryKeys(session, fieldInfo, sql);
						return CollectionTool.getFirst(result);
					}else{
						Criteria criteria = session.createCriteria(entityName);
						ProjectionList projectionList = Projections.projectionList();
						int numFields = 0;
						for(Field<?> field : fieldInfo.getPrefixedPrimaryKeyFields()){
							projectionList.add(Projections.property(field.getPrefixedName()));
							++numFields;
						}
						criteria.setProjection(projectionList);
						addPrimaryKeyOrderToCriteria(criteria);
						criteria.setMaxResults(1);
						Object rows = criteria.uniqueResult();
						if(rows==null){ return null; }
						if(numFields==1){
							rows = new Object[]{rows};
						}
						PK pk = (PK)FieldSetTool.fieldSetFromHibernateResultUsingReflection(
								fieldInfo.getPrimaryKeyClass(), fieldInfo.getPrimaryKeyFields(), rows);
						return pk;
					}
				}
			});
		TraceContext.finishSpan();
		@SuppressWarnings("unchecked")
		PK pk = (PK)result;
		return pk;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<D> getWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		TraceContext.startSpan(getName()+" getWithPrefix");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					if(fieldInfo.getFieldAware()){
						String sql = SqlBuilder.getWithPrefixes(config, tableName, fieldInfo.getFields(), 
								ListTool.wrap(prefix), wildcardLastField);
						List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
						return result;
					}else{
						Criteria criteria = getCriteriaForConfig(config, session);
						Conjunction prefixConjunction = getPrefixConjunction(true, prefix, wildcardLastField);
						if(prefixConjunction == null){
							throw new IllegalArgumentException("cannot do a null prefix match.  Use getAll() instead");
						}
						criteria.add(prefixConjunction);
						List<D> result = criteria.list();
						Collections.sort(result);//todo, make sure the datastore scans in order so we don't need to sort here
						return result;
					}
				}
			});
		TraceContext.finishSpan();
		return (List<D>)result;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<D> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField, 
			final Config config) {
		TraceContext.startSpan(getName()+" getWithPrefixes");
		if(CollectionTool.isEmpty(prefixes)){ return new LinkedList<D>(); }
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					if(fieldInfo.getFieldAware()){
						String sql = SqlBuilder.getWithPrefixes(config, tableName, fieldInfo.getFields(), 
								prefixes, wildcardLastField);
						List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
						return result;
					}else{
						Criteria criteria = getCriteriaForConfig(config, session);
						Disjunction prefixesDisjunction = Restrictions.disjunction();
						if(prefixesDisjunction != null){
							for(Key<PK> prefix : prefixes){
								Conjunction prefixConjunction = getPrefixConjunction(true, prefix, wildcardLastField);
								prefixesDisjunction.add(prefixConjunction);
							}
							criteria.add(prefixesDisjunction);
						}
						List<D> result = criteria.list();
						Collections.sort(result);//todo, make sure the datastore scans in order so we don't need to sort here
						return result;
					}
				}
			});
		TraceContext.finishSpan();
		return (List<D>)result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PK> getKeysInRange(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config) {
		
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		return (List<PK>)getRangeInternal(range, true, config);

//		TraceContext.startSpan(getName()+" getKeysInRange");
//		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
//		Object result = executor.executeTask(
//			new HibernateTask() {
//				public Object run(Session session) {
//					if(fieldInfo.getFieldAware()){
//						String sql = SqlBuilder.getInRange(config, tableName, fieldInfo.getPrimaryKeyFields(), 
//								start, startInclusive, end, endInclusive);
//						List<PK> result = JdbcTool.selectPrimaryKeys(session, fieldInfo, sql);
//						return result;
//					}else{
//						Criteria criteria = getCriteriaForConfig(config, session);
//						//keys only business
//						ProjectionList projectionList = Projections.projectionList();
//						int numFields = 0;
//						for(Field<?> field : fieldInfo.getPrefixedPrimaryKeyFields()){
//							projectionList.add(Projections.property(field.getPrefixedName()));
//							++numFields;
//						}
//						criteria.setProjection(projectionList);
//						//end keys only business
//						addPrimaryKeyOrderToCriteria(criteria);
//						CriteriaTool.addRangesToCriteria(criteria, start, startInclusive, end, endInclusive, fieldInfo);
//						List<Object[]> rows = criteria.list();
//						//key only business
//						List<PK> result = ListTool.createArrayList(CollectionTool.size(rows));
//						for(Object row : IterableTool.nullSafe(rows)){
//							//hibernate will return a plain Object if it's a single col PK
//							Object[] rowCells;
//							if(row instanceof Object[]){ rowCells = (Object[])row; }
//							else{ rowCells = new Object[]{row}; }
//							result.add(FieldSetTool.fieldSetFromHibernateResultUsingReflection(
//									fieldInfo.getPrimaryKeyClass(), fieldInfo.getPrimaryKeyFields(), rowCells));
//						}
//						//end key only business
//						return result;
//					}
//				}
//			});
//		TraceContext.finishSpan();
//		
//		return (List<PK>)result;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<D> getRange(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config) {
		
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		return (List<D>)getRangeInternal(range, false, config);

//		TraceContext.startSpan(getName()+" getRange");
//		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
//		Object result = executor.executeTask(
//			new HibernateTask() {
//				public Object run(Session session) {
//					if(fieldInfo.getFieldAware()){
//						String sql = SqlBuilder.getInRange(config, tableName, fieldInfo.getFields(), 
//								start, startInclusive, end, endInclusive);
//						List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
//						return result;
//					}else{
//						Criteria criteria = getCriteriaForConfig(config, session);
//						addPrimaryKeyOrderToCriteria(criteria);
//						CriteriaTool.addRangesToCriteria(criteria, start, startInclusive, end, endInclusive, fieldInfo);
//						Object result = criteria.list();
//						return result;
//					}
//				}
//			});
//		TraceContext.finishSpan();
//		
//		return (List<D>)result;
	}
	
	
	protected List<? extends FieldSet<?>> getRangeInternal(final Range<PK> range, final boolean keysOnly,
			final Config config){
		String spanNameSuffix = keysOnly ? "getKeysInRange" : "getRange";
		TraceContext.startSpan(getName() + " " + spanNameSuffix);
		HibernateExecutor executor = HibernateExecutor.create(getClient(), config, false);
		Object result = executor.executeTask(new HibernateTask(){
			public Object run(Session session){
				if(fieldInfo.getFieldAware()){
					List<Field<?>> fieldsToSelect = keysOnly ? fieldInfo.getPrimaryKeyFields() : fieldInfo.getFields();
					String sql = SqlBuilder.getInRange(config, tableName, fieldsToSelect, range);
					List<? extends FieldSet<?>> result;
					if(keysOnly){
						result = JdbcTool.selectPrimaryKeys(session, fieldInfo, sql);
					}else{
						result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
					}
					return result;
				}else{
					Criteria criteria = getCriteriaForConfig(config, session);
					if(keysOnly){
						ProjectionList projectionList = Projections.projectionList();
						int numFields = 0;
						for(Field<?> field : fieldInfo.getPrefixedPrimaryKeyFields()){
							projectionList.add(Projections.property(field.getPrefixedName()));
							++numFields;
						}
						criteria.setProjection(projectionList);
					}
					addPrimaryKeyOrderToCriteria(criteria);
					CriteriaTool.addRangesToCriteria(criteria, range, fieldInfo);
					if(keysOnly){
						List<Object[]> rows = criteria.list();
						List<PK> result = ListTool.createArrayList(CollectionTool.size(rows));
						for(Object row : IterableTool.nullSafe(rows)){
							// hibernate will return a plain Object if it's a single col PK
							Object[] rowCells;
							if(row instanceof Object[]){
								rowCells = (Object[])row;
							}else{
								rowCells = new Object[]{row};
							}
							result.add(FieldSetTool.fieldSetFromHibernateResultUsingReflection(fieldInfo
									.getPrimaryKeyClass(), fieldInfo.getPrimaryKeyFields(), rowCells));
						}
						return result;
					}else{
						Object result = criteria.list();
						return result;
					}
				}
			}
		});
		TraceContext.finishSpan();

		return (List<PK>)result;
	}

	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<D> getPrefixedRange(
			final PK prefix, final boolean wildcardLastField,
			final PK start, final boolean startInclusive, 
			final Config config) {

		TraceContext.startSpan(getName()+" getPrefixedRange");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					if(fieldInfo.getFieldAware()){
						String sql = SqlBuilder.getWithPrefixInRange(config, tableName, fieldInfo.getFields(), 
								prefix, wildcardLastField, start, startInclusive, null, false);
						List<D> result = JdbcTool.selectDatabeans(session, fieldInfo, sql);
						return result;
					}else{
						Criteria criteria = getCriteriaForConfig(config, session);
						addPrimaryKeyOrderToCriteria(criteria);
						Conjunction prefixConjunction = getPrefixConjunction(true, prefix, wildcardLastField);
						if(prefixConjunction != null){
							criteria.add(prefixConjunction);
						}		
						if(start != null && CollectionTool.notEmpty(start.getFields())){
							List<Field<?>> startFields = FieldTool.prependPrefixes(fieldInfo.getKeyFieldName(), start.getFields());
							int numNonNullStartFields = FieldTool.countNonNullLeadingFields(startFields);
							Disjunction d = Restrictions.disjunction();
							for(int i=numNonNullStartFields; i > 0; --i){
								Conjunction c = Restrictions.conjunction();
								for(int j=0; j < i; ++j){
									Field<?> startField = startFields.get(j);
									if(j < (i-1)){
										c.add(Restrictions.eq(startField.getPrefixedName(), startField.getValue()));
									}else{
										if(startInclusive && i==numNonNullStartFields){
											c.add(Restrictions.ge(startField.getPrefixedName(), startField.getValue()));
										}else{
											c.add(Restrictions.gt(startField.getPrefixedName(), startField.getValue()));
										}
									}
								}
								d.add(c);
							}
							criteria.add(d);
						}
						Object result = criteria.list();
						return result;
					}
				}
			});
		TraceContext.finishSpan();
		return (List<D>)result;
	}
	
	@Override
	public PeekableIterable<PK> scanKeys(
			PK start, boolean startInclusive, 
			PK end, boolean endInclusive, 
			Config config){
		return new PrimaryKeyScanner<PK>(this, start, startInclusive, end, endInclusive, 
				config, DEFAULT_ITERATE_BATCH_SIZE);
	}
	
	@Override
	public PeekableIterable<D> scan(
			PK start, boolean startInclusive, 
			PK end, boolean endInclusive, 
			Config config){
		return new Scanner<PK,D>(this, start, startInclusive, end, endInclusive, 
				config, DEFAULT_ITERATE_BATCH_SIZE);
	}
	
	
	/********************************* hibernate helpers ***********************************************/
	//shouldn't need this for innodb.  not sure if it hurts or not though.  see Handler_read_rnd_next (innodb may sort anyway?)
	protected void addPrimaryKeyOrderToCriteria(Criteria criteria){
		for(Field<?> field : fieldInfo.getPrefixedPrimaryKeyFields()){
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
			criteria.setFetchSize(config.getLimit());
		}
		if(config.getOffset()!=null){
			criteria.setFirstResult(config.getOffset());
		}
		return criteria;
	}

	
	protected Conjunction getPrefixConjunction(boolean usePrefixedFieldNames,
			Key<PK> prefix, final boolean wildcardLastField){
		int numNonNullFields = FieldSetTool.getNumNonNullFields(prefix);
		if(numNonNullFields==0){ return null; }
		Conjunction conjunction = Restrictions.conjunction();
		int numFullFieldsFinished = 0;
		List<Field<?>> fields = prefix.getFields();
		if(usePrefixedFieldNames){
			fields = FieldTool.prependPrefixes(fieldInfo.getKeyFieldName(), fields);
		}
		for(Field<?> field : fields){
			if(numFullFieldsFinished >= numNonNullFields) break;
			if(field.getValue()==null) {
				throw new DataAccessException("Prefix query on "+
						prefix.getClass()+" cannot contain intermediate nulls.");
			}
			boolean lastNonNullField = (numFullFieldsFinished == numNonNullFields-1);
			boolean stringField = !(field instanceof BasePrimitiveField<?>);
			boolean canDoPrefixMatchOnField = wildcardLastField && lastNonNullField && stringField;
//			String fieldNameWithPrefixIfNecessary = usePrefixedFieldNames
//					? field.getPrefixedName() : field.getName();
			String fieldNameWithPrefixIfNecessary = field.getPrefixedName();
			if(canDoPrefixMatchOnField){
				conjunction.add(Restrictions.like(fieldNameWithPrefixIfNecessary, 
						field.getValue().toString(), MatchMode.START));
			}else{
				conjunction.add(Restrictions.eq(fieldNameWithPrefixIfNecessary, field.getValue()));
			}
			++numFullFieldsFinished;
		}
		return conjunction;
	}
	

}
