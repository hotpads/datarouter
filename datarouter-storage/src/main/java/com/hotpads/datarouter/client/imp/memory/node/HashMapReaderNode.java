package com.hotpads.datarouter.client.imp.memory.node;

import java.util.ArrayList;
import java.util.Collection;
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
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader;
import com.hotpads.datarouter.node.type.physical.base.BasePhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.KeyRangeTool;
import com.hotpads.util.core.collections.Range;

public class HashMapReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements SortedMapStorageReader<PK,D>{

	protected Map<UniqueKey<PK>,D> backingMap = new ConcurrentSkipListMap<>();

	public HashMapReaderNode(NodeParams<PK,D,F> params){
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

	/****************************** SortedMapStorageReader methods ****************************/

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
		return ranges.stream().map(range -> filter(range, false, config)).flatMap(Set::stream).collect(Collectors.toList());
	}

	@Override
	public Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		return DatabeanTool.getKeys(scanMulti(ranges, config));
	}

	private SortedSet<D> filter(Range<PK> range, boolean wildcardLastField, Config config){
		SortedSet<D> results = new TreeSet<>();
		int offset = 0;
		entryLoop : for(Entry<UniqueKey<PK>,D> entry : backingMap.entrySet()){
			if(config != null && config.getOffset() != null && offset < config.getOffset()){
				offset++;
				continue;
			}
			List<Field<?>> keyFields = entry.getValue().getKey().getFields();
			if(range.getStart() != null){
				int nonNullLeadingStartFields = FieldSetTool.getNumNonNullLeadingFields(range.getStart());
				List<Field<?>> startFields = range.getStart().getFields();
				for(int i = 0 ; i < nonNullLeadingStartFields ; i++){
					if(i == nonNullLeadingStartFields - 1
							&& wildcardLastField
							&& startFields.get(i) instanceof StringField
							&& keyFields.get(i).getValueString().startsWith(startFields.get(i).getValueString())){
						break;
					}
					@SuppressWarnings({"unchecked", "rawtypes"})
					int diff = startFields.get(i).compareTo((Field)keyFields.get(i));
					if(diff > 0 || diff == 0 && !range.getStartInclusive() && i == nonNullLeadingStartFields - 1){
						continue entryLoop;
					}
				}
			}
			if(range.getEnd() != null){
				int nonNullLeadingEndFields = FieldSetTool.getNumNonNullLeadingFields(range.getEnd());
				List<Field<?>> endFields = range.getEnd().getFields();
				for(int j = 0 ; j < nonNullLeadingEndFields ; j++){
					if(j == nonNullLeadingEndFields - 1
							&& wildcardLastField
							&& endFields.get(j) instanceof StringField
							&& keyFields.get(j).getValueString().startsWith(endFields.get(j).getValueString())){
						break;
					}
					@SuppressWarnings({"unchecked", "rawtypes"})
					int diff = endFields.get(j).compareTo((Field)keyFields.get(j));
					if(diff < 0 || diff == 0 && !range.getEndInclusive() && j == nonNullLeadingEndFields - 1){
						continue entryLoop;
					}
				}
			}
			results.add(entry.getValue());
			if(config != null && config.getLimit() != null && results.size() == config.getLimit()){
				break;
			}
		}
		return results;
	}

	/*********************** stats ********************************/

	public int getSize(){
		return backingMap.size();
	}
}
