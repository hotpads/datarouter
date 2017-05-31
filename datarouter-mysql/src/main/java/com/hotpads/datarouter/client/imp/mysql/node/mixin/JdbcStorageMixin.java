package com.hotpads.datarouter.client.imp.mysql.node.mixin;

import com.hotpads.datarouter.client.imp.mysql.field.codec.factory.JdbcFieldCodecFactory;

public interface JdbcStorageMixin{

	String getTraceName(String opName);
	JdbcFieldCodecFactory getFieldCodecFactory();

}
