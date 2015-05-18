package com.hotpads.datarouter.client.imp.jdbc;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.field.StandardJdbcFieldCodecFactory;
import com.hotpads.datarouter.test.DatarouterTestGuiceModule;
import com.hotpads.test.ModuleFactory;

public class TestDatarouterJdbcGuiceModule extends AbstractModule{
	private static final Logger logger = LoggerFactory.getLogger(TestDatarouterJdbcGuiceModule.class);
	
	@Override
	protected void configure(){
		System.out.println("binding StandardJdbcFieldCodecFactory");
		bind(JdbcFieldCodecFactory.class).to(StandardJdbcFieldCodecFactory.class);
		install(new DatarouterTestGuiceModule());
	}
	
	public static class TestDatarouterJdbcModuleFactory extends ModuleFactory{
		public TestDatarouterJdbcModuleFactory(){
			super(Collections.singletonList(new TestDatarouterJdbcGuiceModule()));
		}

//		@Override
//		protected List<Module> getOverriders(){
//			List<Module> modules = super.getOverriders();
//			modules.add(new DatarouterTestGuiceModule());
//			return modules;
//		}
	}
}
