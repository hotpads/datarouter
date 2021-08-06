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
package io.datarouter.tasktracker;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.tasktracker.scheduler.LongRunningTaskStatus;
import io.datarouter.tasktracker.storage.LongRunningTask;

public class LongRunningTaskDaoTests{

	@Test
	public void testGetOldestRunSinceLastSuccess(){
		List<LongRunningTask> tasks = new ArrayList<>();
		// null when empty
		Assert.assertNull(getOldestRunSinceLastSuccess(tasks));

		LongRunningTask first = new LongRunningTask();
		tasks.add(first);
		first.setJobExecutionStatus(LongRunningTaskStatus.SUCCESS);
		// only one item, which is SUCCESS, so return null
		Assert.assertNull(getOldestRunSinceLastSuccess(tasks));
		first.setJobExecutionStatus(LongRunningTaskStatus.INTERRUPTED);
		// one item, not SUCCESS, so return it
		Assert.assertEquals(getOldestRunSinceLastSuccess(tasks), first);

		LongRunningTask second = new LongRunningTask();
		tasks.add(second);
		second.setJobExecutionStatus(LongRunningTaskStatus.SUCCESS);
		// most recent item is SUCCESS, so null
		Assert.assertNull(getOldestRunSinceLastSuccess(tasks));
		second.setJobExecutionStatus(LongRunningTaskStatus.RUNNING);
		// all items are not SUCCESS, so return oldest one
		Assert.assertEquals(getOldestRunSinceLastSuccess(tasks), first);
		first.setJobExecutionStatus(LongRunningTaskStatus.SUCCESS);
		// only one item since last SUCCESS, so return it
		Assert.assertEquals(getOldestRunSinceLastSuccess(tasks), second);
	}

	// find the oldest run AFTER/SINCE the most recent SUCCESS
	private static LongRunningTask getOldestRunSinceLastSuccess(List<LongRunningTask> runs){
		LongRunningTask oldestRunSinceLastSuccess = null;
		for(int i = runs.size() - 1; i >= 0; i--){
			if(LongRunningTaskStatus.SUCCESS == runs.get(i).getJobExecutionStatus()){
				break;
			}
			oldestRunSinceLastSuccess = runs.get(i);
		}
		return oldestRunSinceLastSuccess;
	}

}
