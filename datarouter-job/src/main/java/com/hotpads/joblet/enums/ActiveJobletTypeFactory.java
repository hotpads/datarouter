package com.hotpads.joblet.enums;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.joblet.setting.JobletThreadCountSettings;

@Singleton
public class ActiveJobletTypeFactory{

	private final JobletTypeFactory jobletTypeFactory;
	private final JobletThreadCountSettings jobletThreadCountSettings;

	@Inject
	public ActiveJobletTypeFactory(JobletTypeFactory jobletTypeFactory,
			JobletThreadCountSettings jobletThreadCountSettings){
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletThreadCountSettings = jobletThreadCountSettings;
	}


	public List<JobletType<?>> getActiveTypes(){
		return jobletTypeFactory.getAllTypes().stream()
				.filter(type -> jobletThreadCountSettings.getThreadCountForJobletType(type) > 0)
				.collect(Collectors.toList());
	}

	public List<JobletType<?>> getActiveTypesCausingScaling(){
		return jobletTypeFactory.getTypesCausingScaling().stream()
				.filter(type -> jobletThreadCountSettings.getThreadCountForJobletType(type) > 0)
				.collect(Collectors.toList());
	}

}
