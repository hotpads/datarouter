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
package io.datarouter.storage.config.setting.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingNode;
import io.datarouter.util.cached.Cached;

@Singleton
public class NodeWatchSettings extends SettingNode{

	private final Cached<Boolean> tableSamplerJob;
	private final Cached<Boolean> tableRowCountJob;
	private final Cached<Boolean> tableSizeMonitoringJob;

	@Inject
	public NodeWatchSettings(SettingFinder finder){
		super(finder, "datarouter.nodewatch.");
		tableSamplerJob = registerBoolean("tableSampler", false);
		tableRowCountJob = registerBoolean("tableRowCount", false);
		tableSizeMonitoringJob = registerBoolean("tableSizeMonitoringJob", false);
	}

	public Cached<Boolean> getTableSamplerJob(){
		return tableSamplerJob;
	}

	public Cached<Boolean> getTableRowCount(){
		return tableRowCountJob;
	}

	public Cached<Boolean> getTableSizeMonitoringJob(){
		return tableSizeMonitoringJob;
	}

}
