package com.hotpads.datarouter.node.type.index;

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
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SingleUseScannerIterable;

public class ManualUniqueIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends UniqueIndexEntry<IK,IE,PK,D>>
implements UniqueIndexNode<PK, D, IK, IE>{

	protected MapStorage<PK,D> mainNode;
	protected SortedMapStorage<IK,IE> indexNode;

	public ManualUniqueIndexNode(MapStorage<PK,D> mainNode, SortedMapStorage<IK,IE> indexNode){
		this.mainNode = mainNode;
		this.indexNode = indexNode;
	}

	//TODO should i be passing config options around blindly?


	@Override
	public D lookupUnique(IK uniqueKey, Config config){
		if(uniqueKey==null){
			return null;
		}
		IE indexEntry = indexNode.get(uniqueKey, config);
		if(indexEntry==null){
			return null;
		}
		PK primaryKey = indexEntry.getTargetKey();
		D databean = mainNode.get(primaryKey, config);
		return databean;
	}


	@Override
	public List<D> lookupMultiUnique(Collection<IK> uniqueKeys, Config config){
		if(DrCollectionTool.isEmpty(uniqueKeys)){
			return new LinkedList<>();
		}
		List<IE> indexEntries = indexNode.getMulti(uniqueKeys, config);
		List<PK> primaryKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		List<D> databeans = mainNode.getMulti(primaryKeys, config);
		return databeans;
	}


	@Override
	public void deleteUnique(IK indexKey, Config config){
		if(indexKey==null){
			return;
		}
		IE indexEntry = indexNode.get(indexKey, config);
		if(indexEntry==null){
			return;
		}
		PK primaryKey = indexEntry.getTargetKey();
		indexNode.delete(indexKey, config);
		mainNode.delete(primaryKey, config);
	}


	@Override
	public void deleteMultiUnique(Collection<IK> uniqueKeys, Config config){
		if(DrCollectionTool.isEmpty(uniqueKeys)){
			return;
		}
		List<IE> indexEntries = indexNode.getMulti(uniqueKeys, config);
		List<PK> primaryKeys = IndexEntryTool.getPrimaryKeys(indexEntries);
		indexNode.deleteMulti(uniqueKeys, config);
		mainNode.deleteMulti(primaryKeys, config);
	}

	@Override
	public Iterable<IE> scanMulti(Collection<Range<IK>> ranges, Config config){
		return indexNode.scanMulti(ranges, config);
	}

	@Override
	public Iterable<IK> scanKeysMulti(Collection<Range<IK>> ranges, Config config){
		return indexNode.scanKeysMulti(ranges, config);
	}

	@Override
	public SingleUseScannerIterable<D> scanDatabeans(Range<IK> range, Config config){
		return new SingleUseScannerIterable<>(new ManagedIndexDatabeanScanner<>(mainNode, scan(range, config),
				config));
	}

	@Override
	public IE get(IK uniqueKey, Config config){
		return indexNode.get(uniqueKey, config);
	}

	@Override
	public List<IE> getMulti(Collection<IK> uniqueKeys, Config config){
		return indexNode.getMulti(uniqueKeys, config);
	}

}
