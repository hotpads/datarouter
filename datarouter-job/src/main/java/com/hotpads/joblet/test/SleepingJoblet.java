package com.hotpads.joblet.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.joblet.BaseJoblet;
import com.hotpads.joblet.codec.BaseGsonJobletCodec;
import com.hotpads.joblet.test.SleepingJoblet.SleepingJobletParams;
import com.hotpads.util.core.concurrent.ThreadTool;

public class SleepingJoblet extends BaseJoblet<SleepingJobletParams>{
	private static final Logger logger = LoggerFactory.getLogger(SleepingJoblet.class);

	private static final long MAX_SEGMENT_MS = 1000;

	@Override
	public Long process(){
		logger.warn("starting SleepingJoblet {}", params.id);
		long startMs = System.currentTimeMillis();
		long remainingMs = params.sleepTimeMs;
		while(remainingMs > 0){
			assertShutdownNotRequested();
			ThreadTool.sleep(Math.min(remainingMs, MAX_SEGMENT_MS));
			long totalElapsedMs = System.currentTimeMillis() - startMs;
			remainingMs = params.sleepTimeMs - totalElapsedMs;
		}
		logger.warn("finished SleepingJoblet {}", params.id);
		return params.sleepTimeMs;
	}


	public static class SleepingJobletParams{
		private final String id;
		private final long sleepTimeMs;

		public SleepingJobletParams(String id, long sleepTimeMs){
			this.id = id;
			this.sleepTimeMs = sleepTimeMs;
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
