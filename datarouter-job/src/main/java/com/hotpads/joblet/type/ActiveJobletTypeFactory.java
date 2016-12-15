package com.hotpads.joblet.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.clustersetting.ClusterSettingService;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.joblet.setting.JobletThreadCountSettings;
import com.hotpads.webappinstance.databean.WebAppInstance;

@Singleton
public class ActiveJobletTypeFactory{

	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private JobletThreadCountSettings jobletThreadCountSettings;
	@Inject
	private ClusterSettingService clusterSettingService;


	public List<JobletType<?>> getAllActiveTypes(){
		return getActiveTypesFrom(jobletTypeFactory.getAllTypes());
	}

	public List<JobletType<?>> getActiveTypesCausingScaling(){
		return getActiveTypesFrom(jobletTypeFactory.getTypesCausingScaling());
	}

	private List<JobletType<?>> getActiveTypesFrom(Collection<JobletType<?>> typesToConsider){
		List<JobletType<?>> activeTypes = new ArrayList<>();
		for(JobletType<?> type : typesToConsider){
			Setting<Integer> setting = jobletThreadCountSettings.getSettingForJobletType(type);
			Map<WebAppInstance,Integer> threadCountByWebAppInstance = clusterSettingService
					.getSettingValueByWebAppInstance(setting);
			Optional<Integer> anyPositiveThreadCount = threadCountByWebAppInstance.values().stream()
					.filter(threadCount -> threadCount > 0)
					.findAny();
			if(anyPositiveThreadCount.isPresent()){
				activeTypes.add(type);
			}
		}
		return activeTypes;
	}

}
