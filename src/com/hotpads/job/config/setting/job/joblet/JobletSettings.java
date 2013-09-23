package com.hotpads.job.config.setting.job.joblet;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.job.setting.ClusterSettingFinder;
import com.hotpads.job.setting.Setting;
import com.hotpads.job.setting.SettingNode;
import com.hotpads.job.setting.cached.imp.BooleanCachedSetting;

@Singleton
public class JobletSettings extends SettingNode{
	
	protected Setting<Boolean> runFailedJobletProcessor = new BooleanCachedSetting(finder, 
			getName()+"runFailedJobletProcessor", false);
	protected Setting<Boolean> runJoblets = new BooleanCachedSetting(finder, getName()+"runJoblets", true);
	
	@Inject
	public JobletSettings(ThreadCountSettings threadCount, ClusterSettingFinder finder) {
		super(finder, "job.joblet.", "job.");
		children.put(threadCount.getName(), threadCount);
		register(runFailedJobletProcessor);
		register(runJoblets);
	}

		
	/*******************node getters*******************/

	public ThreadCountSettings getThreadCount() {
		return (ThreadCountSettings)getChildren().get(getName()+"threadCount.");
	}

	
	/*******************leaf getters*******************/

	public Setting<Boolean> getRunFailedJobletProcessor() {
		return runFailedJobletProcessor;
	}

	public Setting<Boolean> getRunJoblets() {
		return runJoblets;
	}
	
	
	/*******************tests*******************/

	public static class JobletSettingsTester {
		@Test public void testInjection() {
			Injector injector = Guice.createInjector(new ServiceModule());
			JobletSettings settings = injector.getInstance(JobletSettings.class);
			System.out.println("Test getRunFailedJobletProcessor() value : "+settings.getRunFailedJobletProcessor().getValue());
			System.out.println("Test getRunJoblets() value : "+settings.getRunJoblets().getValue());
		}
	}

}
