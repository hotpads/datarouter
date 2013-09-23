package com.hotpads.job.config.setting.job.joblet;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.job.setting.ClusterSettingFinder;
import com.hotpads.job.setting.Setting;
import com.hotpads.job.setting.SettingNode;
import com.hotpads.job.setting.cached.imp.IntegerCachedSetting;

@Singleton
public class ThreadCountSettings extends SettingNode{
	
	protected Setting<Integer> geocode = new IntegerCachedSetting(finder, getName()+"geocode", 1);		
	protected Setting<Integer> areaLookup = new IntegerCachedSetting(finder, getName()+"areaLookup", 1);	
	protected Setting<Integer> viewRendering = new IntegerCachedSetting(finder, getName()+"viewRendering", 1);	
	protected Setting<Integer> dailyPricingStats = new IntegerCachedSetting(finder, getName()+"dailyPricingStats", 1);	
	protected Setting<Integer> deletion = new IntegerCachedSetting(finder, getName()+"deletion", 1);	
	protected Setting<Integer> deactivation = new IntegerCachedSetting(finder, getName()+"deactivation", 1);	
	protected Setting<Integer> feed = new IntegerCachedSetting(finder, getName()+"feed", 1);	
	protected Setting<Integer> imageCaching = new IntegerCachedSetting(finder, getName()+"imageCaching", 1);	
	protected Setting<Integer> monthlyPricingStats = new IntegerCachedSetting(finder, getName()+"monthlyPricingStats", 1);	
	protected Setting<Integer> areaBorderRendering = new IntegerCachedSetting(finder, getName()+"areaBorderRendering", 1);	
	protected Setting<Integer> areaRelationshipLookup = new IntegerCachedSetting(finder, getName()+"areaRelationshipLookup", 1);	
	protected Setting<Integer> areaListingsLookup = new IntegerCachedSetting(finder, getName()+"areaListingsLookup", 1);		
	protected Setting<Integer> dailyListingEventAggregation = new IntegerCachedSetting(finder, getName()+"dailyListingEventAggregation", 0);	
	protected Setting<Integer> dailyListingTypeEventAggregation = new IntegerCachedSetting(finder, getName()+"dailyListingTypeEventAggregation", 0);	
	protected Setting<Integer> dailyEventAggregation = new IntegerCachedSetting(finder, getName()+"dailyEventAggregation", 0);	
	protected Setting<Integer> dailyAreaEventAggregation = new IntegerCachedSetting(finder, getName()+"dailyAreaEventAggregation", 0);	
	protected Setting<Integer> dailyCompanyEventAggregation = new IntegerCachedSetting(finder, getName()+"dailyCompanyEventAggregation", 0);	
	protected Setting<Integer> dailyFeedEventAggregation = new IntegerCachedSetting(finder, getName()+"dailyFeedEventAggregation", 0);
	protected Setting<Integer> dailyRefEventAggregation = new IntegerCachedSetting(finder, getName()+"dailyRefEventAggregation", 0);	
	protected Setting<Integer> eventIndexing = new IntegerCachedSetting(finder, getName()+"eventIndexing", 0);	
	protected Setting<Integer> eventMigration = new IntegerCachedSetting(finder, getName()+"eventMigration", 0);	
	protected Setting<Integer> saveActiveListingRecords = new IntegerCachedSetting(finder, getName()+"saveActiveListingRecords", 1);	
	protected Setting<Integer> fraudAnalysis = new IntegerCachedSetting(finder, getName()+"fraudAnalysis", 1);
	protected Setting<Integer> listingDupe = new IntegerCachedSetting(finder, getName()+"listingDupe", 1);
	
	@Inject
	public ThreadCountSettings(ClusterSettingFinder finder) {
		super(finder, "job.joblet.threadCount.", "job.joblet.");
		register(geocode);
		register(areaLookup);
		register(viewRendering);
		register(dailyPricingStats);
		register(deletion);
		register(deactivation);
		register(feed);
		register(imageCaching);
		register(monthlyPricingStats);
		register(areaBorderRendering);
		register(areaRelationshipLookup);
		register(areaListingsLookup);
		register(dailyListingEventAggregation);
		register(dailyListingTypeEventAggregation);
		register(dailyEventAggregation);
		register(dailyAreaEventAggregation);
		register(dailyCompanyEventAggregation);
		register(dailyFeedEventAggregation);
		register(dailyRefEventAggregation);
		register(eventIndexing);
		register(eventMigration);
		register(saveActiveListingRecords);
		register(fraudAnalysis);
		register(listingDupe);
	}

	
	public Integer getThreadCountForJobletType(JobletType type){
		switch(type){
		case AreaLookupJoblet : 
			return getAreaLookup().getValue();
		case FeedImportJoblet : 
			return getFeed().getValue();
		case ListingDeletionJoblet : 
			return getDeletion().getValue();
		case ListingDeactivationJoblet : 
			return getDeactivation().getValue();
		case ListingViewRenderingJoblet : 
			return getViewRendering().getValue();
		case GeocodingJoblet : 
			return getGeocode().getValue();
		case ListingDupeJoblet : 
			return getListingDupe().getValue();
		case DailyPricingStatsJoblet : 
			return getDailyPricingStats().getValue();	
		case MonthlyPricingStatsJoblet : 
			return getMonthlyPricingStats().getValue();
		case ImageCachingJoblet : 
			return getImageCaching().getValue();
		case AreaBorderRendering : 
			return getAreaBorderRendering().getValue();
		case AreaRelationshipLookup : 
			return getAreaRelationshipLookup().getValue();
		case AreaListingsLookup : 
			return getAreaListingsLookup().getValue();
		case SaveActiveListingRecordsJoblet :
			return getSaveActiveListingRecords().getValue();
		case FraudAnalysisJoblet :
			return getFraudAnalysis().getValue();
			
		case EventMigration : 
			return getEventMigration().getValue();
		case EventIndexing : 
			return getEventIndexing().getValue();
			
		//don't run multiple aggregators of the same type in parallel
		case DailyEventAggregation : 
			return capXatY(getDailyEventAggregation().getValue(), 1);
		case DailyAreaEventAggregation : 
			return capXatY(getDailyAreaEventAggregation().getValue(), 1);
		case DailyCompanyEventAggregation : 
			return capXatY(getDailyCompanyEventAggregation().getValue(), 1);
		case DailyFeedEventAggregation : 
			return capXatY(getDailyFeedEventAggregation().getValue(), 1);
		case DailyListingEventAggregation : 
			return capXatY(getDailyListingEventAggregation().getValue(), 1);
		case DailyListingTypeEventAggregation : 
			return capXatY(getDailyListingTypeEventAggregation().getValue(), 1);
		case DailyRefEventAggregation : 
			return capXatY(getDailyRefEventAggregation().getValue(), 1);
		}
		
		throw new IllegalArgumentException(type.getVarName() +" is missing numThreads");
	}
	
	protected Integer capXatY(Integer x, Integer y){
		if(x==null || y==null){ return x; }
		if(x > y){ return y; }
		return x;
	}
	
	/*******************leaf getters*******************/

	public Setting<Integer> getGeocode() {
		return geocode;
	}
	
	public Setting<Integer> getAreaLookup() {
		return areaLookup;
	}
	
	public Setting<Integer> getViewRendering() {
		return viewRendering;
	}
	
	public Setting<Integer> getDailyPricingStats() {
		return dailyPricingStats;
	}
	
	public Setting<Integer> getDeletion() {
		return deletion;
	}
	
	public Setting<Integer> getDeactivation() {
		return deactivation;
	}
	
	public Setting<Integer> getFeed() {
		return feed;
	}
	
	public Setting<Integer> getImageCaching() {
		return imageCaching;
	}
	
	public Setting<Integer> getMonthlyPricingStats() {
		return monthlyPricingStats;
	}
	
	public Setting<Integer> getAreaBorderRendering() {
		return areaBorderRendering;
	}
	
	public Setting<Integer> getAreaRelationshipLookup() {
		return areaRelationshipLookup;
	}
	
	public Setting<Integer> getAreaListingsLookup() {
		return areaListingsLookup;
	}
	
	public Setting<Integer> getDailyListingEventAggregation() {
		return dailyListingEventAggregation;
	}
	
	public Setting<Integer> getDailyListingTypeEventAggregation() {
		return dailyListingTypeEventAggregation;
	}
	
	public Setting<Integer> getDailyEventAggregation() {
		return dailyEventAggregation;
	}
	
	public Setting<Integer> getDailyAreaEventAggregation() {
		return dailyAreaEventAggregation;
	}	
	
	public Setting<Integer> getDailyCompanyEventAggregation() {
		return dailyCompanyEventAggregation;
	}	
	
	public Setting<Integer> getDailyFeedEventAggregation() {
		return dailyFeedEventAggregation;
	}
	
	public Setting<Integer> getDailyRefEventAggregation() {
		return dailyRefEventAggregation;
	}
	
	public Setting<Integer> getEventIndexing() {
		return eventIndexing;
	}
	
	public Setting<Integer> getEventMigration() {
		return eventMigration;
	}
	
	public Setting<Integer> getSaveActiveListingRecords() {
		return saveActiveListingRecords;
	}
	
	public Setting<Integer> getFraudAnalysis() {
		return fraudAnalysis;
	}
	
	public Setting<Integer> getListingDupe(){
		return listingDupe;
	}
	
	
	/*******************tests*******************/

	public static class ThreadCountSettingsTester{
		@Test public void testInjection(){
			Injector injector = Guice.createInjector(new ServiceModule());
			ThreadCountSettings settings = injector.getInstance(ThreadCountSettings.class);
			System.out.println("Test getAreaBorderRendering() value : "+settings.getAreaBorderRendering().getValue());
			System.out.println("Test getAreaListingsLookup() value : "+settings.getAreaListingsLookup().getValue());
			System.out.println("Test getAreaLookup() value : "+settings.getAreaLookup().getValue());
			System.out.println("Test getAreaRelationshipLookup() value : "+settings.getAreaRelationshipLookup().getValue());
			System.out.println("Test getDailyAreaEventAggregation() value : "
					+settings.getDailyAreaEventAggregation().getValue());
			System.out.println("Test getDailyCompanyEventAggregation() value : "
					+settings.getDailyCompanyEventAggregation().getValue());
			System.out.println("Test getDailyEventAggregation() value : "
					+settings.getDailyEventAggregation().getValue());
			System.out.println("Test getDailyFeedEventAggregation() value : "
					+settings.getDailyFeedEventAggregation().getValue());
			System.out.println("Test getDailyListingEventAggregation() value : "
					+settings.getDailyListingEventAggregation().getValue());
			System.out.println("Test getDailyListingTypeEventAggregation() value : "
			+settings.getDailyListingTypeEventAggregation().getValue());
			System.out.println("Test getDailyPricingStats() value : "+settings.getDailyPricingStats().getValue());
			System.out.println("Test getDailyRefEventAggregation() value : "
					+settings.getDailyRefEventAggregation().getValue());
			System.out.println("Test getDeactivation() value : "+settings.getDeactivation().getValue());
			System.out.println("Test getDeletion() value : "+settings.getDeletion().getValue());
			System.out.println("Test getEventIndexing() value : "+settings.getEventIndexing().getValue());
			System.out.println("Test getEventMigration() value : "+settings.getEventMigration().getValue());
			System.out.println("Test getFeed() value : "+settings.getFeed().getValue());
			System.out.println("Test getFraudAnalysis() value : "+settings.getFraudAnalysis().getValue());
			System.out.println("Test getGeoCode() value : "+settings.getGeocode().getValue());
			System.out.println("Test getImageCaching() value : "+settings.getImageCaching().getValue());
			System.out.println("Test getListingDupe() value : "+settings.getListingDupe().getValue());
			System.out.println("Test getMonthlyPricingStats() value : "+settings.getMonthlyPricingStats().getValue());
			System.out.println("Test getSaveActiveListingRecords() value : "
					+settings.getSaveActiveListingRecords().getValue());
			System.out.println("Test getViewRendering() value : "+settings.getViewRendering().getValue());
		}
	}
	
}
