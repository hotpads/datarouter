package com.hotpads.datarouter.client.imp.jdbc;

import java.util.Collections;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.field.StandardJdbcFieldCodecFactory;
import com.hotpads.datarouter.config.DatarouterGuiceModule;
import com.hotpads.datarouter.test.DatarouterTestGuiceModule;
import com.hotpads.test.ModuleFactory;

public class DatarouterJdbcGuiceModule extends AbstractModule{

	@Override
	protected void configure(){
		install(new DatarouterGuiceModule());
		
		bind(JdbcFieldCodecFactory.class).to(StandardJdbcFieldCodecFactory.class);
	}
	
	public static class DatarouterJdbcModuleFactory extends ModuleFactory{
		public DatarouterJdbcModuleFactory(){
			super(Collections.singletonList(new DatarouterJdbcGuiceModule()));
		}

		@Override
		protected List<Module> getOverriders(){
			List<Module> modules = super.getOverriders();
			modules.add(new DatarouterTestGuiceModule());
			return modules;
		}
	}
}
