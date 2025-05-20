/*
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
package io.datarouter.nodewatch.link;

import java.util.Optional;

import io.datarouter.httpclient.endpoint.link.DatarouterLink;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;

public class NodewatchThresholdEditLink extends DatarouterLink{

	public static final String
			P_clientName = "clientName",
			P_tableName = "tableName",
			P_maxRows = "maxRows",
			P_update = "update";

	public String clientName;
	public String tableName;
	public Optional<String> maxRows = Optional.empty();
	public Optional<Boolean> update = Optional.empty();

	public NodewatchThresholdEditLink(String clientName, String tableName){
		super(new DatarouterNodewatchPaths().datarouter.nodewatch.threshold.edit);
		this.clientName = clientName;
		this.tableName = tableName;
	}

	public NodewatchThresholdEditLink withMaxRows(String maxRows){
		this.maxRows = Optional.of(maxRows);
		return this;
	}

	public NodewatchThresholdEditLink withUpdate(boolean update){
		this.update = Optional.of(update);
		return this;
	}
}
