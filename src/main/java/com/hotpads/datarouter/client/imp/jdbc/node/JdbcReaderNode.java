package com.hotpads.datarouter.client.imp.jdbc.node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcCountOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetAllOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetFirstKeyOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetFirstOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetKeysOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetPrefixedRangeOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetRangeUncheckedOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetWithPrefixesOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcLookupOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcLookupUniqueOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

public class JdbcReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements MapStorageReader<PK,D>,
		SortedStorageReader<PK,D>,
		IndexedStorageReader<PK,D>{
	
	protected Logger logger = Logger.getLogger(getClass());
	
	/******************************* constructors ************************************/

	public JdbcReaderNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, fielderClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public JdbcReaderNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName) {
		super(databeanClass, fielderClass, router, clientName);
	}

	public JdbcReaderNode(Class<? super D> baseDatabeanClass, Class<D> databeanClass, 
			Class<F> fielderClass,
			DataRouter router, String clientName){
		super(baseDatabeanClass, databeanClass, fielderClass, router, clientName);
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public HibernateClientImp getClient(){
		return (HibernateClientImp)getRouter().getClient(getClientName());
	}
	
	@Override
	public void clearThreadSpecificState(){
		//TODO maybe clear the hibernate session here through the client??
	}

	
	/************************************ MapStorageReader methods ****************************/
	
	public static final int DEFAULT_ITERATE_BATCH_SIZE = 1000;
	
	@Override
	public boolean exists(PK key, Config config) {
		return get(key, config) != null;
	}

	@Override
	public D get(final PK key, final Config config){
		JdbcGetOp<PK,D,F> op = new JdbcGetOp<PK,D,F>(this, "get", ListTool.wrap(key), config);
		return CollectionTool.getFirst(new SessionExecutorImpl<List<D>>(op).call());
	}

	@Override
	public List<D> getAll(final Config config) {
		JdbcGetAllOp<PK,D,F> op = new JdbcGetAllOp<PK,D,F>(this, "getAll", config);
		return new SessionExecutorImpl<List<D>>(op).call();
	}
	
	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config config) {
		JdbcGetOp<PK,D,F> op = new JdbcGetOp<PK,D,F>(this, "getMulti", keys, config);
		return new SessionExecutorImpl<List<D>>(op).call();
	}
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config config) {
		JdbcGetKeysOp<PK,D,F> op = new JdbcGetKeysOp<PK,D,F>(this, "getKeys", keys, config);
		return new SessionExecutorImpl<List<PK>>(op).call();
	}

	
	
	/************************************ IndexedStorageReader methods ****************************/
	
	@Override
	public Long count(final Lookup<PK> lookup, final Config config) {
		JdbcCountOp<PK,D,F> op = new JdbcCountOp<PK,D,F>(this, "count", lookup, config);
		return new SessionExecutorImpl<Long>(op).call();
	}
	
	@Override
	public D lookupUnique(final UniqueKey<PK> uniqueKey, final Config config){
		JdbcLookupUniqueOp<PK,D,F> op = new JdbcLookupUniqueOp<PK,D,F>(this, "lookupUnique", 
				ListTool.wrap(uniqueKey), config);
		List<D> result = new SessionExecutorImpl<List<D>>(op).call();
		if(CollectionTool.size(result)>1){
			throw new DataAccessException("found >1 databeans with unique index key="+uniqueKey);
		}
		return CollectionTool.getFirst(result);
	}

	@Override
	public List<D> lookupMultiUnique(final Collection<? extends UniqueKey<PK>> uniqueKeys, final Config config){
		if(CollectionTool.isEmpty(uniqueKeys)){ return new LinkedList<D>(); }
		JdbcLookupUniqueOp<PK,D,F> op = new JdbcLookupUniqueOp<PK,D,F>(this, "lookupMultiUnique", uniqueKeys,
				config);
		return new SessionExecutorImpl<List<D>>(op).call();
	}
	
	@Override
	//TODO pay attention to wildcardLastField
	public List<D> lookup(final Lookup<PK> lookup, final boolean wildcardLastField, final Config config) {
		JdbcLookupOp<PK,D,F> op = new JdbcLookupOp<PK,D,F>(this, "lookup", ListTool.wrap(lookup), 
				wildcardLastField, config);
		return new SessionExecutorImpl<List<D>>(op).call();
	}
	
	//TODO rename lookupMulti
	@Override
	public List<D> lookup(final Collection<? extends Lookup<PK>> lookups, final Config config) {
		if(CollectionTool.isEmpty(lookups)){ return new LinkedList<D>(); }
		JdbcLookupOp<PK,D,F> op = new JdbcLookupOp<PK,D,F>(this, "lookupMulti", lookups, false, config);
		return new SessionExecutorImpl<List<D>>(op).call();
	}
	
	
	/************************************ SortedStorageReader methods ****************************/

	@Override
	public D getFirst(final Config config) {
		JdbcGetFirstOp<PK,D,F> op = new JdbcGetFirstOp<PK,D,F>(this, "getFirst", config);
		return new SessionExecutorImpl<D>(op).call();
	}

	
	@Override
	public PK getFirstKey(final Config config) {
		JdbcGetFirstKeyOp<PK,D,F> op = new JdbcGetFirstKeyOp<PK,D,F>(this, "getFirstKey", config);
		return new SessionExecutorImpl<PK>(op).call();
	}

	@Override
	public List<D> getWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		return getWithPrefixes(ListTool.wrap(prefix),wildcardLastField,config);
	}

	@Override
	public List<D> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField, 
			final Config config) {
		JdbcGetWithPrefixesOp<PK,D,F> op = new JdbcGetWithPrefixesOp<PK,D,F>(this, "getWithPrefixes", prefixes, 
				wildcardLastField, config);
		return new SessionExecutorImpl<List<D>>(op).call();
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	@Override
	public List<PK> getKeysInRange(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config) {
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		return (List<PK>)getRangeUnchecked(range, true, config);
	}
	

	@Deprecated
	@SuppressWarnings("unchecked")
	@Override
	public List<D> getRange(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config) {
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		return (List<D>)getRangeUnchecked(range, false, config);
	}
	
	
	//this gets ugly because we are dealing with PrimaryKeys/Databeans and Jdbc/Hibernate
	public List<? extends FieldSet<?>> getRangeUnchecked(final Range<PK> range, final boolean keysOnly,
			final Config config){
		String opName = keysOnly ? "getKeysInRange" : "getRange";
		JdbcGetRangeUncheckedOp<PK,D,F> op = new JdbcGetRangeUncheckedOp<PK,D,F>(this, opName, range, keysOnly,
				config);
		return new SessionExecutorImpl<List<? extends FieldSet<?>>>(op).call();
	}

	
	@Override
	public List<D> getPrefixedRange(
			final PK prefix, final boolean wildcardLastField,
			final PK start, final boolean startInclusive, 
			final Config config) {
		JdbcGetPrefixedRangeOp<PK,D,F> op = new JdbcGetPrefixedRangeOp<PK,D,F>(this, "getPrefixedRange", 
				prefix, wildcardLastField, start, startInclusive, config);
		return new SessionExecutorImpl<List<D>>(op).call();
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(
			PK start, boolean startInclusive, 
			PK end, boolean endInclusive, 
			Config config){
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		SortedScanner<PK> scanner = new JdbcPrimaryKeyScanner<PK,D>(this, fieldInfo, range, config);
		return new SortedScannerIterable<PK>(scanner);
	}
	
	@Override
	public SortedScannerIterable<D> scan(
			PK start, boolean startInclusive, 
			PK end, boolean endInclusive, 
			Config config){
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		SortedScanner<D> scanner = new JdbcDatabeanScanner<PK,D>(this, fieldInfo, range, config);
		return new SortedScannerIterable<D>(scanner);
	}
	
	

}