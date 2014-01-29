package com.hotpads.datarouter.op;

import com.hotpads.datarouter.config.Isolation;


public interface TxnOp<T>
extends ClientOp<T>{

	Isolation getIsolation();
	boolean isAutoCommit();
	
}
