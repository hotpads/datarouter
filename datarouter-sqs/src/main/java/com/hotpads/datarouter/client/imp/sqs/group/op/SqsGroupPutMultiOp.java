package com.hotpads.datarouter.client.imp.sqs.group.op;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.hotpads.datarouter.client.imp.sqs.BaseSqsNode;
import com.hotpads.datarouter.client.imp.sqs.SqsDataTooLargeException;
import com.hotpads.datarouter.client.imp.sqs.single.SqsNode;
import com.hotpads.datarouter.client.imp.sqs.single.op.SqsOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.bytes.StringByteTool;

public class SqsGroupPutMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SqsOp<PK,D,F,Void>{

	private Collection<D> databeans;

	public SqsGroupPutMultiOp(Collection<D> databeans, Config config, BaseSqsNode<PK,D,F> sqsNode){
		super(config, sqsNode);
		this.databeans = databeans;
	}

	@Override
	protected Void run(){
		List<D> rejectedDatabeans = new ArrayList<>();
		List<D> databeanGroup = new ArrayList<>();
		for(D databean : databeans){
			String encodedDatabean = codec.toString(databean, fielder);
			if(StringByteTool.getUtf8Bytes(encodedDatabean).length > SqsNode.MAX_BYTES_PER_MESSAGE){
				rejectedDatabeans.add(databean);
				continue;
			}
			databeanGroup = addOrPutGroup(databeanGroup, databean);
		}
		putGroup(databeanGroup);
		if(rejectedDatabeans.size() > 0){
			throw new SqsDataTooLargeException().withRejectedDatabeans(rejectedDatabeans);
		}
		return null;
	}
	
	private List<D> addOrPutGroup(List<D> group, D databean){
		List<D> newGroup = new ArrayList<>(group);
		newGroup.add(databean);
		String encodedNewGroup = codec.toStringMulti(newGroup, fielder);
		if(StringByteTool.getUtf8Bytes(encodedNewGroup).length > SqsNode.MAX_BYTES_PER_MESSAGE){
			putGroup(group);
			return Arrays.asList(databean);
		}
		return newGroup;
	}

	private void putGroup(List<D> databeans){
		String encodedDatabeans = codec.toStringMulti(databeans, fielder);
		SendMessageRequest request = new SendMessageRequest(queueUrl, encodedDatabeans);
		amazonSqsClient.sendMessage(request);
	}
}
