package com.hotpads.datarouter.storage.stream;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class StreamRecord<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> extends BaseStreamRecord<PK,D>{

	private final D databean;

	public StreamRecord(String sequenceNumber, Date approximateArrivalTimestamp, D databean){
		super(sequenceNumber, approximateArrivalTimestamp);
		this.databean = databean;
	}

	public D getDatabean(){
		return databean;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> List<D> getDatabeans(
			Collection<StreamRecord<PK,D>> records){
		return records.stream().map(StreamRecord::getDatabean).collect(Collectors.toList());
	}

}