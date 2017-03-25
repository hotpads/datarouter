package com.hotpads.datarouter.config.strategy;

import java.util.concurrent.Callable;

public class NoneConfigStrategy implements Callable<Void>{

	@Override
	public Void call(){
		return null;
	}

}
