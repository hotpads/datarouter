package com.hotpads.export;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.imp.noop.NoOpNode;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;


@Singleton
public class EmptyDataExportNodesProvider implements DataExportNodes{

	@Override
	public IndexedSortedMapStorage<DataExportItemKey, DataExportItem> getDataExportItem(){
		return new NoOpNode<>();
	}
}
