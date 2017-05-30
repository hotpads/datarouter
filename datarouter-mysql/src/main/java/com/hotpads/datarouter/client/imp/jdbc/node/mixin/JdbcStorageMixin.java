package com.hotpads.datarouter.client.imp.jdbc.node.mixin;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;

public interface JdbcStorageMixin{

	String getTraceName(String opName);
	JdbcFieldCodecFactory getFieldCodecFactory();

}
