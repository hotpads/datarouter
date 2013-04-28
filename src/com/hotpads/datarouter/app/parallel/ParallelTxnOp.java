package com.hotpads.datarouter.app.parallel;

import com.hotpads.datarouter.config.Isolation;


public interface ParallelTxnOp<T>{

	Isolation getIsolation();
	void beginTxns();
	void commitTxns();
	void rollbackTxns();
	
}
