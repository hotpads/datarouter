package com.hotpads.datarouter.storage.queue;

public class QueueMessageKey{

	private byte[] handle;
	
	public QueueMessageKey(byte[] handle){
		this.handle = handle;
	}
	
	public byte[] getHandle(){
		return handle;
	}
}
