package com.hotpads.datarouter.node.type.writebehind.base;

import java.util.concurrent.Future;

public class OutstandingWriteWrapper{
	private final Long created;
	public final Future<?> write;
	public final String opDesc;

	public OutstandingWriteWrapper(Long created, Future<?> write, String opDesc){
		this.created = created;
		this.write = write;
		this.opDesc = opDesc;
	}

	/********************** methods *************************************/

	public Long getAgeMs(){
		return System.currentTimeMillis() - created;
	}

}