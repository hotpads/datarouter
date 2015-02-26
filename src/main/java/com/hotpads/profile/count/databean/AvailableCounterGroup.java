package com.hotpads.profile.count.databean;

import java.util.HashSet;

import com.hotpads.datarouter.util.core.SetTool;

public class AvailableCounterGroup{

	private AvailableCounter availableCounter;
	private HashSet<String> servers;
	private String name;

	public AvailableCounterGroup(AvailableCounter availableCounter){
		super();
		this.availableCounter = availableCounter;
		this.name = availableCounter.getName();
		this.servers = SetTool.createHashSet();
		this.servers.add(availableCounter.getSource());
	}

	public AvailableCounterGroup(String availableCounterName){
		super();
		this.availableCounter = new AvailableCounter();
		this.availableCounter.setName(availableCounterName);
		this.servers = SetTool.createHashSet();
		this.servers.add(availableCounter.getSource());
	}


	public String getName(){
		return this.name;
	}

	public Long getPeriodMs(){
		return this.availableCounter.getPeriodMs();
	}

	public AvailableCounter getAvailableCounter(){
		return availableCounter;
	}

	public void setAvailableCounter(AvailableCounter availableCounter){
		this.availableCounter = availableCounter;
	}

	public HashSet<String> getServers(){
		return servers;
	}

	public void setServers(HashSet<String> servers){
		this.servers = servers;
	}

	public void addServers(HashSet<String> servers){
		this.servers.addAll(servers);
	}

	public void addServer(String servers){
		this.servers.add(servers);
	}

	@Override
	public boolean equals(Object obj){
		if(obj != null && obj instanceof AvailableCounterGroup){ return this.getName().toLowerCase().equals(
				((AvailableCounterGroup)obj).getName().toLowerCase()); }
		return false;
	}
}
