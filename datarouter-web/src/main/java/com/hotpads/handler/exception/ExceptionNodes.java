package com.hotpads.handler.exception;

import com.hotpads.datarouter.monitoring.exception.HttpRequestRecord;
import com.hotpads.datarouter.monitoring.exception.HttpRequestRecordKey;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;

public interface ExceptionNodes{

	SortedMapStorage<ExceptionRecordKey, ExceptionRecord> getExceptionRecordNode();
	IndexedSortedMapStorage<HttpRequestRecordKey, HttpRequestRecord> getHttpRequestRecordNode();

}