package com.hotpads.datarouter.client.imp.jdbc.node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.jdbc.JdbcClientImp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcCountOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetAllOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetFirstKeyOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetFirstOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetKeysOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetPrefixedRangeOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetPrimaryKeyRangeOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetRangeOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetWithPrefixesOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcLookupOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcLookupUniqueOp;
import com.hotpads.datarouter.client.imp.jdbc.scan.JdbcDatabeanScanner;
import com.hotpads.datarouter.client.imp.jdbc.scan.JdbcIndexScanner;
import com.hotpads.datarouter.client.imp.jdbc.scan.JdbcPrimaryKeyScanner;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
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
	private static Logger logger = Logger.getLogger(JdbcReaderNode.class);
	
	/******************************* constructors ************************************/

	public JdbcReaderNode(NodeParams<PK,D,F> params){
		super(params);
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public JdbcClientImp getClient(){
		return (JdbcClientImp)getRouter().getClient(getClientName());
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
		String opName = MapStorageReader.OP_get;
		JdbcGetOp<PK,D,F> op = new JdbcGetOp<PK,D,F>(this, opName, ListTool.wrap(key), config);
		List<D> databeans = new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();//should only be one
		return CollectionTool.getFirst(databeans);
	}
	
	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config config) {
		String opName = MapStorageReader.OP_getMulti;
		JdbcGetOp<PK,D,F> op = new JdbcGetOp<PK,D,F>(this, opName, keys, config);
		return new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
	}
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config config) {
		String opName = MapStorageReader.OP_getKeys;
		JdbcGetKeysOp<PK,D,F> op = new JdbcGetKeysOp<PK,D,F>(this, opName, keys, config);
		return new SessionExecutorImpl<List<PK>>(op, getTraceName(opName)).call();
	}

	
	
	/************************************ IndexedStorageReader methods ****************************/
	
	@Override
	public Long count(final Lookup<PK> lookup, final Config config) {
		String opName = IndexedStorageReader.OP_count;
		JdbcCountOp<PK,D,F> op = new JdbcCountOp<PK,D,F>(this, opName, lookup, config);
		return new SessionExecutorImpl<Long>(op, getTraceName(opName)).call();
	}
	
	@Override
	public D lookupUnique(final UniqueKey<PK> uniqueKey, final Config config){
		String opName = IndexedStorageReader.OP_lookupUnique;
		JdbcLookupUniqueOp<PK,D,F> op = new JdbcLookupUniqueOp<PK,D,F>(this, opName, 
				ListTool.wrap(uniqueKey), config);
		List<D> result = new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
		if(CollectionTool.size(result)>1){
			throw new DataAccessException("found >1 databeans with unique index key="+uniqueKey);
		}
		return CollectionTool.getFirst(result);
	}

	@Override
	public List<D> lookupMultiUnique(final Collection<? extends UniqueKey<PK>> uniqueKeys, final Config config){
		String opName = IndexedStorageReader.OP_lookupMultiUnique;
		if(CollectionTool.isEmpty(uniqueKeys)){ return new LinkedList<D>(); }
		JdbcLookupUniqueOp<PK,D,F> op = new JdbcLookupUniqueOp<PK,D,F>(this, opName, uniqueKeys,
				config);
		return new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
	}
	
	@Override
	//TODO pay attention to wildcardLastField
	public List<D> lookup(final Lookup<PK> lookup, final boolean wildcardLastField, final Config config) {
		String opName = IndexedStorageReader.OP_lookup;
		JdbcLookupOp<PK,D,F> op = new JdbcLookupOp<PK,D,F>(this, opName, ListTool.wrap(lookup), 
				wildcardLastField, config);
		return new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
	}
	
	//TODO rename lookupMulti
	@Override
	public List<D> lookup(final Collection<? extends Lookup<PK>> lookups, final Config config) {
		String opName = IndexedStorageReader.OP_lookupMulti;
		if(CollectionTool.isEmpty(lookups)){ return new LinkedList<D>(); }
		JdbcLookupOp<PK,D,F> op = new JdbcLookupOp<PK,D,F>(this, opName, lookups, false, config);
		return new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
	}
	
	
	/************************************ SortedStorageReader methods ****************************/

	@Override
	public D getFirst(final Config config) {
		String opName = SortedStorageReader.OP_getFirst;
		JdbcGetFirstOp<PK,D,F> op = new JdbcGetFirstOp<PK,D,F>(this, opName, config);
		return new SessionExecutorImpl<D>(op, getTraceName(opName)).call();
	}

	
	@Override
	public PK getFirstKey(final Config config) {
		String opName = SortedStorageReader.OP_getFirstKey;
		JdbcGetFirstKeyOp<PK,D,F> op = new JdbcGetFirstKeyOp<PK,D,F>(this, opName, config);
		return new SessionExecutorImpl<PK>(op, getTraceName(opName)).call();
	}

	@Override
	public List<D> getWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		return getWithPrefixes(ListTool.wrap(prefix),wildcardLastField,config);
	}

	@Override
	public List<D> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField, 
			final Config config) {
		String opName = SortedStorageReader.OP_getWithPrefixes;
		JdbcGetWithPrefixesOp<PK,D,F> op = new JdbcGetWithPrefixesOp<PK,D,F>(this, opName, prefixes, 
				wildcardLastField, config);
		return new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
	}

	@Deprecated
	@Override
	public List<PK> getKeysInRange(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config) {
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		String opName = SortedStorageReader.OP_getKeysInRange;
		JdbcGetPrimaryKeyRangeOp<PK,D,F> op = new JdbcGetPrimaryKeyRangeOp<PK,D,F>(this, opName, range, config);
		return new SessionExecutorImpl<List<PK>>(op, getTraceName(opName)).call();
	}
	

	@Deprecated
	@Override
	public List<D> getRange(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config) {
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		String opName = SortedStorageReader.OP_getRange;
		JdbcGetRangeOp<PK,D,F> op = new JdbcGetRangeOp<PK,D,F>(this, opName, range, config);
		return new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
	}
	
	
	@Override
	public List<D> getPrefixedRange(
			final PK prefix, final boolean wildcardLastField,
			final PK start, final boolean startInclusive, 
			final Config config) {
		String opName = SortedStorageReader.OP_getPrefixedRange;
		JdbcGetPrefixedRangeOp<PK,D,F> op = new JdbcGetPrefixedRangeOp<PK,D,F>(this, opName, 
				prefix, wildcardLastField, start, startInclusive, config);
		return new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(Range<PK> pRange, Config config){
		Range<PK> range = Range.nullSafe(pRange);
		SortedScanner<PK> scanner = new JdbcPrimaryKeyScanner<PK,D>(this, fieldInfo, range, config);
		return new SortedScannerIterable<PK>(scanner);
	}
	
	@Override
	public SortedScannerIterable<D> scan(Range<PK> pRange, Config config){
		Range<PK> range = Range.nullSafe(pRange);
		SortedScanner<D> scanner = new JdbcDatabeanScanner<PK,D>(this, fieldInfo, range, config);
		return new SortedScannerIterable<D>(scanner);
	}
	
	public <L extends Lookup<PK>> SortedScannerIterable<D> scanIndex(Class<L> indexClass, boolean retreiveAllFields){
		SortedScanner<D> scanner = new JdbcIndexScanner<PK,D,F,L>(this, indexClass, retreiveAllFields);
		return new SortedScannerIterable<D>(scanner);
	}
	
	
	/*********************** helper ******************************/
	
	protected String getTraceName(String opName){
		return getName() + " " + opName;
	}

}
