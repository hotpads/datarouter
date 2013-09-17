package com.hotpads.profile.count.collection.predicate;

import com.google.common.base.Predicate;
import com.hotpads.profile.count.databean.Count;

public class FilterCountByServer implements Predicate<Count>{

	private String server;

	/**
	 * @param server
	 */
	public FilterCountByServer(String server){
		this.server = server;
	}


	@Override
	public boolean apply(Count input){
		if(server.toLowerCase().equals("all")){
			return true;
		}
	return input.getSource().toLowerCase().equals(server.toLowerCase());
	}


	public String getServer(){
		return server;
	}

	public void setServer(String server){
		this.server = server;
	}

}
