package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;

public class WriteWrapper<T>{

	private String op;
	private Collection<T> objects;
	private Config config;

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

	public WriteWrapper(WriteWrapper<T> writeWrapper){
		this.op = writeWrapper.op;
		this.objects = new ArrayList<>(writeWrapper.objects);
		this.config = writeWrapper.config;
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
