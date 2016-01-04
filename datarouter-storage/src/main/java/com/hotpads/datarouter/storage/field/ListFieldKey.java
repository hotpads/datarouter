package com.hotpads.datarouter.storage.field;

import java.util.List;

public class ListFieldKey<V extends Comparable<V>,L extends List<V>>
extends BaseFieldKey<L>{

	public ListFieldKey(String name){
		super(name);
	}

	@Override
	public boolean isCollection(){
		return true;
	}

}