package com.hotpads.joblet.execute;

import java.util.List;
import java.util.Map;

import com.hotpads.joblet.dto.JobletTypeSummary;
import com.hotpads.joblet.dto.RunningJoblet;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.util.core.exception.NotImplementedException;

public interface JobletProcessors{

	void createAndStartProcessors();
	Map<JobletType<?>,List<RunningJoblet>> getRunningJobletsByType();
	List<JobletTypeSummary> getTypeSummaries();
	void killThread(long threadId);
	void requestShutdown();


	@Deprecated //remove
	default void restartExecutor(int jobletTypeCode){
		throw new NotImplementedException();
	}

}
