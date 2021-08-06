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
package io.datarouter.joblet.enums;

import org.testng.Assert;
import org.testng.annotations.Test;

public class JobletPriorityTests{

	@Test
	public void testGetLowestPriorty(){
		JobletPriority lowestPriority = JobletPriority.getLowestPriority();
		for(JobletPriority priority : JobletPriority.values()){
			if(priority == lowestPriority){
				continue;
			}
			Assert.assertTrue(priority.isHigher(lowestPriority), priority.name()
					+ " is lower than lowest priority " + lowestPriority.name());
		}
	}

	@Test
	public void testGetHighestPriorty(){
		JobletPriority highestPriority = JobletPriority.getHighestPriority();
		for(JobletPriority priority : JobletPriority.values()){
			if(priority == highestPriority){
				continue;
			}
			Assert.assertFalse(priority.isHigher(highestPriority), priority.name()
					+ " is higher than highest priority " + highestPriority.name());
		}
	}

}
