package com.hotpads.joblet.execute;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.hotpads.joblet.dto.RunningJoblet;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.util.core.exception.NotImplementedException;

public interface JobletProcessors{

	void createAndStartProcessors();
	void requestShutdown();
	Map<JobletType<?>,List<RunningJoblet>> getRunningJobletsByType();

	@Deprecated //use dto
	default List<JobletExecutorThread> getCurrentlyRunningJobletExecutorThreads(){
		return Collections.emptyList();
	}

	@Deprecated //remove
	default void killThread(long threadId){
		throw new NotImplementedException();
	}

	@Deprecated //remove
	default void restartExecutor(int jobletTypeCode){
		throw new NotImplementedException();
	}

	@Deprecated //rewrite
	default Map<JobletType<?>,ParallelJobletProcessor> getMap(){
		return Collections.emptyMap();
	}

}
