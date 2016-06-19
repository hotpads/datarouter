package com.hotpads.joblet;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.joblet.enums.JobletType;

public interface JobletSettings{

	Setting<Boolean> getRunJoblets();
	Setting<Integer> getMaxJobletServers();
	Setting<Integer> getMinJobletServers();
	Setting<Integer> getNumServersToAddPerPeriod();
	Setting<Integer> getCpuTickets();
	Setting<Integer> getMemoryTickets();

	Integer getThreadCountForJobletType(JobletType<?> jobletType);

}
