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
package io.datarouter.job.readme;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Singleton;

@Singleton
public class ExampleTriggerGroup extends BaseTriggerGroup{

	public ExampleTriggerGroup(){
		super("Example", ZoneIds.AMERICA_NEW_YORK); // category name
		registerLocked(
				"3 * * * * ?", // trigger on the 3rd second of every minute
				() -> true, //  run unconditionally, or alternatively pass a dynamic setting
				ExampleJob.class, // the job class
				true); // alert if the job can't finish before the next trigger
	}

}
