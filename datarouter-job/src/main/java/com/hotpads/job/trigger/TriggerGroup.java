package com.hotpads.job.trigger;

import java.util.List;

import com.hotpads.job.web.TriggersRepository.JobPackage;

public interface TriggerGroup{

	List<JobPackage> makeJobPackages();

}
