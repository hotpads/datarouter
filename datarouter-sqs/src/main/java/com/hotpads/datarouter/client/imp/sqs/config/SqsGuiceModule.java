package com.hotpads.datarouter.client.imp.sqs.config;

import com.google.inject.AbstractModule;
import com.hotpads.datarouter.client.imp.sqs.SqsClientType;
import com.hotpads.datarouter.client.imp.sqs.encode.JsonSqsEncoder;
import com.hotpads.datarouter.client.imp.sqs.encode.SqsEncoder;

public class SqsGuiceModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(SqsEncoder.class).to(JsonSqsEncoder.class);
		bind(SqsClientType.class);
	}
}