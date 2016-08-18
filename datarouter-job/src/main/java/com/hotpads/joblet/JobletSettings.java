package com.hotpads.joblet;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.joblet.enums.JobletQueueMechanism;
import com.hotpads.joblet.enums.JobletType;

public interface JobletSettings{

	Setting<Boolean> getRunJoblets();
	Setting<Integer> getMaxJobletServers();
	Setting<Integer> getMinJobletServers();
	Setting<Integer> getNumServersToAddPerPeriod();
	Setting<Integer> getCpuTickets();
	Setting<Integer> getMemoryTickets();
	Setting<String> getQueueMechanism();

	Integer getThreadCountForJobletType(JobletType<?> jobletType);

	default JobletQueueMechanism getQueueMechanismEnum(){
		return JobletQueueMechanism.fromPersistentString(getQueueMechanism().getValue());
	}

}
