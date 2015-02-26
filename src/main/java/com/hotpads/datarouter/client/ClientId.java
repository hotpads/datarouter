package com.hotpads.datarouter.client;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.util.core.CollectionTool;
import com.hotpads.datarouter.util.core.ComparableTool;
import com.hotpads.datarouter.util.core.ListTool;

public class ClientId implements Comparable<ClientId>{

	protected String name;
	protected Boolean writable;
	
	public ClientId(String name, Boolean writable){
		if(name==null){ throw new IllegalArgumentException("name cannot be null"); }
		this.name = name;
		this.writable = writable;
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null)?0:name.hashCode());
		result = prime * result + ((writable == null)?0:writable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof ClientId)) return false;
		ClientId other = (ClientId)obj;
		if(name == null){
			if(other.name != null) return false;
		}else if(!name.equals(other.name)) return false;
		if(writable == null){
			if(other.writable != null) return false;
		}else if(!writable.equals(other.writable)) return false;
		return true;
	}
	
	@Override
	public int compareTo(ClientId other){
		int c = ComparableTool.nullFirstCompareTo(name, other.name);
		if(c!=0) { return c; }
		return ComparableTool.nullFirstCompareTo(writable, other.writable);
	}
	
	@Override
	public String toString(){
		return "ClientId["+name+","+writable+"]";
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

	public String getName(){
		return name;
	}

	public Boolean getWritable(){
		return writable;
	}
	
	
}
