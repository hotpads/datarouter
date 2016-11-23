package com.hotpads.joblet.queue;

import java.util.Optional;

import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.util.core.profile.PhaseTimer;

public interface JobletRequestSelector{

	Optional<JobletRequest> getJobletRequestForProcessing(PhaseTimer timer, JobletType<?> type, String reservedBy);

}
