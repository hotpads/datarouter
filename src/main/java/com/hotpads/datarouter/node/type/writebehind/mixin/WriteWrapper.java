package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.util.core.ListTool;

public class WriteWrapper<T>{

	private String op;

	private Collection<T> objects;

	private Config config;

	public WriteWrapper(){
		this.objects = ListTool.createLinkedList();
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

	@Override
	public String toString(){
		return "WriteWrapper {hashCode=" + Integer.toHexString(hashCode()) + "}[op=" + op + ", objectsNb="
				+ objects.size() + ", config=" + config + "]";
	}

}
