package com.hotpads.datarouter.client;

public interface Client 
extends Comparable<Client>{

	String getName();
	ClientType getType();
}
