package com.hotpads.datarouter.op.executor;

import com.hotpads.datarouter.config.Isolation;


public interface TxnExecutor{

	Isolation getIsolation();
	void beginTxns();
	void commitTxns();
	void rollbackTxns();
	
}
