package com.hotpads.joblet.queue;

import java.util.Optional;

import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletType;

public interface JobletRequestSelector{

	Optional<JobletRequest> getJobletRequestForProcessing(JobletType<?> type, String reservedBy);

}
