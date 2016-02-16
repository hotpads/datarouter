package com.hotpads.datarouter.client.imp.sqs.group.op;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.hotpads.datarouter.client.imp.sqs.BaseSqsNode;
import com.hotpads.datarouter.client.imp.sqs.SqsDataTooLargeException;
import com.hotpads.datarouter.client.imp.sqs.op.SqsOp;
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

	private final Collection<D> databeans;
	private final byte[] collectionPrefix;
	private final byte[] collectionSeparator;
	private final byte[] collectionSuffix;

	public SqsGroupPutMultiOp(Collection<D> databeans, Config config, BaseSqsNode<PK,D,F> sqsNode){
		super(config, sqsNode);
		this.databeans = databeans;
		this.collectionPrefix = StringByteTool.getUtf8Bytes(codec.getCollectionPrefix());
		this.collectionSeparator = StringByteTool.getUtf8Bytes(codec.getCollectionSeparator());
		this.collectionSuffix = StringByteTool.getUtf8Bytes(codec.getCollectionSuffix());
	}

	@Override
	protected Void run(){
		List<D> rejectedDatabeans = new ArrayList<>();
		ByteArrayOutputStream databeanGroup = new ByteArrayOutputStream();
		databeanGroup.write(collectionPrefix, 0, collectionPrefix.length);
		for(D databean : databeans){
			byte[] encodedDatabean = StringByteTool.getUtf8Bytes(codec.toString(databean, fielder));
			if(encodedDatabean.length + 2*collectionPrefix.length > BaseSqsNode.MAX_BYTES_PER_MESSAGE){
				rejectedDatabeans.add(databean);
				continue;
			}
			addToQueueAndFlushIfNecessary(databeanGroup, encodedDatabean);
		}
		flush(databeanGroup);
		if(rejectedDatabeans.size() > 0){
			throw new SqsDataTooLargeException().withRejectedDatabeans(rejectedDatabeans);
		}
		return null;
	}

	private void addToQueueAndFlushIfNecessary(ByteArrayOutputStream group, byte[] databean){
		if(group.size() + databean.length + collectionSuffix.length > BaseSqsNode.MAX_BYTES_PER_MESSAGE){
			flush(group);
			group.reset();
			group.write(collectionPrefix, 0, collectionPrefix.length);
		}
		if(group.size() > 1){
			group.write(collectionSeparator, 0, collectionSeparator.length);
		}
		group.write(databean, 0, databean.length);
	}

	private void flush(ByteArrayOutputStream group){
		group.write(collectionSuffix, 0, collectionSuffix.length);
		String stringGroup = StringByteTool.fromUtf8Bytes(group.toByteArray());
		SendMessageRequest request = new SendMessageRequest(queueUrl, stringGroup);
		amazonSqsClient.sendMessage(request);
	}
}
