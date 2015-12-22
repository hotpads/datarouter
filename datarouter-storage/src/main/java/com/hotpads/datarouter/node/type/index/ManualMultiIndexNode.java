package com.hotpads.datarouter.node.type.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.op.scan.ManagedIndexDatabeanScanner;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntryTool;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SingleUseScannerIterable;

public class ManualMultiIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends MultiIndexEntry<IK,IE,PK,D>>

implements MultiIndexNode<PK, D, IK, IE>{

	protected MapStorage<PK,D> mainNode;
	protected SortedMapStorage<IK,IE> indexNode;

	public ManualMultiIndexNode(MapStorage<PK,D> mainNode, SortedMapStorage<IK,IE> indexNode){
		this.mainNode = mainNode;
		this.indexNode = indexNode;
	}

	//TODO should i be passing config options around blindly?
	//TODO need to watch out for offset/limit


	/********************* IndexReader ******************************/

	@Override
	public List<D> lookupMulti(IK indexKey, Config config){
		if(indexKey==null){
			return new LinkedList<>();
		}
		//hard-coding startInclusive to true because it will usually be true on the first call,
		// but subsequent calls may want false, so consider adding as method param
		Range<IK> indexKeyRange = new Range<>(indexKey, true, indexKey, true);
		List<IE> indexEntries = DrListTool.createArrayList(indexNode.scan(indexKeyRange, null));
		List<PK> primaryKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		List<D> databeans = mainNode.getMulti(primaryKeys, config);
		return databeans;
	}


	@Override
	public List<D> lookupMultiMulti(Collection<IK> indexKeys, Config config){
		if(DrCollectionTool.isEmpty(indexKeys)){
			return new LinkedList<>();
		}
		List<IE> allIndexEntries = new ArrayList<>();
		for(IK indexKey : indexKeys){
			Range<IK> indexKeyRange = new Range<>(indexKey, true, indexKey, true);
			List<IE> indexEntries = DrListTool.createArrayList(indexNode.scan(indexKeyRange, null));
			allIndexEntries.addAll(DrCollectionTool.nullSafe(indexEntries));
		}
		List<PK> primaryKeys = IndexEntryTool.getPrimaryKeys(allIndexEntries);
		List<D> databeans = mainNode.getMulti(primaryKeys, config);
		return databeans;
	}

	@Override
	public Iterable<IE> scan(Range<IK> range, Config config){
		return indexNode.scan(range, config);
	}

	@Override
	public SingleUseScannerIterable<D> scanDatabeans(Range<IK> range, Config config){
		return new SingleUseScannerIterable<>(new ManagedIndexDatabeanScanner<>(mainNode, scan(range, config), config));
	}

	@Override
	public Iterable<IK> scanKeys(Range<IK> range, Config config){
		return indexNode.scanKeys(range, config);
	}
}
