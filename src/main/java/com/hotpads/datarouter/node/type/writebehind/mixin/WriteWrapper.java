package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;

public class WriteWrapper<T>{

	private String op;

	private Collection<T> objects;

	private Config config;

	public WriteWrapper(){
	}

	public WriteWrapper(String op, List<T> objects, Config config){
		this.op = op;
		this.objects = objects;
		this.config = config;
	}

	public WriteWrapper(String op, Collection<T> objects, Config config){
		this.op = op;
		this.objects = objects;
		this.config = config;
	}
	
	public void clear(){
		op = null;
		objects.clear();
		config = null;
	}

	public void setOp(String op){
		this.op = op;
	}

	public String getOp(){
		return op;
	}

	public Collection<T> getObjects(){
		return objects;
	}

	public Config getConfig(){
		return config;
	}

}
