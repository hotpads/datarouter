/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.nodewatch;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.storage.node.tableconfig.ClientTableEntityPrefixNameWrapper;

public class TableSamplerCounters{

	private final ClientTableEntityPrefixNameWrapper nodeNames;

	public TableSamplerCounters(ClientTableEntityPrefixNameWrapper nodeNames){
		this.nodeNames = nodeNames;
	}

	public void incrementRpcs(long by){
		incrementBy(" rpcs", by);
	}

	//number of keys transferred over the network
	public void incrementKeys(long by){
		incrementBy(" keys", by);
	}

	public void incrementRows(long by){
		incrementBy(" rows", by);
	}

	private void incrementBy(String suffix, long rows){
		inc(" all" + suffix, rows);

		String clientPart = " client " + nodeNames.getClientName();
		inc(clientPart + suffix, rows);

		String tablePart = " table " + nodeNames.getTableName();
		inc(clientPart + tablePart + suffix, rows);

		if(nodeNames.hasSubEntityPrefix()){
			String subEntityPrefixPart = " subEntityPrefix " + nodeNames.getSubEntityPrefix();
			inc(clientPart + tablePart + subEntityPrefixPart + suffix, rows);
		}
	}

	private void inc(String name, long by){
		Counters.inc("TableSampler" + name, by);
	}

}
