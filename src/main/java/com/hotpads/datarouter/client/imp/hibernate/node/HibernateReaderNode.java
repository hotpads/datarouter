package com.hotpads.datarouter.client.imp.hibernate.node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateCountOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetFirstKeyOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetFirstOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetKeysOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetRangeUncheckedOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetWithPrefixesOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateLookupOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateLookupUniqueOp;
import com.hotpads.datarouter.client.imp.hibernate.scan.HibernateDatabeanScanner;
import com.hotpads.datarouter.client.imp.hibernate.scan.HibernatePrimaryKeyScanner;
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
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.core.DrBatchTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

public class HibernateReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements MapStorageReader<PK,D>,
		SortedStorageReader<PK,D>,
		IndexedStorageReader<PK,D>{
	protected static Logger logger = LoggerFactory.getLogger(HibernateReaderNode.class);
	
	/******************************* constructors ************************************/

	public HibernateReaderNode(NodeParams<PK,D,F> params){
		super(params);
	}
	
	
	/***************************** plumbing methods ***********************************/

	@Override
	public HibernateClientImp getClient(){
		return (HibernateClientImp)getRouter().getClient(getClientName());
	}
	
	/************************************ MapStorageReader methods ****************************/
	
	public static final int 
		DEFAULT_GET_MULTI_BATCH_SIZE = 200,
		DEFAULT_ITERATE_BATCH_SIZE = 1000;
	
	@Override
	public boolean exists(PK key, Config config) {
		return get(key, config) != null;
	}

	@Override
	public D get(final PK key, final Config config){
		String opName = MapStorageReader.OP_get;
		HibernateGetOp<PK,D,F> op = new HibernateGetOp<PK,D,F>(this, DrListTool.wrap(key), config);
		return DrCollectionTool.getFirst(new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call());
	}
	
	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config pConfig){
		String opName = MapStorageReader.OP_getMulti;
		List<D> result = DrListTool.createArrayListWithSize(keys);
		Config config = Config.nullSafe(pConfig);
		int batchSize = config.getIterateBatchSizeOverrideNull(DEFAULT_GET_MULTI_BATCH_SIZE);
		for(List<PK> keyBatch : DrBatchTool.getBatches(keys, batchSize)){
			HibernateGetOp<PK,D,F> op = new HibernateGetOp<PK,D,F>(this, keyBatch, config);
			List<D> resultBatch = new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
			result.addAll(DrCollectionTool.nullSafe(resultBatch));
		}
		return result;
	}
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config pConfig){
		String opName = MapStorageReader.OP_getKeys;
		Config config = Config.nullSafe(pConfig);
		int batchSize = config.getIterateBatchSizeOverrideNull(DEFAULT_GET_MULTI_BATCH_SIZE);
		List<PK> result = DrListTool.createArrayListWithSize(keys);
		for(List<PK> keyBatch : DrBatchTool.getBatches(keys, batchSize)){
			HibernateGetKeysOp<PK,D,F> op = new HibernateGetKeysOp<PK,D,F>(this, keyBatch, config);
			result.addAll(new SessionExecutorImpl<List<PK>>(op, getTraceName(opName)).call());
		}
		return result;
	}

	
	
	/************************************ IndexedStorageReader methods ****************************/
	
	@Override
	public Long count(final Lookup<PK> lookup, final Config config) {
		String opName = IndexedStorageReader.OP_count;
		HibernateCountOp<PK,D,F> op = new HibernateCountOp<PK,D,F>(this, opName, lookup, config);
		return new SessionExecutorImpl<Long>(op, getTraceName(opName)).call();
	}
	
	@Override
	public D lookupUnique(final UniqueKey<PK> uniqueKey, final Config config){
		String opName = IndexedStorageReader.OP_lookupUnique;
		HibernateLookupUniqueOp<PK,D,F> op = new HibernateLookupUniqueOp<PK,D,F>(this, opName, 
				DrListTool.wrap(uniqueKey), config);
		List<D> result = new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
		if(DrCollectionTool.size(result)>1){
			throw new DataAccessException("found >1 databeans with unique index key="+uniqueKey);
		}
		return DrCollectionTool.getFirst(result);
	}

	@Override
	public List<D> lookupMultiUnique(final Collection<? extends UniqueKey<PK>> uniqueKeys, final Config config){
		String opName = IndexedStorageReader.OP_lookupMultiUnique;
		if(DrCollectionTool.isEmpty(uniqueKeys)){ return new LinkedList<D>(); }
		HibernateLookupUniqueOp<PK,D,F> op = new HibernateLookupUniqueOp<PK,D,F>(this, opName, uniqueKeys,
				config);
		return new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
	}
	
	@Override
	//TODO pay attention to wildcardLastField
	public List<D> lookup(final Lookup<PK> lookup, final boolean wildcardLastField, final Config config) {
		String opName = IndexedStorageReader.OP_lookup;
		HibernateLookupOp<PK,D,F> op = new HibernateLookupOp<PK,D,F>(this, opName, DrListTool.wrap(lookup), 
				wildcardLastField, config);
		return new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
	}
	
	//TODO rename lookupMulti
	@Override
	public List<D> lookup(final Collection<? extends Lookup<PK>> lookups, final Config config) {
		String opName = IndexedStorageReader.OP_lookupMulti;
		if(DrCollectionTool.isEmpty(lookups)){ return new LinkedList<D>(); }
		HibernateLookupOp<PK,D,F> op = new HibernateLookupOp<PK,D,F>(this, opName, lookups, false, config);
		return new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
	}
	
	
	/************************************ SortedStorageReader methods ****************************/

	@Override
	public D getFirst(final Config config) {
		String opName = SortedStorageReader.OP_getFirst;
		HibernateGetFirstOp<PK,D,F> op = new HibernateGetFirstOp<PK,D,F>(this, opName, config);
		return new SessionExecutorImpl<D>(op, getTraceName(opName)).call();
	}

	
	@Override
	public PK getFirstKey(final Config config) {
		String opName = SortedStorageReader.OP_getFirstKey;
		HibernateGetFirstKeyOp<PK,D,F> op = new HibernateGetFirstKeyOp<PK,D,F>(this, opName, config);
		return new SessionExecutorImpl<PK>(op, getTraceName(opName)).call();
	}

	@Override
	public List<D> getWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		return getWithPrefixes(DrListTool.wrap(prefix),wildcardLastField,config);
	}

	@Override
	public List<D> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField, 
			final Config config) {
		String opName = SortedStorageReader.OP_getWithPrefixes;
		HibernateGetWithPrefixesOp<PK,D,F> op = new HibernateGetWithPrefixesOp<PK,D,F>(this, opName, prefixes, 
				wildcardLastField, config);
		return new SessionExecutorImpl<List<D>>(op, getTraceName(opName)).call();
	}

	//used by HibernatePrimaryKeyScanner
	@SuppressWarnings("unchecked")
	public List<PK> getKeysInRange(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config) {
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		return (List<PK>)getRangeUnchecked(range, true, config);
	}
	

	//used by HibernateDatabeanScanner
	@SuppressWarnings("unchecked")
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
		String opName = keysOnly ? SortedStorageReader.OP_getKeysInRange : SortedStorageReader.OP_getRange;
		HibernateGetRangeUncheckedOp<PK,D,F> op = new HibernateGetRangeUncheckedOp<PK,D,F>(this, opName, range, keysOnly,
				config);
		return new SessionExecutorImpl<List<? extends FieldSet<?>>>(op, getTraceName(opName)).call();
	}

	@Override
	public SortedScannerIterable<PK> scanKeys(Range<PK> pRange, Config config){
		Range<PK> range = Range.nullSafe(pRange);
		SortedScanner<PK> scanner = new HibernatePrimaryKeyScanner<PK,D>(this, fieldInfo, range, config);
		return new SortedScannerIterable<PK>(scanner);
	}
	
	@Override
	public SortedScannerIterable<D> scan(Range<PK> pRange, Config config){
		Range<PK> range = Range.nullSafe(pRange);
		SortedScanner<D> scanner = new HibernateDatabeanScanner<PK,D>(this, fieldInfo, range, config);
		return new SortedScannerIterable<D>(scanner);
	}
	
	
	/*********************** helper ******************************/
	
	protected String getTraceName(String opName){
		return getName() + " " + opName;
	}
	
	
	/********************************* hibernate helpers ***********************************************/
	
	//shouldn't need this for innodb.  not sure if it hurts or not though.  see Handler_read_rnd_next (innodb may sort anyway?)
	public void addPrimaryKeyOrderToCriteria(Criteria criteria){
		for(Field<?> field : fieldInfo.getPrefixedPrimaryKeyFields()){
			criteria.addOrder(Order.asc(field.getPrefixedName()));
		}
	}
	
	
	public Criteria getCriteriaForConfig(Config config, Session session){
		final String entityName = this.getPackagedTableName();
		Criteria criteria = session.createCriteria(entityName);
		
		if(config == null){
			return criteria;
		}
		//need clearer spec on how to handle limit and iterateBatchSize
		if(config.getIterateBatchSize()!=null){
			config.setLimit(config.getIterateBatchSize());
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

	
	public Conjunction getPrefixConjunction(boolean usePrefixedFieldNames,
			Key<PK> prefix, final boolean wildcardLastField){
		int numNonNullFields = FieldSetTool.getNumNonNullLeadingFields(prefix);
		if(numNonNullFields==0){ return null; }
		Conjunction conjunction = Restrictions.conjunction();
		int numFullFieldsFinished = 0;
		List<Field<?>> fields = prefix.getFields();
		if(usePrefixedFieldNames){
			fields = FieldTool.prependPrefixes(fieldInfo.getKeyFieldName(), fields);
		}
		for(Field<?> field : fields){
			if(numFullFieldsFinished >= numNonNullFields){ break; }
			if(field.getValue()==null) {
				throw new DataAccessException("Prefix query on "+
						prefix.getClass()+" cannot contain intermediate nulls.");
			}
			boolean lastNonNullField = (numFullFieldsFinished == numNonNullFields-1);
			boolean stringField = !(field instanceof BasePrimitiveField<?>);
			boolean canDoPrefixMatchOnField = wildcardLastField && lastNonNullField && stringField;
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
