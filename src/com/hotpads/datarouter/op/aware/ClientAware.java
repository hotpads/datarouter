package com.hotpads.datarouter.op.aware;

import com.hotpads.datarouter.client.Client;

public interface ClientAware<T>{

	Client getClient();
	
}
