package com.hotpads.datarouter.client.imp.sqs.single.op;

import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.hotpads.datarouter.client.imp.sqs.BaseSqsNode;
import com.hotpads.datarouter.client.imp.sqs.SqsDataTooLargeException;
import com.hotpads.datarouter.client.imp.sqs.op.SqsOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.generation.FieldGeneratorTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.bytes.StringByteTool;

public class SqsPutOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SqsOp<PK,D,F,Void>{

	private final D databean;

	public SqsPutOp(D databean, Config config, BaseSqsNode<PK,D,F> sqsNode){
		super(config, sqsNode);
		this.databean = databean;
	}

	@Override
	protected Void run(){
		FieldGeneratorTool.optFieldToGenerate(fieldInfo, databean).ifPresent(field -> FieldGeneratorTool
				.generateAndSetValueForField(fieldInfo, databean, field, bean -> true));
		String encodedDatabean = codec.toString(databean, fielder);
		if(StringByteTool.getUtf8Bytes(encodedDatabean).length > BaseSqsNode.MAX_BYTES_PER_MESSAGE){
			throw new SqsDataTooLargeException(databean);
		}
		SendMessageRequest request = new SendMessageRequest(queueUrl, encodedDatabean);
		amazonSqsClient.sendMessage(request);
		return null;
	}

}
