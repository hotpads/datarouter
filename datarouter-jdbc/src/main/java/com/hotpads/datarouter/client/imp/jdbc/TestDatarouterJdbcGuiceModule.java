package com.hotpads.datarouter.client.imp.jdbc;

import java.util.Collections;

import com.google.inject.AbstractModule;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.StandardJdbcFieldCodecFactory;
import com.hotpads.datarouter.test.DatarouterTestGuiceModule;
import com.hotpads.test.ModuleFactory;

public class TestDatarouterJdbcGuiceModule extends AbstractModule{
	
	@Override
	protected void configure(){
		bind(JdbcFieldCodecFactory.class).to(StandardJdbcFieldCodecFactory.class);
		install(new DatarouterTestGuiceModule());
	}
	
	public static class TestDatarouterJdbcModuleFactory extends ModuleFactory{
		public TestDatarouterJdbcModuleFactory(){
			super(Collections.singletonList(new TestDatarouterJdbcGuiceModule()));
		}

	}
}
