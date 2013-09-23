package com.hotpads.job.config.setting.job;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.job.setting.ClusterSettingFinder;
import com.hotpads.job.setting.Setting;
import com.hotpads.job.setting.SettingNode;
import com.hotpads.job.setting.cached.imp.BooleanCachedSetting;

@Singleton
public class EventSettings extends SettingNode{
	
	protected Setting<Boolean> aggregateEvents = new BooleanCachedSetting(finder, 
			getName()+"aggregateEvents", false);		
	protected Setting<Boolean> aggregateListingTypeEvents = new BooleanCachedSetting(finder, 
			getName()+"aggregateListingTypeEvents", false);
	protected Setting<Boolean> aggregateListingEvents = new BooleanCachedSetting(finder, 
			getName()+"aggregateListingEvents", false);
	protected Setting<Boolean> aggregateAreaEvents = new BooleanCachedSetting(finder, 
			getName()+"aggregateAreaEvents", false);
	protected Setting<Boolean> aggregateCompanyEvents = new BooleanCachedSetting(finder, 
			getName()+"aggregateCompanyEvents", false);
	protected Setting<Boolean> aggregateFeedEvents = new BooleanCachedSetting(finder, 
			getName()+"aggregateFeedEvents", false);
	protected Setting<Boolean> aggregateMonthlyEvents = new BooleanCachedSetting(finder, 
			getName()+"aggregateMonthlyEvents", false);
	protected Setting<Boolean> aggregateDisplayedListingGroups = new BooleanCachedSetting(finder, 
			getName()+"aggregateDisplayedListingGroups", false);
	protected Setting<Boolean> aggregateRefEvents = new BooleanCachedSetting(finder, 
			getName()+"aggregateRefEvents", false);
	protected Setting<Boolean> createSaveActiveListingRecordJoblets = new BooleanCachedSetting(finder, 
			getName()+"createSaveActiveListingRecordJoblets", false);
	protected Setting<Boolean> saveEvents = new BooleanCachedSetting(finder, 
			getName()+"saveEvents", true);
	protected Setting<Boolean> sqsEventPersister = new BooleanCachedSetting(finder, 
			getName()+"sqsEventPersister", false);
	
	@Inject
	public EventSettings(ClusterSettingFinder finder) {
		super(finder, "job.event.", "job.");
		register(aggregateEvents);	
		register(aggregateListingTypeEvents);	
		register(aggregateListingEvents);	
		register(aggregateAreaEvents);	
		register(aggregateCompanyEvents);	
		register(aggregateFeedEvents);	
		register(aggregateMonthlyEvents);	
		register(aggregateDisplayedListingGroups);	
		register(aggregateRefEvents);	
		register(createSaveActiveListingRecordJoblets);	
		register(saveEvents);
		register(sqsEventPersister);
	}
	
	
	/******************* get/set**************************/

	public Setting<Boolean> getAggregateEvents(){
		return aggregateEvents;
	}

	public Setting<Boolean> getAggregateListingTypeEvents(){
		return aggregateListingTypeEvents;
	}

	public Setting<Boolean> getAggregateListingEvents(){
		return aggregateListingEvents;
	}

	public Setting<Boolean> getAggregateAreaEvents(){
		return aggregateAreaEvents;
	}

	public Setting<Boolean> getAggregateCompanyEvents(){
		return aggregateCompanyEvents;
	}

	public Setting<Boolean> getAggregateFeedEvents(){
		return aggregateFeedEvents;
	}

	public Setting<Boolean> getAggregateMonthlyEvents(){
		return aggregateMonthlyEvents;
	}

	public Setting<Boolean> getAggregateDisplayedListingGroups(){
		return aggregateDisplayedListingGroups;
	}

	public Setting<Boolean> getAggregateRefEvents(){
		return aggregateRefEvents;
	}

	public Setting<Boolean> getCreateSaveActiveListingRecordJoblets(){
		return createSaveActiveListingRecordJoblets;
	}

	public Setting<Boolean> getSaveEvents(){
		return saveEvents;
	}

	public Setting<Boolean> getSQSEventPersister() {
		return sqsEventPersister;
	}
}
