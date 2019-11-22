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

public abstract class BaseJobPlugin extends BaseWebPlugin{

	private final List<Class<? extends BaseTriggerGroup>> triggerGroupClasses = new ArrayList<>();

	protected void addTriggerGroup(Class<? extends BaseTriggerGroup> triggerGroup){
		triggerGroupClasses.add(triggerGroup);
	}

	public List<Class<? extends BaseTriggerGroup>> getTriggerGroupClasses(){
		return triggerGroupClasses;
	}

}
