package com.hotpads.job.setting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.job.setting.cached.imp.BooleanCachedSetting;
import com.hotpads.job.setting.cached.imp.IntegerCachedSetting;
import com.hotpads.job.setting.cached.imp.StringCachedSetting;
import com.hotpads.util.core.RuntimeTool;

@Singleton
public class JobSettings extends SettingNode{
	
	//protected Setting<Integer> jobletThreadTickets = new IntegerCachedSetting(finder, getName()+"jobletThreadTickets", 16);
	protected Setting<Integer> cpuTickets = new IntegerCachedSetting(finder, getName()+"cpuTickets", RuntimeTool.getNumProcessors() * 20);
	protected Setting<Integer> memoryTickets = new IntegerCachedSetting(finder, getName()+"memoryTickets", RuntimeTool.getTotalMemoryMBytes());
	protected Setting<Boolean> processJobs = new BooleanCachedSetting(finder, getName()+"processJobs", true);
	protected Setting<String> logo = new StringCachedSetting(finder, getName()+"logo", 
			"http://politicallunacy.files.wordpress.com/2010/07/christmas-cake.jpg");
	
	@Inject
	public JobSettings(ClusterSettingFinder finder, JobTriggers triggers, JobletSettings joblet, EventSettings event,
			StatSettings stat, FraudSettings fraud, MaintenanceSettings maintenance, ProfilingSettings profiling, 
			PropertySettings property) {
		super(finder, "job.", "");
		
		children.put(event.getName(), event);	
		children.put(joblet.getName(), joblet);
		children.put(maintenance.getName(), maintenance);
		children.put(profiling.getName(), profiling);
		children.put(property.getName(), property);
		children.put(stat.getName(), stat);
		children.put(fraud.getName(), fraud);
		children.put(triggers.getName(), triggers);
		
		register(processJobs);
		register(logo);
		//register(jobletThreadTickets);
		register(cpuTickets);
		register(memoryTickets);
	}	
	

	/******************* node getters *******************/
	
	public EventSettings getEvent() {
		return (EventSettings)getChildren().get(getName()+"event.");
	}
	
	public JobletSettings getJoblet() {
		return (JobletSettings)getChildren().get(getName()+"joblet.");
	}
	
	public MaintenanceSettings getMaintenance() {
		return (MaintenanceSettings)getChildren().get(getName()+"maintenance.");
	}
	
	public ProfilingSettings getProfiling(){
		return (ProfilingSettings)getChildren().get(getName()+"profiling.");
	}
	
	public PropertySettings getProperty() {
		return (PropertySettings)getChildren().get(getName()+"property.");
	}
	
	public StatSettings getStat() {
		return (StatSettings)getChildren().get(getName()+"stat.");
	}
	
	public FraudSettings getFraud() {
		return (FraudSettings)getChildren().get(getName()+"fraud.");
	}
	
	public JobTriggers getTriggers() {
		return (JobTriggers)getChildren().get(getName()+"trigger.");
	}
	
	
	/******************* leaf getters *******************/
	
	public Setting<Boolean> getProcessJobs(){
		return processJobs;
	}
	
	/*public Setting<Integer> getJobletThreadTickets(){
		return jobletThreadTickets;
	}*/
	
	public Setting<Integer> getCpuTickets(){
		return cpuTickets;
	}
	
	public Setting<Integer> getMemoryTickets(){
		return memoryTickets;
	}
	
	public Setting<String> getUrl(){
		return logo;
	}
	
}
