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
import com.hotpads.job.setting.cached.imp.IntegerCachedSetting;

@Singleton
public class MaintenanceSettings extends SettingNode{
	
	protected Setting<Boolean> aggregateUserItemRecords = new BooleanCachedSetting(finder, 
			getName()+"aggregateUserItemRecords", false);
	protected Setting<Boolean> viewCleanup = new BooleanCachedSetting(finder, getName()+"viewCleanup", false);
	protected Setting<Boolean> processPendingS3Deletes = new BooleanCachedSetting(finder, 
			getName()+"processPendingS3Deletes", false);
	protected Setting<Boolean> deleteExpiredListings = new BooleanCachedSetting(finder, 
			getName()+"deleteExpiredListings", false);
	protected Setting<Boolean> cleanupConfig = new BooleanCachedSetting(finder, getName()+"cleanupConfig", false);
	protected Setting<Boolean> backup = new BooleanCachedSetting(finder, getName()+"backup", false);
	protected Setting<Boolean> importPipelineMonitoring = new BooleanCachedSetting(finder, 
			getName()+"importPipelineMonitoring", false);
	protected Setting<Boolean> releaseUnusedPhoneNumbers = new BooleanCachedSetting(finder, 
			getName()+"releaseUnusedPhoneNumbers", false);
	protected Setting<Boolean> hbaseCompactions = new BooleanCachedSetting(finder, getName()+"hbaseCompactions", false);
	protected Setting<Boolean> listingDupeVacuum = new BooleanCachedSetting(finder, getName()+"listingDupeVacuum", false);
	protected Setting<Boolean> sessionDatabeanVacuum = new BooleanCachedSetting(finder, getName()+"sessionDatabeanVacuum", false);
	protected Setting<Integer> maxRegionsPerMinute = new IntegerCachedSetting(finder, getName()+"maxRegionsPerMinute", 1);
	protected Setting<Boolean> jobletCounter = new BooleanCachedSetting(finder, getName()+"jobletCounter", false);
	
	@Inject
	public MaintenanceSettings(ClusterSettingFinder finder) {
		super(finder, "job.maintenance.", "job.");
		register(sessionDatabeanVacuum);
		register(jobletCounter);
		register(processPendingS3Deletes);
		register(deleteExpiredListings);
		//register(backup);
		register(viewCleanup);
	}
	
	public Setting<Boolean> getAggregateUserItemRecords() {
		return aggregateUserItemRecords;
	}
	
	public Setting<Boolean> getViewCleanup() {
		return viewCleanup;
	}
	
	public Setting<Boolean> getProcessPendingS3Deletes() {
		return processPendingS3Deletes;
	}
	
	public Setting<Boolean> getDeleteExpiredListings() {
		return deleteExpiredListings;
	}
	
	public Setting<Boolean> getCleanupConfig() {
		return cleanupConfig;
	}
	
	public Setting<Boolean> getBackup(){
		return backup;
	}
	
	public Setting<Boolean> getImportPipelineMonitoring() {
		return importPipelineMonitoring;
	}
	
	public Setting<Boolean> getReleaseUnusedPhoneNumbers() {
		return releaseUnusedPhoneNumbers;
	}
	
	public Setting<Boolean> getHbaseCompactions(){
		return hbaseCompactions;
	}
	
	public Setting<Boolean> getListingDupeVacuum(){
		return listingDupeVacuum;
	}
	
	public Setting<Boolean> getSessionDatabeanVacuum(){
		return sessionDatabeanVacuum;
	}
	
	public Setting<Boolean> getJobletCounter(){
		return jobletCounter;
	}
	
	public Setting<Integer> getMaxRegionsPerMinute(){
		return maxRegionsPerMinute;
	}
	
	public static class MaintenanceSettingsTester {
		@Test public void testInjection() {
			Injector injector = Guice.createInjector(new ServiceModule());
			MaintenanceSettings settings = injector.getInstance(MaintenanceSettings.class);
			System.out.println("Test getAggregateUserItemRecords() value : "+settings.getAggregateUserItemRecords().getValue());
			System.out.println("Test getViewCleanup() value : "+settings.getViewCleanup().getValue());
			System.out.println("Test getProcessPendingS3Deletes() value : "+settings.getProcessPendingS3Deletes().getValue());
			System.out.println("Test getDeleteExpiredListings() value : "+settings.getDeleteExpiredListings().getValue());
			System.out.println("Test getCleanupConfig() value : "+settings.getCleanupConfig().getValue());
			System.out.println("Test getBackup() value :"+settings.getBackup().getValue());
			System.out.println("Test getImportPipelineMonitoring() value : "+settings.getImportPipelineMonitoring().getValue());
			System.out.println("Test getReleaseUnusedPhoneNumbers() value : "+settings.getReleaseUnusedPhoneNumbers().getValue());
			System.out.println("Test getHbaseCompactions() value : "+settings.getHbaseCompactions().getValue());
			System.out.println("Test getListingDupeVacuum() value : "+settings.getListingDupeVacuum().getValue());
			System.out.println("Test getMaxRegionsPerMinute() value : "+settings.getMaxRegionsPerMinute().getValue());
		}
	}
	
}
