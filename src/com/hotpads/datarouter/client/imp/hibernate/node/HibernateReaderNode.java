package com.hotpads.datarouter.client.imp.hibernate.node;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
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
import com.hotpads.datarouter.storage.index.Lookup;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;

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

	
	/************************************ MapStorageReader methods ****************************/
	
	@Override
	public boolean exists(Key<D> key, Config config) {
		return this.get(key, config) != null;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public D get(final Key<D> key, Config config) {
		final String entityName = this.getPackagedPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = session.createCriteria(entityName);
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
		final String entityName = this.getPackagedPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = session.createCriteria(entityName);
					if(config!=null && config.getLimit()!=null){
						criteria.setMaxResults(config.getLimit());
					}
					Object listOfDatabeans = criteria.list();
					return listOfDatabeans;
				}
			});
		return (List<D>)result;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public List<D> getMulti(final Collection<? extends Key<D>> keys, Config config) {		
		final String entityName = this.getPackagedPhysicalName();
		if(CollectionTool.isEmpty(keys)){ return null; }
//		final Class<? extends Databean> persistentClass = CollectionTool.getFirst(keys).getDatabeanClass();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = session.createCriteria(entityName);
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
		final String entityName = this.getPackagedPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = session.createCriteria(entityName);
					for(Field field : CollectionTool.nullSafe(lookup.getFields())){
						criteria.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
					}
					if(config != null && config.getLimit() != null){
						criteria.setMaxResults(config.getLimit());
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
		final String entityName = this.getPackagedPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = session.createCriteria(entityName);
					for (Lookup<D> lookup : lookups) {
						Disjunction or = Restrictions.disjunction();
						for(Field field : CollectionTool.nullSafe(lookup.getFields())){
							or.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
						}
						criteria.add(or);
					}
					if(config != null && config.getLimit() != null){
						criteria.setMaxResults(config.getLimit());
					}
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
		final String entityName = this.getPackagedPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = session.createCriteria(entityName);
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
	public List<D> getRangeWithPrefix(final Key<D> prefix, final Config config) {
		final String entityName = this.getPackagedPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					
					int numNonNullFields = 0;
					for(Comparable<?> value : CollectionTool.nullSafe(prefix.getFieldValues())){
						if(value != null){
							++numNonNullFields;
						}
					}
					
					Criteria criteria = session.createCriteria(entityName);
					int numFullFieldsFinished = 0;
					for(Field field : CollectionTool.nullSafe(prefix.getFields())){
						if(numFullFieldsFinished < numNonNullFields){
							boolean lastNonNullField = numFullFieldsFinished == numNonNullFields - 1;
							boolean stringField = field.getValue() instanceof String;
							
							boolean canDoPrefixMatchOnField = lastNonNullField && stringField;
							
							if(canDoPrefixMatchOnField){
								criteria.add(Restrictions.like(field.getPrefixedName(), (String)field.getValue(), MatchMode.START));
							}else{
								criteria.add(Restrictions.eq(field.getPrefixedName(), field.getValue()));
							}
							++numFullFieldsFinished;
						}
					}
					if(config != null && config.getLimit() != null){
						criteria.setMaxResults(config.getLimit());
					}
					Object result = criteria.list();
					return result;
				}
			});
		return (List<D>)result;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<D> getRange(
			final Key<D> start, final boolean startInclusive, 
			final Key<D> end, final boolean endInclusive, 
			final Config config) {
		
		final String entityName = this.getPackagedPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(), config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = session.createCriteria(entityName);
					if(start != null || end != null){
						int numFields = start.getFields().size();
						Iterator<Field> startFields = start==null?null:start.getFields().iterator();
						Iterator<Field> endFields = end==null?null:end.getFields().iterator();
						int fieldNum = 0;
						Conjunction interFieldConjunction = Restrictions.conjunction();
						while(startFields.hasNext()){
							++fieldNum;//one based
							Field startField = startFields==null?null:startFields.next();
							Field endField = endFields==null?null:endFields.next();
							Conjunction intraFieldConjunction = Restrictions.conjunction();
							if(fieldNum<numFields){
								if(startField!=null){ 
									intraFieldConjunction.add(Restrictions.ge(startField.getPrefixedName(), startField.getValue())); 
								}
								if(endField!=null){ 
									intraFieldConjunction.add(Restrictions.le(endField.getPrefixedName(), endField.getValue())); 
								}
							}else{//last field
								if(startField!=null){ 
									if(startInclusive){
										intraFieldConjunction.add(Restrictions.ge(startField.getPrefixedName(), startField.getValue())); 
									}else{
										intraFieldConjunction.add(Restrictions.gt(startField.getPrefixedName(), startField.getValue()));
									}
								}
								if(endField!=null){ 
									if(endInclusive){
										intraFieldConjunction.add(Restrictions.le(endField.getPrefixedName(), endField.getValue()));
									}else{
										intraFieldConjunction.add(Restrictions.lt(endField.getPrefixedName(), endField.getValue()));
									}
								}
							}
							interFieldConjunction.add(intraFieldConjunction);
						}
						criteria.add(interFieldConjunction);
					}
					
					if(config != null && config.getLimit() != null){
						criteria.setMaxResults(config.getLimit());
					}
					Object result = criteria.list();
					return result;
				}
			});
		return (List<D>)result;
	}
	
	
	
	
	
	
	
	
	
	
}
