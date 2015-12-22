package com.hotpads.profile.count.collection.predicate;

import com.google.common.base.Predicate;
import com.hotpads.profile.count.databean.AvailableCounterGroup;

public class AnalyticsAvailableCounterGroupPredicate implements Predicate<AvailableCounterGroup>{

	private String name;

	public AnalyticsAvailableCounterGroupPredicate(String availableCounterGroupName){
		this.setName(availableCounterGroupName);
	}

	@Override
	public boolean apply(AvailableCounterGroup input){
		return input.getName().toLowerCase().equals(name.toLowerCase());
	}

	/**
	 * @return the name
	 */
	public String getName(){
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name){
		this.name = name;
	}

}
