package com.hotpads.datarouter.client.imp.hibernate.node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateCountOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetAllOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetFirstKeyOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetFirstOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetKeysOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetPrefixedRangeOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetRangeUncheckedOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateGetWithPrefixesOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateLookupOp;
import com.hotpads.datarouter.client.imp.hibernate.op.read.HibernateLookupUniqueOp;
import com.hotpads.datarouter.client.imp.hibernate.scan.HibernateDatabeanScanner;
import com.hotpads.datarouter.client.imp.hibernate.scan.HibernatePrimaryKeyScanner;
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
import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.BatchTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

public class HibernateReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
extends BasePhysicalNode<PK,D,F>
implements MapStorageReader<PK,D>,
		SortedStorageReader<PK,D>,
		IndexedStorageReader<PK,D>{
	
	protected Logger logger = Logger.getLogger(getClass());
	
	/******************************* constructors ************************************/

	public HibernateReaderNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName, 
			String physicalName, String qualifiedPhysicalName) {
		super(databeanClass, fielderClass, router, clientName, physicalName, qualifiedPhysicalName);
	}
	
	public HibernateReaderNode(Class<D> databeanClass, Class<F> fielderClass,
			DataRouter router, String clientName) {
		super(databeanClass, fielderClass, router, clientName);
	}

	public HibernateReaderNode(Class<? super D> baseDatabeanClass, Class<D> databeanClass, 
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
	
	public static final int 
		DEFAULT_GET_MULTI_BATCH_SIZE = 200,
		DEFAULT_ITERATE_BATCH_SIZE = 1000;
	
	@Override
	public boolean exists(PK key, Config config) {
		return get(key, config) != null;
	}

	@Override
	public D get(final PK key, final Config config){
		HibernateGetOp<PK,D,F> op = new HibernateGetOp<PK,D,F>(this, "get", ListTool.wrap(key), config);
		return CollectionTool.getFirst(new SessionExecutorImpl<List<D>>(op).call());
	}

	@Override
	public List<D> getAll(final Config config){
		HibernateGetAllOp<PK,D,F> op = new HibernateGetAllOp<PK,D,F>(this, "getAll", config);
		return new SessionExecutorImpl<List<D>>(op).call();
	}
	
	@Override
	public List<D> getMulti(final Collection<PK> keys, final Config pConfig){
		DRCounters.incSuffixClientNode(ClientType.hibernate, "getMulti", getClientName(), getName());
		List<D> result = ListTool.createArrayListWithSize(keys);
		Config config = Config.nullSafe(pConfig);
		int batchSize = config.getIterateBatchSizeOverrideNull(DEFAULT_GET_MULTI_BATCH_SIZE);
		for(List<PK> keyBatch : BatchTool.getBatches(keys, batchSize)){
			HibernateGetOp<PK,D,F> op = new HibernateGetOp<PK,D,F>(this, "getMultiBatch", keyBatch, config);
			List<D> resultBatch = new SessionExecutorImpl<List<D>>(op).call();
			result.addAll(CollectionTool.nullSafe(resultBatch));
		}
		return result;
	}
	
	@Override
	public List<PK> getKeys(final Collection<PK> keys, final Config pConfig){
		DRCounters.incSuffixClientNode(ClientType.hibernate, "getKeys", getClientName(), getName());
		Config config = Config.nullSafe(pConfig);
		int batchSize = config.getIterateBatchSizeOverrideNull(DEFAULT_GET_MULTI_BATCH_SIZE);
		List<PK> result = ListTool.createArrayListWithSize(keys);
		for(List<PK> keyBatch : BatchTool.getBatches(keys, batchSize)){
			HibernateGetKeysOp<PK,D,F> op = new HibernateGetKeysOp<PK,D,F>(this, "getKeysBatch", keyBatch, config);
			result.addAll(new SessionExecutorImpl<List<PK>>(op).call());
		}
		return result;
	}

	
	
	/************************************ IndexedStorageReader methods ****************************/
	
	@Override
	public Long count(final Lookup<PK> lookup, final Config config) {
		HibernateCountOp<PK,D,F> op = new HibernateCountOp<PK,D,F>(this, "count", lookup, config);
		return new SessionExecutorImpl<Long>(op).call();
	}
	
	@Override
	public D lookupUnique(final UniqueKey<PK> uniqueKey, final Config config){
		HibernateLookupUniqueOp<PK,D,F> op = new HibernateLookupUniqueOp<PK,D,F>(this, "lookupUnique", 
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
		HibernateLookupUniqueOp<PK,D,F> op = new HibernateLookupUniqueOp<PK,D,F>(this, "lookupMultiUnique", uniqueKeys,
				config);
		return new SessionExecutorImpl<List<D>>(op).call();
	}
	
	@Override
	//TODO pay attention to wildcardLastField
	public List<D> lookup(final Lookup<PK> lookup, final boolean wildcardLastField, final Config config) {
		HibernateLookupOp<PK,D,F> op = new HibernateLookupOp<PK,D,F>(this, "lookup", ListTool.wrap(lookup), 
				wildcardLastField, config);
		return new SessionExecutorImpl<List<D>>(op).call();
	}
	
	//TODO rename lookupMulti
	@Override
	public List<D> lookup(final Collection<? extends Lookup<PK>> lookups, final Config config) {
		if(CollectionTool.isEmpty(lookups)){ return new LinkedList<D>(); }
		HibernateLookupOp<PK,D,F> op = new HibernateLookupOp<PK,D,F>(this, "lookupMulti", lookups, false, config);
		return new SessionExecutorImpl<List<D>>(op).call();
	}
	
	
	/************************************ SortedStorageReader methods ****************************/

	@Override
	public D getFirst(final Config config) {
		HibernateGetFirstOp<PK,D,F> op = new HibernateGetFirstOp<PK,D,F>(this, "getFirst", config);
		return new SessionExecutorImpl<D>(op).call();
	}

	
	@Override
	public PK getFirstKey(final Config config) {
		HibernateGetFirstKeyOp<PK,D,F> op = new HibernateGetFirstKeyOp<PK,D,F>(this, "getFirstKey", config);
		return new SessionExecutorImpl<PK>(op).call();
	}

	@Override
	public List<D> getWithPrefix(final PK prefix, final boolean wildcardLastField, final Config config) {
		return getWithPrefixes(ListTool.wrap(prefix),wildcardLastField,config);
	}

	@Override
	public List<D> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField, 
			final Config config) {
		HibernateGetWithPrefixesOp<PK,D,F> op = new HibernateGetWithPrefixesOp<PK,D,F>(this, "getWithPrefixes", prefixes, 
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
		HibernateGetRangeUncheckedOp<PK,D,F> op = new HibernateGetRangeUncheckedOp<PK,D,F>(this, opName, range, keysOnly,
				config);
		return new SessionExecutorImpl<List<? extends FieldSet<?>>>(op).call();
	}

	
	@Override
	public List<D> getPrefixedRange(
			final PK prefix, final boolean wildcardLastField,
			final PK start, final boolean startInclusive, 
			final Config config) {
		HibernateGetPrefixedRangeOp<PK,D,F> op = new HibernateGetPrefixedRangeOp<PK,D,F>(this, "getPrefixedRange", 
				prefix, wildcardLastField, start, startInclusive, config);
		return new SessionExecutorImpl<List<D>>(op).call();
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(
			PK start, boolean startInclusive, 
			PK end, boolean endInclusive, 
			Config config){
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		SortedScanner<PK> scanner = new HibernatePrimaryKeyScanner<PK,D>(this, fieldInfo, range, config);
		return new SortedScannerIterable<PK>(scanner);
	}
	
	@Override
	public SortedScannerIterable<D> scan(
			PK start, boolean startInclusive, 
			PK end, boolean endInclusive, 
			Config config){
		Range<PK> range = Range.create(start, startInclusive, end, endInclusive);
		SortedScanner<D> scanner = new HibernateDatabeanScanner<PK,D>(this, fieldInfo, range, config);
		return new SortedScannerIterable<D>(scanner);
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
		int numNonNullFields = FieldSetTool.getNumNonNullFields(prefix);
		if(numNonNullFields==0){ return null; }
		Conjunction conjunction = Restrictions.conjunction();
		int numFullFieldsFinished = 0;
		List<Field<?>> fields = prefix.getFields();
		if(usePrefixedFieldNames){
			fields = FieldTool.prependPrefixes(fieldInfo.getKeyFieldName(), fields);
		}
		for(Field<?> field : fields){
			if(numFullFieldsFinished >= numNonNullFields) break;
			if(field.getValue()==null) {
				throw new DataAccessException("Prefix query on "+
						prefix.getClass()+" cannot contain intermediate nulls.");
			}
			boolean lastNonNullField = (numFullFieldsFinished == numNonNullFields-1);
			boolean stringField = !(field instanceof BasePrimitiveField<?>);
			boolean canDoPrefixMatchOnField = wildcardLastField && lastNonNullField && stringField;
//			String fieldNameWithPrefixIfNecessary = usePrefixedFieldNames
//					? field.getPrefixedName() : field.getName();
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