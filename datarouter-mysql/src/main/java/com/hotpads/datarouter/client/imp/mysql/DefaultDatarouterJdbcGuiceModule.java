package com.hotpads.datarouter.client.imp.mysql;

import com.google.inject.AbstractModule;
import com.hotpads.datarouter.client.imp.mysql.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.mysql.field.codec.factory.StandardJdbcFieldCodecFactory;

public class DefaultDatarouterJdbcGuiceModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(JdbcFieldCodecFactory.class).to(StandardJdbcFieldCodecFactory.class);
	}

}
