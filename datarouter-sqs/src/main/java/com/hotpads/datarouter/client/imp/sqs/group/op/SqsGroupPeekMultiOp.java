package com.hotpads.datarouter.client.imp.sqs.group.op;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.sqs.model.Message;
import com.hotpads.datarouter.client.imp.sqs.BaseSqsNode;
import com.hotpads.datarouter.client.imp.sqs.op.BaseSqsPeekMultiOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.GroupQueueMessage;
import com.hotpads.util.core.bytes.StringByteTool;

public class SqsGroupPeekMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseSqsPeekMultiOp<PK,D,F,GroupQueueMessage<PK,D>>{

	public SqsGroupPeekMultiOp(Config config, BaseSqsNode<PK,D,F> sqsNode){
		super(config, sqsNode);
	}

	@Override
	protected List<GroupQueueMessage<PK, D>> extractDatabeans(List<Message> messages){
		List<GroupQueueMessage<PK,D>> groupQueueMessages = new ArrayList<>();
		for(Message message : messages){
			List<D> databeans = codec.fromStringMulti(message.getBody(), fielder, databeanSupplier);
			byte[] receiptHandle = StringByteTool.getUtf8Bytes(message.getReceiptHandle());
			groupQueueMessages.add(new GroupQueueMessage<>(receiptHandle, databeans));
		}
		return groupQueueMessages;
	}
}
