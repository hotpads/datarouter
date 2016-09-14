package com.hotpads.datarouter.storage.stream;

public class StreamRecordKey{
	private final String sequenceNumber;

	public StreamRecordKey(String sequenceNumber){
		this.sequenceNumber = sequenceNumber;
	}

	public String getSequenceNumber(){
		return sequenceNumber;
	}

}
