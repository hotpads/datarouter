package com.hotpads.datarouter.app.client.parallel;

import com.hotpads.datarouter.app.TxnApp;

public interface ParallelTxnApp<T>
extends TxnApp<T>{

	void beginTxns() throws Exception;
	void commitTxns() throws Exception;
	void rollbackTxns() throws Exception;
	
}
