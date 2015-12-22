package com.hotpads.datarouter.node.adapter.callsite;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.profile.callsite.LineOfCode;

public interface CallsiteAdapter{

	void recordCallsite(Config config, long startNs, int numItems);
	void recordCollectionCallsite(Config config, long startTimeNs, Collection<?> items);
	LineOfCode getCallsite();

}
