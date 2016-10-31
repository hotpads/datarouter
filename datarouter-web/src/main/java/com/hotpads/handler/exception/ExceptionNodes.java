package com.hotpads.handler.exception;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.exception.analysis.HttpRequestRecord;
import com.hotpads.exception.analysis.HttpRequestRecordKey;

public interface ExceptionNodes{

	SortedMapStorage<ExceptionRecordKey, ExceptionRecord> getExceptionRecordNode();
	IndexedSortedMapStorage<HttpRequestRecordKey, HttpRequestRecord> getHttpRequestRecordNode();

}