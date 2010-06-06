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
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.base.physical.BasePhysicalNode;
import com.hotpads.datarouter.node.scanner.Scanner;
import com.hotpads.datarouter.node.type.physical.PhysicalIndexedSortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.trace.TraceContext;
import com.hotpads.util.core.BatchTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.iterable.PeekableIterator;

public class HibernateReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>> 
extends BasePhysicalNode<PK,D>
implements PhysicalIndexedSortedStorageReaderNode<PK,D>{
	protected Logger logger = Logger.getLogger(getClass());

	public HibernateReaderNode(Class<D> databeanClass, 
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HibernateReaderNode(Class<D> databeanClass,
			DataRouter router, String clientName) {
		super(databeanClass, router, clientName);
	}

	@Override
	public HibernateClientImp getClient(){
		return (HibernateClientImp)this.router.getClient(getClientName());
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return null;
	}
	
	@Override
	public void clearThreadSpecificState(){
		//TODO maybe clear the hibernate session here through the client??
	}

	
	/************************************ MapStorageReader methods ****************************/
	
	public static final int defaultIterateBatchSize = 25;
	
	@Override
	public boolean exists(UniqueKey<PK> key, Config config) {
		return this.get(key, config) != null;
	}


	@SuppressWarnings("unchecked")
	@Override
	public D get(final UniqueKey<PK> key, final Config config) {
		if(key==null){ return null; }
		TraceContext.startSpan(getName()+" get");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					List<Field<?>> fields = key.getFields();
					for(Field<?> field : fields){
						criteria.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
					}
					D result = (D)criteria.uniqueResult();
					return result;
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
					Criteria criteria = getCriteriaForConfig(config, session);
					Object listOfDatabeans = criteria.list();
					return listOfDatabeans;//todo, make sure the datastore scans in order so we don't need to sort here
				}
			});
		TraceContext.finishSpan();
		return (List<D>)result;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public List<D> getMulti(final Collection<? extends UniqueKey<PK>> keys, final Config config) {	
		TraceContext.startSpan(getName()+" getMulti");	
		if(CollectionTool.isEmpty(keys)){ return new LinkedList<D>(); }
//		final Class<? extends Databean> persistentClass = CollectionTool.getFirst(keys).getDatabeanClass();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					int batchSize = defaultIterateBatchSize;
					if(config!=null && config.getIterateBatchSize()!=null){
						batchSize = config.getIterateBatchSize();
					}
					List<? extends Key<PK>> sortedKeys = ListTool.createArrayList(keys);
					Collections.sort(sortedKeys);
					int numBatches = BatchTool.getNumBatches(sortedKeys.size(), batchSize);
					List<D> all = ListTool.createArrayList(keys.size());
					for(int batchNum=0; batchNum < numBatches; ++batchNum){
						List<? extends Key<PK>> keyBatch = BatchTool.getBatch(sortedKeys, batchSize, batchNum);
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
						List<D> batch = criteria.list();
						Collections.sort(batch);//can sort here because batches were already sorted
						ListTool.nullSafeArrayAddAll(all, batch);
					}
					return all;
				}
			});
		TraceContext.finishSpan();
		return (List<D>)result;
	}

	
	
	/************************************ IndexedStorageReader methods ****************************/
	
	@Override
	@SuppressWarnings("unchecked")
	public List<D> lookup(final Lookup<PK> lookup, final Config config) {
		TraceContext.startSpan(getName()+" lookup");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					for(Field<?> field : CollectionTool.nullSafe(lookup.getFields())){
						criteria.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
					}
					List<D> result = criteria.list();
					Collections.sort(result);//todo, make sure the datastore scans in order so we don't need to sort here
					return result;
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
					Criteria criteria = getCriteriaForConfig(config, session);
					criteria.setMaxResults(1);
					Object result = criteria.uniqueResult();
					return result;//TODO this method needs to send sorting order to the datastore!!!!!!!!!!
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
		final String entityName = this.getPackagedPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = session.createCriteria(entityName);
					ProjectionList projectionList = Projections.projectionList();
					int numFields = 0;
					for(Field<?> field : primaryKeyFields){
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
					PK pk = (PK)FieldTool.fieldSetFromSqlUsingReflection(primaryKeyClass, primaryKeyFields, rows);
					return pk;
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
					Criteria criteria = getCriteriaForConfig(config, session);
					Conjunction prefixConjunction = getPrefixConjunction(prefix, wildcardLastField);
					if(prefixConjunction == null){
						throw new IllegalArgumentException("cannot do a null prefix match.  Use getAll() instead");
					}
					criteria.add(prefixConjunction);
					List<D> result = criteria.list();
					Collections.sort(result);//todo, make sure the datastore scans in order so we don't need to sort here
					return result;
				}
			});
		TraceContext.finishSpan();
		return (List<D>)result;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<D> getWithPrefixes(final Collection<? extends PK> prefixes, final boolean wildcardLastField, final Config config) {
		TraceContext.startSpan(getName()+" getWithPrefixes");
		if(CollectionTool.isEmpty(prefixes)){ return new LinkedList<D>(); }
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					Disjunction prefixesDisjunction = Restrictions.disjunction();
					if(prefixesDisjunction != null){
						for(Key<PK> prefix : prefixes){
							Conjunction prefixConjunction = getPrefixConjunction(prefix, wildcardLastField);
							prefixesDisjunction.add(prefixConjunction);
						}
						criteria.add(prefixesDisjunction);
					}
					List<D> result = criteria.list();
					Collections.sort(result);//todo, make sure the datastore scans in order so we don't need to sort here
					return result;
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

		TraceContext.startSpan(getName()+" getRange");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					ProjectionList projectionList = Projections.projectionList();
					int numFields = 0;
					for(Field<?> field : primaryKeyFields){
						projectionList.add(Projections.property(field.getPrefixedName()));
						++numFields;
					}
					criteria.setProjection(projectionList);
					addPrimaryKeyOrderToCriteria(criteria);
					addRangesToCriteria(criteria, start, startInclusive, end, endInclusive);
					List<Object[]> rows = criteria.list();
					List<PK> result = ListTool.createArrayList(CollectionTool.size(rows));
					for(Object[] row : IterableTool.nullSafe(rows)){
						result.add(FieldTool.fieldSetFromSqlUsingReflection(primaryKeyClass, primaryKeyFields, row));
					}
					return result;
				}
			});
		TraceContext.finishSpan();
		
		return (List<PK>)result;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<D> getRange(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config) {

		TraceContext.startSpan(getName()+" getRange");
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, false);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					addPrimaryKeyOrderToCriteria(criteria);
					addRangesToCriteria(criteria, start, startInclusive, end, endInclusive);
					Object result = criteria.list();
					return result;
				}
			});
		TraceContext.finishSpan();
		
		return (List<D>)result;
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
					Criteria criteria = getCriteriaForConfig(config, session);
					
					addPrimaryKeyOrderToCriteria(criteria);

					Conjunction prefixConjunction = getPrefixConjunction(prefix, wildcardLastField);
					if(prefixConjunction != null){
						criteria.add(prefixConjunction);
					}
										
					if(start != null && CollectionTool.notEmpty(start.getFields())){
						List<Field<?>> startFields = ListTool.createArrayList(start.getFields());
						int numNonNullStartFields = Field.countNonNullLeadingFields(startFields);
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
			});
		TraceContext.finishSpan();
		return (List<D>)result;
	}
	
	@Override
	public PeekableIterator<D> scan(
			PK start, boolean startInclusive, 
			PK end, boolean endInclusive, 
			Config config){
		return new Scanner<PK,D>(this, start, startInclusive, end, endInclusive, config, 1000);
	}

	
	/********************************* helpers ***********************************************/
	
	protected void addPrimaryKeyOrderToCriteria(Criteria criteria){
		for(Field<?> field : this.primaryKeyFields){
			criteria.addOrder(Order.asc(field.getPrefixedName()));
		}
	}
	
	
	protected Criteria getCriteriaForConfig(Config config, Session session){
		final String entityName = this.getPackagedPhysicalName();
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

	
	protected Conjunction getPrefixConjunction(Key<PK> prefix, final boolean wildcardLastField){
		int numNonNullFields = 0;
		for(Comparable<?> value : CollectionTool.nullSafe(prefix.getFieldValues())){
			if(value != null){
				++numNonNullFields;
			}
		}
		if(numNonNullFields==0){
			return null; 
		}
		Conjunction conjunction = Restrictions.conjunction();
		int numFullFieldsFinished = 0;
		for(Field<?> field : CollectionTool.nullSafe(prefix.getFields())){
			if(numFullFieldsFinished < numNonNullFields){
				boolean lastNonNullField = numFullFieldsFinished == numNonNullFields - 1;
				boolean stringField = !(field instanceof PrimitiveField<?>);
				
				boolean canDoPrefixMatchOnField = wildcardLastField && lastNonNullField && stringField;
				
				if(canDoPrefixMatchOnField){
					conjunction.add(Restrictions.like(field.getPrefixedName(), field.getValue().toString(), MatchMode.START));
				}else{
					conjunction.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
				}
				++numFullFieldsFinished;
			}
		}
		return conjunction;
	}

	
	protected void addRangesToCriteria(Criteria criteria, 
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive){

		if(start != null && CollectionTool.notEmpty(start.getFields())){
			List<Field<?>> startFields = ListTool.createArrayList(start.getFields());
			int numNonNullStartFields = Field.countNonNullLeadingFields(startFields);
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
		
		if(end != null && CollectionTool.notEmpty(end.getFields())){
			List<Field<?>> endFields = ListTool.createArrayList(end.getFields());
			int numNonNullEndFields = Field.countNonNullLeadingFields(endFields);
			Disjunction d = Restrictions.disjunction();
			for(int i=0; i < numNonNullEndFields; ++i){
				Conjunction c = Restrictions.conjunction();
				for(int j=0; j <= i; ++j){
					Field<?> endField = endFields.get(j);
					if(j==i){
						if(endInclusive){
							c.add(Restrictions.le(endField.getPrefixedName(), endField.getValue()));
						}else{
							c.add(Restrictions.lt(endField.getPrefixedName(), endField.getValue()));
						}
					}else{
						c.add(Restrictions.eq(endField.getPrefixedName(), endField.getValue()));
					}
				}
				d.add(c);
			}
			criteria.add(d);
		}
	}
	
	
	
	
	
}
