package com.hotpads.datarouter.client.imp.hibernate.node;

import java.util.Collection;

import com.hotpads.datarouter.client.imp.hibernate.op.write.HibernatePutOp;
import com.hotpads.datarouter.client.imp.hibernate.util.HibernateResultParser;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcDeleteAllOp;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcDeleteOp;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcIndexDeleteOp;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcPrefixDeleteOp;
import com.hotpads.datarouter.client.imp.jdbc.op.write.JdbcUniqueIndexDeleteOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class HibernateNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends HibernateReaderNode<PK,D,F>
implements PhysicalIndexedSortedMapStorageNode<PK,D>{

	private final JdbcFieldCodecFactory fieldCodecFactory;
	
	public HibernateNode(NodeParams<PK,D,F> params, JdbcFieldCodecFactory fieldCodecFactory, 
			HibernateResultParser resultParser){
		super(params, resultParser, fieldCodecFactory);
		this.fieldCodecFactory = fieldCodecFactory;
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return this;
	}
	
	
	/************************************ MapStorageWriter methods ****************************/

	@Override
	public void put(final D databean, final Config config) {
		String opName = MapStorageWriter.OP_put;
		HibernatePutOp<PK,D,F> op = new HibernatePutOp<>(this, DrListTool.wrap(databean), config);
		new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	
	@Override
	public void putMulti(Collection<D> databeans, final Config config) {
		String opName = MapStorageWriter.OP_putMulti;
		if(DrCollectionTool.isEmpty(databeans)){
			return;
		}
		HibernatePutOp<PK,D,F> op = new HibernatePutOp<>(this, databeans, config);
		new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}
	
	@Override
	public void deleteAll(final Config config) {
		String opName = MapStorageWriter.OP_deleteAll;
		JdbcDeleteAllOp<PK,D,F> op = new JdbcDeleteAllOp<>(this, config);
		new SessionExecutorImpl<>(op, getTraceName(opName)).call();
		
	}

	@Override
	public void delete(PK key, Config config){
		String opName = MapStorageWriter.OP_delete;
		JdbcDeleteOp<PK,D> op = new JdbcDeleteOp<>(this, fieldCodecFactory, DrListTool.wrap(key), config);
		new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	@Override
	public void deleteMulti(final Collection<PK> keys, final Config config){
		String opName = MapStorageWriter.OP_deleteMulti;
		if(DrCollectionTool.isEmpty(keys)){
			return;//avoid starting txn
		}
		JdbcDeleteOp<PK,D> op = new JdbcDeleteOp<>(this, fieldCodecFactory, keys, config);
		new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}
	

	/************************************ IndexedStorageWriter methods ****************************/

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		String opName = IndexedStorageWriter.OP_deleteUnique;
		JdbcUniqueIndexDeleteOp<PK,D> op = new JdbcUniqueIndexDeleteOp<>(this, fieldCodecFactory, DrListTool
				.wrap(uniqueKey), config);
		new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}
	
	@Override
	public void deleteMultiUnique(final Collection<? extends UniqueKey<PK>> uniqueKeys, final Config config){
		String opName = IndexedStorageWriter.OP_deleteMultiUnique;
		if(DrCollectionTool.isEmpty(uniqueKeys)){
			return;//avoid starting txn
		}
		JdbcUniqueIndexDeleteOp<PK,D> op = new JdbcUniqueIndexDeleteOp<>(this, fieldCodecFactory, uniqueKeys,
				config);
		new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}
	
	@Override
	public void delete(final Lookup<PK> lookup, final Config config) {
		String opName = IndexedStorageWriter.OP_indexDelete;
		JdbcIndexDeleteOp<PK,D> op = new JdbcIndexDeleteOp<>(this, fieldCodecFactory, lookup, config);
		new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}
	
	@Override
	public <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		// TODO implement managed indexes for Hibernate
		throw new NotImplementedException();
	}
	

	/************************************ SortedStorageWriter methods ****************************/

	@Override
	public void deleteRangeWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		String opName = SortedStorageWriter.OP_deleteRangeWithPrefix;
		JdbcPrefixDeleteOp<PK,D> op = new JdbcPrefixDeleteOp<>(this, fieldCodecFactory, prefix, 
				wildcardLastField, config);
		new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	
	
}
