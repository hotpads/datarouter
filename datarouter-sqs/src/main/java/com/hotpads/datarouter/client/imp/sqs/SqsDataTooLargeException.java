package com.hotpads.datarouter.client.imp.sqs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@SuppressWarnings("serial")
public class SqsDataTooLargeException extends DataAccessException{

	private Collection<Databean<?,?>> rejectedDatabeans;
	
	public SqsDataTooLargeException(Databean<?,?> rejectedDatabean){
		this();
		this.rejectedDatabeans = Collections.<Databean<?,?>>singleton(rejectedDatabean);
	}
	
	public SqsDataTooLargeException(){
		super("Some databeans were too large for SQS.");
	}
	
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>> 
	SqsDataTooLargeException withRejectedDatabeans(Collection<D> databeans){
		rejectedDatabeans = new ArrayList<>();
		for(D databean : databeans){
			rejectedDatabeans.add(databean);
		}
		return this;
	}
	
	public Collection<Databean<?,?>> getRejectedDatabeans(){
		return rejectedDatabeans;
	}
}
