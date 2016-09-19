package com.hotpads.joblet;

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import com.hotpads.WebAppName;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;
import com.hotpads.datarouter.util.core.DrRuntimeTool;
import com.hotpads.joblet.enums.JobletQueueMechanism;
import com.hotpads.joblet.enums.JobletType;

public class JobletSettings
extends SettingNode{

	public static final int PERMITS_PER_HARDWARE_THREAD = 10;//TODO move this near joblets
	public static final int MAX_MEMORY_PERMITS = 1000;
	public static final int MAX_CPU_PERMITS = 16 * PERMITS_PER_HARDWARE_THREAD;

	private final Setting<Boolean> runJoblets;
	private final Setting<Integer> maxJobletServers;
	private final Setting<Integer> minJobletServers;
	private final Setting<Integer> numServersToAddPerPeriod;
	private final Setting<Integer> cpuTickets;
	private final Setting<Integer> memoryTickets;
	private final Setting<String> queueMechanism;


	@Inject
	public JobletSettings(SettingFinder finder, WebAppName webAppName,
			JobletThreadCountSettings jobletThreadCountSettings){
		super(finder, webAppName + ".joblet.", webAppName + ".");

		registerChild(jobletThreadCountSettings);

		runJoblets = registerBoolean("runJoblets", false);
		maxJobletServers = registerInteger("maxJobletServers", 16);
		minJobletServers = registerInteger("minJobletServers", 8);
		numServersToAddPerPeriod = registerInteger("numServersToAddPerPeriod", 1);

		int defaultCpuTickets = Math.max(DrRuntimeTool.getNumProcessors() * PERMITS_PER_HARDWARE_THREAD,
				2 * MAX_CPU_PERMITS);
		cpuTickets = registerInteger("cpuTickets", defaultCpuTickets);

		int defaultMemoryTickets = Math.max(DrRuntimeTool.getTotalMemoryMBytes(), 2
				* MAX_MEMORY_PERMITS);
		memoryTickets = registerInteger("memoryTickets", defaultMemoryTickets);

		queueMechanism = registerString("queueMechanism", JobletQueueMechanism.JDBC_LOCK_FOR_UPDATE
				.getPersistentString());
	}

	/*------------------ methods -----------------------*/

	public Integer getThreadCountForJobletType(JobletType<?> jobletType){
		return Optional.ofNullable(getThreadCountSettings().getThreadCountForJobletType(jobletType)).orElse(0);
	}

	public JobletQueueMechanism getQueueMechanismEnum(){
		return JobletQueueMechanism.fromPersistentString(getQueueMechanism().getValue());
	}

	/*------------------ node getters ------------------*/

	public JobletThreadCountSettings getThreadCountSettings(){
		String name = getName() + JobletThreadCountSettings.NAME + ".";
		return Objects.requireNonNull((JobletThreadCountSettings)getChildren().get(name));
	}

	/*-------------------- get/set ----------------------*/

	public Setting<Boolean> getRunJoblets(){
		return runJoblets;
	}

	public Setting<Integer> getMaxJobletServers(){
		return maxJobletServers;
	}

	public Setting<Integer> getMinJobletServers(){
		return minJobletServers;
	}

	public Setting<Integer> getNumServersToAddPerPeriod(){
		return numServersToAddPerPeriod;
	}

	public Setting<Integer> getCpuTickets(){
		return cpuTickets;
	}

	public Setting<Integer> getMemoryTickets(){
		return memoryTickets;
	}

	public Setting<String> getQueueMechanism(){
		return queueMechanism;
	}
}
