package com.hotpads.datarouter.client.imp.sqs.op;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.hotpads.datarouter.client.imp.sqs.group.SqsGroupNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.queue.QueueMessageKey;
import com.hotpads.util.core.bytes.StringByteTool;

public class SqsAckOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SqsOp<PK,D,F,Void>{

	private final QueueMessageKey key;

	public SqsAckOp(QueueMessageKey key, Config config, SqsGroupNode<PK,D,F> sqsNode){
		super(config, sqsNode);
		this.key = key;
	}

	@Override
	protected Void run(){
		String handle = StringByteTool.fromUtf8Bytes(key.getHandle());
		DeleteMessageRequest deleteRequest = new DeleteMessageRequest(queueUrl, handle);
		amazonSqsClient.deleteMessage(deleteRequest);
		return null;
	}

}
