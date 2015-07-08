package com.hotpads.datarouter.client.imp.sqs.single.op;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.sqs.model.Message;
import com.hotpads.datarouter.client.imp.sqs.BaseSqsNode;
import com.hotpads.datarouter.client.imp.sqs.op.BaseSqsPeekMultiOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessage;
import com.hotpads.util.core.bytes.StringByteTool;

public class SqsPeekMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>> 
extends BaseSqsPeekMultiOp<PK,D,F,QueueMessage<PK,D>>{

	public SqsPeekMultiOp(Config config, BaseSqsNode<PK,D,F> sqsNode){
		super(config, sqsNode);
	}
	
	@Override
	protected List<QueueMessage<PK, D>> extractDatabeans(List<Message> messages){
		List<QueueMessage<PK,D>> results = new ArrayList<>(messages.size());
		for(Message message : messages){
			D databean = codec.fromString(message.getBody(), fielder, databeanType);
			byte[] receiptHandle = StringByteTool.getUtf8Bytes(message.getReceiptHandle());
			results.add(new QueueMessage<>(receiptHandle , databean));
		}
		return results;
	}

}
