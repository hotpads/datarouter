package com.hotpads.datarouter.client.imp;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings.AvailabilitySettingNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.util.core.DrComparableTool;

public abstract class BaseClient
implements Client{

	private final String name;
	private final AvailabilitySettingNode availability;

	public BaseClient(String name, ClientAvailabilitySettings clientAvailabilitySettings){
		this.name = name;
		this.availability = clientAvailabilitySettings.getAvailabilityForClientName(getName());
	}

	/**************************** standard ******************************/

	@Override
	public int compareTo(Client client){
		return DrComparableTool.nullFirstCompareTo(getName(), client.getName());
	}

	@Override
	public AvailabilitySettingNode getAvailability(){
		return availability;
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public Future<Optional<String>> notifyNodeRegistration(PhysicalNode<?,?> node){
		return CompletableFuture.completedFuture(Optional.empty());
	}

	@Override
	public String toString(){
		return name;
	}

}
