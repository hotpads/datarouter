package com.hotpads.datarouter.client;

import java.util.Collection;
import java.util.List;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class ClientId{

	protected String name;
	protected Boolean writable;
	
	public ClientId(String name, Boolean writable){
		this.name = name;
		this.writable = writable;
	}
	
	public static List<String> getNames(Collection<ClientId> ids){
		List<String> names = ListTool.createArrayListWithSize(ids);
		for(ClientId id : CollectionTool.nullSafe(ids)){
			names.add(id.name);
		}
		return names;
	}
	
	public static List<String> getWritableNames(Collection<ClientId> ids){
		List<String> names = ListTool.createArrayListWithSize(ids);
		for(ClientId id : CollectionTool.nullSafe(ids)){
			if(id.writable){ names.add(id.name); }
		}
		return names;
	}
	
}
