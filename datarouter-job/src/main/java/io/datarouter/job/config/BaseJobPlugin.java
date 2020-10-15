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
package io.datarouter.job.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.web.config.BaseWebPlugin;

/**
 * BaseJobPlugin is an extension of BaseWebPlugin. This plugin brings in auto registration and dynamic configuration of
 * TriggerGroups.
 */
public abstract class BaseJobPlugin extends BaseWebPlugin{

	private final List<Class<? extends BaseTriggerGroup>> triggerGroups = new ArrayList<>();

	protected void addTriggerGroup(Class<? extends BaseTriggerGroup> triggerGroup){
		triggerGroups.add(triggerGroup);
	}

	public List<Class<? extends BaseTriggerGroup>> getTriggerGroups(){
		return triggerGroups;
	}

	/*--------------------------- add job plugins ---------------------------*/

	private final List<BaseJobPlugin> jobPlugins = new ArrayList<>();

	protected void addJobPlugin(BaseJobPlugin plugin){
		jobPlugins.add(plugin);
	}

	public List<BaseJobPlugin> getJobPlugins(){
		return jobPlugins;
	}

}
