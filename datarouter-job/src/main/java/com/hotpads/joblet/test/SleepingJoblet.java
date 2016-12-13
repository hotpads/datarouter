package com.hotpads.joblet.test;

import com.hotpads.joblet.BaseJoblet;
import com.hotpads.joblet.codec.BaseGsonJobletCodec;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.test.SleepingJoblet.SleepingJobletParams;
import com.hotpads.joblet.type.JobletType;
import com.hotpads.util.core.concurrent.ThreadTool;

public class SleepingJoblet extends BaseJoblet<SleepingJobletParams>{

	public static final JobletType<SleepingJobletParams> JOBLET_TYPE = new JobletType<>(-1, "SleepingJoblet",
			"Sleeping", SleepingJobletCodec::new, SleepingJoblet.class, 1, 1, true);

	private static final long MAX_SEGMENT_MS = 1000;

	@Override
	public Long process(){
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
			ThreadTool.sleep(Math.min(remainingMs, MAX_SEGMENT_MS));
			long totalElapsedMs = System.currentTimeMillis() - startMs;
			remainingMs = params.sleepTimeMs - totalElapsedMs;
		}
		return params.sleepTimeMs;
	}


	public static class SleepingJobletParams{
		private final String id;
		private final long sleepTimeMs;
		private final int numFailures;

		public SleepingJobletParams(String id, long sleepTimeMs, int numFailures){
			this.id = id;
			this.sleepTimeMs = sleepTimeMs;
			this.numFailures = numFailures;
		}
	}


	public static class SleepingJobletCodec extends BaseGsonJobletCodec<SleepingJobletParams>{
		public SleepingJobletCodec(){
			super(SleepingJobletParams.class);
		}

		@Override
		public int calculateNumItems(SleepingJobletParams params){
			return 1;
		}
	}

}
