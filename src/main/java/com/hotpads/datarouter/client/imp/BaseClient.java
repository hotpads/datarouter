package com.hotpads.datarouter.client.imp;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.util.core.DrComparableTool;

public abstract class BaseClient
implements Client{
	
	/**************************** standard ******************************/
	
	@Override
	public int compareTo(Client o){
		return DrComparableTool.nullFirstCompareTo(getName(), o.getName());
	}
}
