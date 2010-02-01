package com.hotpads.datarouter.app;

import com.hotpads.datarouter.config.Isolation;


public interface TxnApp<T> 
extends ClientApp<T> {
	
	Isolation getIsolation();
	
}
