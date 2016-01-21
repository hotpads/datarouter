package com.hotpads.datarouter.client.imp.memory.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import com.hotpads.datarouter.client.imp.memory.MemoryClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.index.ManagedNodesHolder;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.KeyRangeTool;
import com.hotpads.util.core.collections.Range;

public class MemoryReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements IndexedSortedMapStorageReader<PK,D>{

	protected final Map<UniqueKey<PK>,D> backingMap = new ConcurrentSkipListMap<>();
	private final ManagedNodesHolder<PK,D> managedNodesHolder = new ManagedNodesHolder<>();

	public MemoryReaderNode(NodeParams<PK,D,F> params){
		super(params);
	}

	@Override
	public MemoryClient getClient(){
		return (MemoryClient)getRouter().getClient(getClientId().getName());
	}

	@Override
	public Node<PK,D> getMaster() {
		return null;
	}

	@Override
	public List<Node<PK,D>> getChildNodes(){
		return new ArrayList<>();
	}

	/************************************ MapStorageReader methods ****************************/

	@Override
	public boolean exists(PK key, Config config) {
		return backingMap.containsKey(key);
	}


	@Override
	public D get(final PK key, Config config) {
		return backingMap.get(key);
	}


	@Override
	public List<D> getMulti(final Collection<PK> keys, Config config) {
		List<D> result = new LinkedList<>();
		for(Key<PK> key : DrCollectionTool.nullSafe(keys)){
			D value = backingMap.get(key);
			if(value != null){
				result.add(value);
			}
		}
		return result;
	}


	@Override
	public List<PK> getKeys(final Collection<PK> keys, Config config) {
		List<PK> result = new LinkedList<>();
		for(Key<PK> key : DrCollectionTool.nullSafe(keys)){
			D value = backingMap.get(key);
			if(value != null){
				result.add(value.getKey());
			}
		}
		return result;
	}

	/********************************* SortedStorageReader methods ****************************/

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		return new ArrayList<>(filter(KeyRangeTool.forPrefix(prefix), wildcardLastField, config));
	}

	@Override
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config){
		return prefixes.stream()
				.map(prefix -> getWithPrefix(prefix, wildcardLastField, config))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		return ranges.stream()
				.map(range -> filter(range, false, config))
				.flatMap(Set::stream)
				.collect(Collectors.toList());
	}

	@Override
	public Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		return DatabeanTool.getKeys(scanMulti(ranges, config));
	}

	private SortedSet<D> filter(Range<? extends FieldSet<?>> range, boolean wildcardLastField, Config config){
		range = Range.nullSafe(range);
		int nonNullLeadingStartFields = 0;
		int nonNullLeadingEndFields = 0;
		List<Field<?>> startFields = null;
		List<Field<?>> endFields = null;
		if(range.getStart() != null){
			nonNullLeadingStartFields = FieldSetTool.getNumNonNullLeadingFields(range.getStart());
			startFields = range.getStart().getFields();
		}
		if(range.getEnd() != null){
			nonNullLeadingEndFields = FieldSetTool.getNumNonNullLeadingFields(range.getEnd());
			endFields = range.getEnd().getFields();
		}
		SortedSet<D> results = new TreeSet<>();
		int offset = 0;
		entryLoop : for(Entry<UniqueKey<PK>,D> entry : backingMap.entrySet()){
			if(config != null && config.getOffset() != null && offset < config.getOffset()){
				offset++;
				continue;
			}
			Map<String,Field<?>> entryFieldsByName = FieldSetTool.generateFieldMap(getFieldInfo().getFieldsWithValues(
					entry.getValue()));
			for(int i = 0 ; i < nonNullLeadingStartFields ; i++){
				Field<?> keyField = entryFieldsByName.get(startFields.get(i).getKey().getName());
				if(i == nonNullLeadingStartFields - 1
						&& wildcardLastField
						&& startFields.get(i) instanceof StringField
						&& keyField.getValueString().startsWith(startFields.get(i).getValueString())){
					break;
				}
				@SuppressWarnings({"unchecked", "rawtypes"})
				int diff = startFields.get(i).compareTo((Field)keyField);
				if(diff > 0 || diff == 0 && !range.getStartInclusive() && i == nonNullLeadingStartFields - 1){
					continue entryLoop;
				}
			}
			for(int j = 0 ; j < nonNullLeadingEndFields ; j++){
				Field<?> keyField = entryFieldsByName.get(endFields.get(j).getKey().getName());
				if(j == nonNullLeadingEndFields - 1
						&& wildcardLastField
						&& endFields.get(j) instanceof StringField
						&& keyField.getValueString().startsWith(endFields.get(j).getValueString())){
					break;
				}
				@SuppressWarnings({"unchecked", "rawtypes"})
				int diff = endFields.get(j).compareTo((Field)keyField);
				if(diff < 0 || diff == 0 && !range.getEndInclusive() && j == nonNullLeadingEndFields - 1){
					continue entryLoop;
				}
			}
			results.add(entry.getValue());
			if(config != null && config.getLimit() != null && results.size() == config.getLimit()){
				break;
			}
		}
		return results;
	}

	/****************************** IndexedStorageReader methods ****************************/

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		return DrCollectionTool.getFirst(getBySecondaryIndex(Collections.singleton(uniqueKey), true));
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		return getBySecondaryIndex(uniqueKeys, true);
	}

	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config){
		return getBySecondaryIndex(Collections.singleton(lookup), false);
	}

	@Override
	public List<D> lookupMulti(Collection<? extends Lookup<PK>> lookups, Config config){
		return getBySecondaryIndex(lookups, false);
	}

	private List<D> getBySecondaryIndex(Collection<? extends FieldSet<?>> lookups, boolean unique){
		List<List<Field<?>>> lookupsFields = lookups.stream()
				.map(FieldSet::getFields)
				.collect(Collectors.toList());
		List<D> results = new ArrayList<>();
		for(Entry<UniqueKey<PK>,D> entry : backingMap.entrySet()){
			Map<String,Field<?>> entryFieldsByName = FieldSetTool.generateFieldMap(getFieldInfo()
					.getFieldsWithValues(entry.getValue()));
			Iterator<List<Field<?>>> lookupsFieldsIterator = lookupsFields.iterator();
			lookups : while(lookupsFieldsIterator.hasNext()){
				for(Field<?> lookupField : lookupsFieldsIterator.next()){
					Field<?> entryField = entryFieldsByName.get(lookupField.getKey().getName());
					if(lookupField.getValue() == null){
						break;
					}
					if(!lookupField.getValue().equals(entryField.getValue())){
						continue lookups;
					}
				}
				results.add(entry.getValue());
				if(unique){
					lookupsFieldsIterator.remove();
				}
			}
		}
		return results;
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return getBySecondaryIndex(keys, false).stream()
				.map(indexEntryFieldInfo.getSampleDatabean()::createFromDatabean)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>> List<D> getMultiByIndex(Collection<IK> keys, Config config){
		return getBySecondaryIndex(keys, false);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Iterable<IE> scanIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range, Config config){
		return filter(range, false, config).stream()
				.map(indexEntryFieldInfo.getSampleDatabean()::createFromDatabean)
				.flatMap(Collection::stream)
				.sorted((indexEntry1, indexEntry2) -> indexEntry1.getKey().compareTo(indexEntry2.getKey()))
				.collect(Collectors.toList());
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Iterable<IK> scanIndexKeys(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		return DatabeanTool.getKeys(scanIndex(indexEntryFieldInfo, range, config));
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			N extends ManagedNode<PK,D,IK,IE,IF>> N registerManaged(N managedNode){
		return managedNodesHolder.registerManagedNode(managedNode);
	}

	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return managedNodesHolder.getManagedNodes();
	}

	/*********************** stats ********************************/

	public int getSize(){
		return backingMap.size();
	}

}
