package com.hotpads.datarouter.app.client.parallel;

import com.hotpads.datarouter.app.ClientApp;

public interface ParallelClientApp<T>
extends ClientApp<T>{

	void reserveConections();
	void releaseConnections();

}
