package com.hotpads.datarouter.client.imp.kinesis.single.op;

import com.hotpads.datarouter.client.imp.kinesis.BaseKinesisNode;
import com.hotpads.datarouter.client.imp.kinesis.KinesisDataTooLargeException;
import com.hotpads.datarouter.client.imp.kinesis.op.KinesisOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.generation.FieldGeneratorTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class KinesisPutOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends KinesisOp<PK,D,F,Void>{

	private final D databean;

	public KinesisPutOp(D databean, Config config, BaseKinesisNode<PK,D,F> kinesisNode){
		super(config, kinesisNode);
		this.databean = databean;
	}

	@Override
	protected Void run(){
		FieldGeneratorTool.generateAndSetValueForFieldIfNecessary(fieldInfo, databean);
		String encodedDatabean = codec.toString(databean, fielder);
		if(StringByteTool.getUtf8Bytes(encodedDatabean).length > BaseKinesisNode.MAX_BYTES_PER_RECORD){
			throw new KinesisDataTooLargeException(databean);
		}
		//put here
		throw new NotImplementedException();
	}

}
