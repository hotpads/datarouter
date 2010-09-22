/**
 * 
 */
package com.hotpads.datarouter.node.base.writebehind;

import java.util.concurrent.Future;

public class OutstandingWriteWrapper{
	protected Long created;
	protected Future<?> write;
	
	public OutstandingWriteWrapper(Long created, Future<?> write){
		this.created = created;
		this.write = write;
	}
	
	/********************** methods *************************************/
	
	public Long getAgeMs(){
		return System.currentTimeMillis() - created;
	}
	
	
	/************************* get/set *********************************/
	
	public Long getCreated(){
		return created;
	}
	public Future<?> getWrite(){
		return write;
	}
}