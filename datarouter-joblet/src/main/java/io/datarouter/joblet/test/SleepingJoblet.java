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
package io.datarouter.joblet.test;

import io.datarouter.joblet.codec.DatarouterBaseGsonJobletCodec;
import io.datarouter.joblet.model.BaseJoblet;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.test.SleepingJoblet.SleepingJobletParams;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletType.JobletTypeBuilder;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.concurrent.ThreadTool;

public class SleepingJoblet extends BaseJoblet<SleepingJobletParams>{

	public static final JobletType<SleepingJobletParams> JOBLET_TYPE = new JobletTypeBuilder<>(
			"SleepingJoblet", //unnecessary word "Joblet" in name
			SleepingJobletCodec::new,
			SleepingJoblet.class)
			.withShortQueueName("Sleeping") //unnecessary shortQueueName
			.withTag(Tag.DATAROUTER)
			.build();

	private static final long MAX_SEGMENT_MS = 1000;

	@Override
	public void process(){
		long startMs = System.currentTimeMillis();
		long remainingMs = params.sleepTimeMs;
		int numPreviousFailures = getJobletRequest().getNumFailures();
		if(numPreviousFailures < params.numFailures){
			int thisFailureNum = numPreviousFailures + 1;
			String message = "SleepingJoblet intentional failure " + thisFailureNum + "/" + JobletRequest.MAX_FAILURES
					+ " on " + jobletRequest;
			throw new RuntimeException(message);
		}
		while(remainingMs > 0){
			assertShutdownNotRequested();
			ThreadTool.sleepUnchecked(Math.min(remainingMs, MAX_SEGMENT_MS));
			long totalElapsedMs = System.currentTimeMillis() - startMs;
			remainingMs = params.sleepTimeMs - totalElapsedMs;
		}
	}

	public record SleepingJobletParams(
			String id,
			long sleepTimeMs,
			int numFailures){
	}

	public static class SleepingJobletCodec
	extends DatarouterBaseGsonJobletCodec<SleepingJobletParams>{

		public SleepingJobletCodec(){
			super(SleepingJobletParams.class);
		}

		@Override
		public int calculateNumItems(SleepingJobletParams params){
			return 1;
		}

	}

}
