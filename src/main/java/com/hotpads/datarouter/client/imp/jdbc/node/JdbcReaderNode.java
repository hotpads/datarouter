package com.hotpads.datarouter.client.imp.jdbc.node;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.jdbc.JdbcClientImp;
import com.hotpads.datarouter.client.imp.jdbc.scan.JdbcDatabeanScanner;
import com.hotpads.datarouter.client.imp.jdbc.scan.JdbcPrimaryKeyScanner;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
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
	private static final Logger logger = LoggerFactory.getLogger(JdbcReaderNode.class);
	
	private final JdbcReaderOps<PK,D,F> jdbcReaderOps;
	
	/******************************* constructors ************************************/

	public JdbcReaderNode(NodeParams<PK,D,F> params){
		super(params);
		this.jdbcReaderOps = new JdbcReaderOps<>(this);
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public JdbcClientImp getClient(){
		return (JdbcClientImp)getRouter().getClient(getClientName());
	}
	
	/************************************ MapStorageReader methods ****************************/
	
	public static final int DEFAULT_ITERATE_BATCH_SIZE = 1000;
	
	@Override
	public boolean exists(PK key, Config config) {
		return null != jdbcReaderOps.get(key, config);
	}

	@Override
	public D get(final PK key, final Config config){
		return DrCollectionTool.getFirst(jdbcReaderOps.getMulti(DrListTool.wrap(key), config));
	}
	
	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config config) {
		return jdbcReaderOps.getMulti(keys, config);
	}
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config config) {
		return jdbcReaderOps.getKeys(keys, config);
	}

	
	/************************************ IndexedStorageReader methods ****************************/
	
	@Override
	public Long count(final Lookup<PK> lookup, final Config config) {
		return jdbcReaderOps.count(lookup, config);
	}
	
	@Override
	public D lookupUnique(final UniqueKey<PK> uniqueKey, final Config config){
		return jdbcReaderOps.lookupUnique(uniqueKey, config);
	}

	@Override
	public List<D> lookupMultiUnique(final Collection<? extends UniqueKey<PK>> uniqueKeys, final Config config){
		return jdbcReaderOps.lookupMultiUnique(uniqueKeys, config);
	}
	
	@Override
	//TODO pay attention to wildcardLastField
	public List<D> lookup(final Lookup<PK> lookup, final boolean wildcardLastField, final Config config) {
		return jdbcReaderOps.lookup(lookup, wildcardLastField, config);
	}
	
	//TODO rename lookupMulti
	@Override
	public List<D> lookup(final Collection<? extends Lookup<PK>> lookups, final Config config) {
		return jdbcReaderOps.lookup(lookups, config);
	}

	//TODO add to IndexedStorageReader interface
	//@Override
	public <PKLookup extends BaseLookup<PK>> SortedScannerIterable<PKLookup> scanIndex(Class<PKLookup> indexClass){
		return jdbcReaderOps.scanIndex(indexClass);
	}
	
	/************************************ SortedStorageReader methods ****************************/

	@Override
	public D getFirst(final Config config) {
		return jdbcReaderOps.getFirst(config);
	}
	
	@Override
	public PK getFirstKey(final Config config) {
		return jdbcReaderOps.getFirstKey(config);
	}

	@Override
	public List<D> getWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		return getWithPrefixes(DrListTool.wrap(prefix), wildcardLastField, config);
	}

	@Override
	public List<D> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField, 
			final Config config) {
		return jdbcReaderOps.getWithPrefixes(prefixes, wildcardLastField, config);
	}

	@Override
	public SortedScannerIterable<PK> scanKeys(Range<PK> pRange, Config config){
		Range<PK> range = Range.nullSafe(pRange);
		SortedScanner<PK> scanner = new JdbcPrimaryKeyScanner<PK,D>(jdbcReaderOps, fieldInfo, range, config);
		return new SortedScannerIterable<PK>(scanner);
	}
	
	@Override
	public SortedScannerIterable<D> scan(Range<PK> pRange, Config config){
		Range<PK> range = Range.nullSafe(pRange);
		SortedScanner<D> scanner = new JdbcDatabeanScanner<PK,D>(jdbcReaderOps, fieldInfo, range, config);
		return new SortedScannerIterable<D>(scanner);
	}
	
	
	/*********************** helper ******************************/
	
	protected String getTraceName(String opName){
		return jdbcReaderOps.getTraceName(opName);
	}
	
}
