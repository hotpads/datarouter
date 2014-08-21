package com.hotpads.logging;

import com.hotpads.util.core.logging.HotPadsLog4j2Configuration;
import com.hotpads.util.core.logging.UtilLog4j2Configuration;

public final class DatarouterLog4j2Configuration extends HotPadsLog4j2Configuration{

	public DatarouterLog4j2Configuration(){
		registerParent(UtilLog4j2Configuration.class);
	}

}
