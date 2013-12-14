package com.hotpads.datarouter.client.imp.hibernate.node;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.hibernate.op.write.HibernateDeleteAllOp;
import com.hotpads.datarouter.client.imp.hibernate.op.write.HibernateDeleteOp;
import com.hotpads.datarouter.client.imp.hibernate.op.write.HibernateIndexDeleteOp;
import com.hotpads.datarouter.client.imp.hibernate.op.write.HibernatePrefixDeleteOp;
import com.hotpads.datarouter.client.imp.hibernate.op.write.HibernatePutOp;
import com.hotpads.datarouter.client.imp.hibernate.op.write.HibernateUniqueIndexDeleteOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

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
		HibernatePutOp<PK,D,F> op = new HibernatePutOp<PK,D,F>(this, "put", ListTool.wrap(databean), config);
		op.call();
	}

	
	@Override
	public void putMulti(Collection<D> databeans, final Config config) {
		if(CollectionTool.isEmpty(databeans)){ return; }//avoid starting txn
		HibernatePutOp<PK,D,F> op = new HibernatePutOp<PK,D,F>(this, "putMulti", databeans, config);
		op.call();
	}
	
	@Override
	public void deleteAll(final Config config) {
		HibernateDeleteAllOp<PK,D,F> op = new HibernateDeleteAllOp<PK,D,F>(this, "deleteAll", config);
		op.call();
	}

	@Override
	public void delete(PK key, Config config){
		HibernateDeleteOp<PK,D,F> op = new HibernateDeleteOp<PK,D,F>(this, "delete", ListTool.wrap(key), config);
		op.call();
	}

	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config){
		if(CollectionTool.isEmpty(keys)){ return; }//avoid starting txn
		HibernateDeleteOp<PK,D,F> op = new HibernateDeleteOp<PK,D,F>(this, "deleteMulti", keys, config);
		op.call();
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		HibernateUniqueIndexDeleteOp<PK,D,F> op = new HibernateUniqueIndexDeleteOp<PK,D,F>(this, "deleteUnique", 
				ListTool.wrap(uniqueKey), config);
		op.call();
	}
	
	@Override
	public void deleteMultiUnique(final Collection<? extends UniqueKey<PK>> uniqueKeys, final Config config){
		if(CollectionTool.isEmpty(uniqueKeys)){ return; }//avoid starting txn
		HibernateUniqueIndexDeleteOp<PK,D,F> op = new HibernateUniqueIndexDeleteOp<PK,D,F>(this, "deleteMultiUnique", uniqueKeys, 
				config);
		op.call();
	}

	@Override
	public void deleteRangeWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		HibernatePrefixDeleteOp<PK,D,F> op = new HibernatePrefixDeleteOp<PK,D,F>(this,
				"deleteRangeWithPrefix", prefix, wildcardLastField, config);
		op.call();
	}
	
	@Override
	public void delete(final Lookup<PK> lookup, final Config config) {
		HibernateIndexDeleteOp<PK,D,F> op = new HibernateIndexDeleteOp<PK,D,F>(this, "indexDelete", lookup, config);
		op.call();
	}

	
	
}
