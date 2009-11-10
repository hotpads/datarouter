package com.hotpads.datarouter.app.client.parallel;

import com.hotpads.datarouter.app.TxnApp;

public interface ParallelTxnApp<T>
extends TxnApp<T>{

	void beginTxns();
	void commitTxns();
	void rollbackTxns();
	
}
