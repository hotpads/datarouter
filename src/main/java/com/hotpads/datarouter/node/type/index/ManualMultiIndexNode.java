package com.hotpads.datarouter.node.type.index;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.compound.readwrite.CompoundMapRWStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.op.scan.ManagedIndexDatabeanScanner;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntryTool;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class ManualMultiIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends MultiIndexEntry<IK,IE,PK,D>>

implements MultiIndexNode<PK, D, IK, IE>
//		,IndexWriter<PK,D,IK>
{

	protected CompoundMapRWStorage<PK,D> mainNode;
	protected SortedMapStorage<IK,IE> indexNode;
	
	public ManualMultiIndexNode(CompoundMapRWStorage<PK,D> mainNode, SortedMapStorage<IK,IE> indexNode){
		this.mainNode = mainNode;
		this.indexNode = indexNode;
	}

	//TODO should i be passing config options around blindly?
	//TODO need to watch out for offset/limit
	
	
	/********************* IndexReader ******************************/
	
	@Override
	public List<D> lookupMulti(IK indexKey, boolean wildcardLastField, Config config){
		if(indexKey==null){ return new LinkedList<D>(); }
		//hard-coding startInclusive to true because it will usually be true on the first call, 
		// but subsequent calls may want false, so consider adding as method param
		List<IE> indexEntries = indexNode.getPrefixedRange(indexKey, wildcardLastField, null, true, config);
		List<PK> primaryKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		List<D> databeans = mainNode.reader().getMulti(primaryKeys, config);
		return databeans;
	}
	
	@Override
	public List<D> lookupMultiMulti(Collection<IK> indexKeys, boolean wildcardLastField, Config config){
		if(CollectionTool.isEmpty(indexKeys)){ return new LinkedList<D>(); }
		List<IE> indexEntries = ListTool.createLinkedList();
		for(IK indexKey : indexKeys){//TODO fetch all in one call getPrefixedRanges(...
			indexEntries.addAll(indexNode.getPrefixedRange(indexKey, wildcardLastField, null, true, config));
		}
		List<PK> primaryKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		List<D> databeans = mainNode.reader().getMulti(primaryKeys, config);
		return databeans;
	}

	@Override
	public SortedScannerIterable<IE> scan(Range<IK> range, Config config){
		return indexNode.scan(range, config);
	}

	@Override
	public SortedScannerIterable<D> scanAndFetch(Range<IK> range, Config config){
		return new SortedScannerIterable<D>(new ManagedIndexDatabeanScanner<>(mainNode.reader(), scan(range, config),
				config));
	}

	@Override
	public SortedScannerIterable<IK> scanKeys(Range<IK> range, Config config){
		return indexNode.scanKeys(range, config);
	}
}
