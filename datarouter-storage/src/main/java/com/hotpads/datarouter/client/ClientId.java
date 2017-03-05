package com.hotpads.datarouter.client;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;

public class ClientId implements Comparable<ClientId>{

	private String name;
	private boolean writable;
	private boolean disableable;

	public ClientId(String name, boolean writable){
		this(name, writable, true);
	}

	public ClientId(String name, boolean writable, boolean disableable){
		this.name = name;
		this.writable = writable;
		this.disableable = disableable;
	}

	@Override
	public int hashCode(){
		return Objects.hash(name, writable, disableable);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(!(obj instanceof ClientId)){
			return false;
		}
		ClientId other = (ClientId)obj;
		if(!name.equals(other.name)){
			return false;
		}
		if(writable != other.writable){
			return false;
		}
		if(disableable != other.disableable){
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(ClientId other){
		int diff = name.compareTo(other.name);
		if(diff != 0){
			return diff;
		}
		diff = Boolean.compare(writable, other.writable);
		if(diff != 0){
			return diff;
		}
		return Boolean.compare(disableable, disableable);
	}

	@Override
	public String toString(){
		return "ClientId[" + name + "," + writable + "," + disableable + "]";
	}

	public static List<String> getNames(Collection<ClientId> ids){
		List<String> names = DrListTool.createArrayListWithSize(ids);
		for(ClientId id : DrCollectionTool.nullSafe(ids)){
			names.add(id.name);
		}
		return names;
	}

	public static List<String> getWritableNames(Collection<ClientId> ids){
		List<String> names = DrListTool.createArrayListWithSize(ids);
		for(ClientId id : DrCollectionTool.nullSafe(ids)){
			if(id.writable){
				names.add(id.name);
			}
		}
		return names;
	}

	public String getName(){
		return name;
	}

	public boolean getWritable(){
		return writable;
	}

	public boolean getDisableable(){
		return disableable;
	}

}
