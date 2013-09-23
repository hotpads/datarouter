package com.hotpads.job.config.setting.job;

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
public class StatSettings extends SettingNode{

	protected Setting<Boolean> archiveAreaStat = new BooleanCachedSetting(finder, 
			getName()+"archiveAreaStat", false);	
	protected Setting<Boolean> createDailyPricingStatTasks = new BooleanCachedSetting(finder,
			getName()+"createDailyPricingStatTasks", false);
	protected Setting<Boolean> testJob1 = new BooleanCachedSetting(finder, 
			getName()+"testJob1", false);
	
	@Inject
	public StatSettings(ClusterSettingFinder finder){
		super(finder, "job.stat.", "job.");
		register(archiveAreaStat);
		register(createDailyPricingStatTasks);
		register(testJob1);
	}


	public Setting<Boolean> getArchiveAreaStat(){
		return archiveAreaStat;
	}

	public Setting<Boolean> getCreateDailyPricingStatTasks(){
		return createDailyPricingStatTasks;
	}
	
	public Boolean getRunTest1() {
		return testJob1.getValue();
	}
	
	
	
	public static class StatsSettingsTester {
		@Test public void testInjection() {
			Injector injector = Guice.createInjector(new ServiceModule());
			StatSettings settings = injector.getInstance(StatSettings.class);
			System.out.println("Test getArchiveAreaStat() value : "+settings.getArchiveAreaStat());
			System.out.println("Test getCreateDailyPricingStatTasks() value : "+settings.getCreateDailyPricingStatTasks());
			System.out.println("Test getRunTest1() value : "+settings.getRunTest1());
		}
	}
	
}