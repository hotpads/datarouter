package com.hotpads.export;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage;

public interface DataExportNodes{

	SortedMapStorage<DataExportItemKey,DataExportItem> getDataExportItem();
}
