package com.hotpads.datarouter.storage.stream;

import java.util.Date;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class BaseStreamRecord <PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{
	private final StreamRecordKey key;
	private final Date approximateArrivalTimestamp;

	public BaseStreamRecord(String sequenceNumber, Date approximateArrivalTimestamp){
		this.key = new StreamRecordKey(sequenceNumber);
		this.approximateArrivalTimestamp = approximateArrivalTimestamp;
	}

	public StreamRecordKey getKey(){
		return key;
	}

	public Date getApproximateArrivalTimestamp(){
		return approximateArrivalTimestamp;
	}

}
