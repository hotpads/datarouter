package com.hotpads.datarouter.app;

import com.hotpads.datarouter.config.Isolation;


public interface TxnOp<T>{
	
	Isolation getIsolation();
	void beginTxn();
	void commitTxn();
	void rollbackTxn();
	
}
