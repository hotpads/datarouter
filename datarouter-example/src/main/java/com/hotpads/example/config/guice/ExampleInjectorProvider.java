package com.hotpads.example.config.guice;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Module;
import com.google.inject.Stage;
import com.hotpads.datarouter.config.DatarouterGuiceModule;
import com.hotpads.datarouter.config.InjectorProvider;

public class ExampleInjectorProvider extends InjectorProvider{

	public ExampleInjectorProvider(Stage stage){
		super(stage, getModules());
	}

	private static List<Module> getModules(){
		List<Module> modules = new ArrayList<>();
		modules.add(new DatarouterGuiceModule());
		modules.add(new ExampleGuiceModule());
		return modules;
	}

}
