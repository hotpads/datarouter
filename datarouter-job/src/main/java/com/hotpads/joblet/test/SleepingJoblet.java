package com.hotpads.joblet.test;

import com.hotpads.joblet.BaseJoblet;
import com.hotpads.joblet.codec.BaseGsonJobletCodec;
import com.hotpads.joblet.test.SleepingJoblet.SleepingJobletParams;
import com.hotpads.util.core.concurrent.ThreadTool;

public class SleepingJoblet extends BaseJoblet<SleepingJobletParams>{

	@Override
	public Long process(){
		ThreadTool.sleep(params.sleepTimeMs);
		return params.sleepTimeMs;
	}


	public static class SleepingJobletParams{
		private final long sleepTimeMs;

		public SleepingJobletParams(long sleepTimeMs){
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
