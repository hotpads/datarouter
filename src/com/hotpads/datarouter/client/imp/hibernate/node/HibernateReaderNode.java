package com.hotpads.datarouter.client.imp.hibernate.node;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.hibernate.HibernateExecutor;
import com.hotpads.datarouter.client.imp.hibernate.HibernateTask;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.base.physical.BasePhysicalNode;
import com.hotpads.datarouter.node.type.physical.PhysicalIndexedSortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.lookup.Lookup;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;

public class HibernateReaderNode<D extends Databean> 
extends BasePhysicalNode<D>
implements PhysicalIndexedSortedStorageReaderNode<D>
{

	public HibernateReaderNode(Class<D> databeanClass, DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HibernateReaderNode(Class<D> databeanClass, DataRouter router, String clientName) {
		super(databeanClass, router, clientName);
	}

	@Override
	public HibernateClientImp getClient(){
		return (HibernateClientImp)this.router.getClient(getClientName());
	}
	
	@Override
	public Node<D> getMaster() {
		return null;
	}
	
	@Override
	public void clearThreadSpecificState(){
		//TODO maybe clear the hibernate session here through the client??
	}

	
	/************************************ MapStorageReader methods ****************************/
	
	@Override
	public boolean exists(Key<D> key, Config config) {
		return this.get(key, config) != null;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public D get(final Key<D> key, final Config config) {
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					List<Field> fields = key.getFields();
					for(Field field : fields){
						criteria.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
					}
					Object result = criteria.uniqueResult();
					return result;
				}
			});
		return (D)result;
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public List<D> getAll(final Config config) {
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					Object listOfDatabeans = criteria.list();
					return listOfDatabeans;
				}
			});
		return (List<D>)result;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public List<D> getMulti(final Collection<? extends Key<D>> keys, final Config config) {		
		if(CollectionTool.isEmpty(keys)){ return null; }
//		final Class<? extends Databean> persistentClass = CollectionTool.getFirst(keys).getDatabeanClass();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					Disjunction orSeparatedIds = Restrictions.disjunction();
					for(Key<D> key : CollectionTool.nullSafe(keys)){
						Conjunction possiblyCompoundId = Restrictions.conjunction();
						List<Field> fields = key.getFields();
						for(Field field : fields){
							possiblyCompoundId.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
						}
						orSeparatedIds.add(possiblyCompoundId);
					}
					criteria.add(orSeparatedIds);
					Object listOfDatabeans = criteria.list();
					return listOfDatabeans;
				}
			});
		return (List<D>)result;
	}

	
	
	/************************************ IndexedStorageReader methods ****************************/
	
	@Override
	@SuppressWarnings("unchecked")
	public List<D> lookup(final Lookup<D> lookup, final Config config) {
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					for(Field field : CollectionTool.nullSafe(lookup.getFields())){
						criteria.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
					}
					Object result = criteria.list();
					return result;
				}
			});
		return (List<D>)result;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<D> lookup(final Collection<? extends Lookup<D>> lookups, final Config config) {
		if(CollectionTool.isEmpty(lookups)){ return null; }
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					Disjunction or = Restrictions.disjunction();
					for(Lookup<D> lookup : lookups){
						Conjunction and = Restrictions.conjunction();
						for(Field field : CollectionTool.nullSafe(lookup.getFields())){
							and.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
						}
						or.add(and);
					}
					criteria.add(or);
					Object result = criteria.list();
					return result;
				}
			});
		return (List<D>)result;
	}
	
	
	/************************************ SortedStorageReader methods ****************************/

	@SuppressWarnings("unchecked")
	@Override
	public D getFirst(final Config config) {
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					criteria.setMaxResults(1);
					Object result = criteria.uniqueResult();
					return result;
				}
			});
		return (D)result;
	}

//	@Override
//	public Key<D> getFirstKey(final Config config) {
//		this.datab
//		final String entityName = this.getPackagedPhysicalName();
//		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
//		Object result = executor.executeTask(
//			new HibernateTask() {
//				public Object run(Session session) {
//					Criteria criteria = session.createCriteria(entityName);
//					ProjectionList projection = Projections.projectionList();
//					session.
//					criteria.setMaxResults(1);
//					Object result = criteria.uniqueResult();
//					return result;
//				}
//			});
//		return (D)result;
//	}

	@SuppressWarnings("unchecked")
	@Override
	public List<D> getWithPrefix(final Key<D> prefix, final boolean wildcardLastField, final Config config) {
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					Conjunction prefixConjunction = getPrefixConjunction(prefix, wildcardLastField);
					if(prefixConjunction != null){
						criteria.add(prefixConjunction);
					}
					Object result = criteria.list();
					return result;
				}
			});
		return (List<D>)result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<D> getWithPrefixes(final Collection<? extends Key<D>> prefixes, final boolean wildcardLastField, final Config config) {
		if(CollectionTool.isEmpty(prefixes)){ return null; }
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					Disjunction prefixesDisjunction = Restrictions.disjunction();
					if(prefixesDisjunction != null){
						for(Key<D> prefix : prefixes){
							Conjunction prefixConjunction = getPrefixConjunction(prefix, wildcardLastField);
							prefixesDisjunction.add(prefixConjunction);
						}
						criteria.add(prefixesDisjunction);
					}
					Object result = criteria.list();
					return result;
				}
			});
		return (List<D>)result;
	}
	
	private Conjunction getPrefixConjunction(Key<D> prefix, final boolean wildcardLastField){
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
		for(Field field : CollectionTool.nullSafe(prefix.getFields())){
			if(numFullFieldsFinished < numNonNullFields){
				boolean lastNonNullField = numFullFieldsFinished == numNonNullFields - 1;
				boolean stringField = field.getValue() instanceof String;
				
				boolean canDoPrefixMatchOnField = wildcardLastField && lastNonNullField && stringField;
				
				if(canDoPrefixMatchOnField){
					conjunction.add(Restrictions.like(field.getPrefixedName(), (String)field.getValue(), MatchMode.START));
				}else{
					conjunction.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
				}
				++numFullFieldsFinished;
			}
		}
		return conjunction;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<D> getRange(
			final Key<D> start, final boolean startInclusive, 
			final Key<D> end, final boolean endInclusive, 
			final Config config) {
		
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					
					addOrderToCriteriaUsingPrimaryKeys(criteria, start, end);
										
					if(start != null && CollectionTool.notEmpty(start.getFields())){
						List<Field> startFields = ListTool.createArrayList(start.getFields());
						int numNonNullStartFields = Field.countNonNullLeadingFields(startFields);
						Disjunction d = Restrictions.disjunction();
						for(int i=numNonNullStartFields; i > 0; --i){
							Conjunction c = Restrictions.conjunction();
							for(int j=0; j < i; ++j){
								Field startField = startFields.get(j);
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
						List<Field> endFields = ListTool.createArrayList(end.getFields());
						int numNonNullEndFields = Field.countNonNullLeadingFields(endFields);
						Disjunction d = Restrictions.disjunction();
						for(int i=0; i < numNonNullEndFields; ++i){
							Conjunction c = Restrictions.conjunction();
							for(int j=0; j <= i; ++j){
								Field endField = endFields.get(j);
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
					Object result = criteria.list();
					return result;
				}
			});
		return (List<D>)result;
	}
	

	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<D> getPrefixedRange(
			final Key<D> prefix, final boolean wildcardLastField,
			final Key<D> start, final boolean startInclusive, 
			final Config config) {
		
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = getCriteriaForConfig(config, session);
					
					addOrderToCriteriaUsingPrimaryKeys(criteria, start, null);

					Conjunction prefixConjunction = getPrefixConjunction(prefix, wildcardLastField);
					if(prefixConjunction != null){
						criteria.add(prefixConjunction);
					}
										
					if(start != null && CollectionTool.notEmpty(start.getFields())){
						List<Field> startFields = ListTool.createArrayList(start.getFields());
						int numNonNullStartFields = Field.countNonNullLeadingFields(startFields);
						Disjunction d = Restrictions.disjunction();
						for(int i=numNonNullStartFields; i > 0; --i){
							Conjunction c = Restrictions.conjunction();
							for(int j=0; j < i; ++j){
								Field startField = startFields.get(j);
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
		return (List<D>)result;
	}
	
	
	
	protected void addOrderToCriteriaUsingPrimaryKeys(Criteria criteria, Key<D> start, Key<D> end){
		if(ObjectTool.bothNull(start, end)){
			return;//can't figure out the order
		}
		Key<D> key = start!=null?start:end;
		for(Field field : key.getFields()){
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
	
	
	
	
	
	
	
}
