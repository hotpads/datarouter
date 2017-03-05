package com.hotpads.handler.exception;

import com.hotpads.datarouter.client.imp.noop.NoOpNode;
import com.hotpads.datarouter.monitoring.exception.HttpRequestRecord;
import com.hotpads.datarouter.monitoring.exception.HttpRequestRecordKey;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;

public class NoOpExceptionNodes implements ExceptionNodes{

	@Override
	public SortedMapStorage<ExceptionRecordKey,ExceptionRecord> getExceptionRecordNode(){
		return new NoOpNode<>();
	}

	@Override
	public IndexedSortedMapStorage<HttpRequestRecordKey,HttpRequestRecord> getHttpRequestRecordNode(){
		return new NoOpNode<>();
	}

}
