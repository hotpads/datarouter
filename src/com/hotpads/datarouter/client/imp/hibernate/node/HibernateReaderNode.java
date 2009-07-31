package com.hotpads.datarouter.client.imp.hibernate.node;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.hibernate.HibernateExecutor;
import com.hotpads.datarouter.client.imp.hibernate.HibernateTask;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.base.physical.BasePhysicalNode;
import com.hotpads.datarouter.node.type.physical.PhysicalIndexedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.index.Lookup;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.StringTool;

public class HibernateReaderNode<D extends Databean> 
extends BasePhysicalNode<D>
implements PhysicalIndexedStorageReaderNode<D>
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
	public List<D> lookup(final Lookup<D> key, final Config config) {
		final String entityName = this.getPackagedPhysicalName();
		HibernateExecutor executor = HibernateExecutor.create(this.getClient(),	config, null);
		Object result = executor.executeTask(
			new HibernateTask() {
				public Object run(Session session) {
					Criteria criteria = session.createCriteria(entityName);
					for(Field field : CollectionTool.nullSafe(key.getFields())){
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
	
}
