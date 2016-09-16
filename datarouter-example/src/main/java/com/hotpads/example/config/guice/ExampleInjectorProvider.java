package com.hotpads.example.config.guice;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Module;
import com.google.inject.Stage;
import com.hotpads.datarouter.config.DatarouterCoreGuiceModule;
import com.hotpads.datarouter.config.InjectorProvider;
import com.hotpads.notification.config.guice.DatarouterNotificationGuiceModule;

public class ExampleInjectorProvider extends InjectorProvider{

	public ExampleInjectorProvider(Stage stage){
		super(stage, getModules());
	}

	private static List<Module> getModules(){
		List<Module> modules = new ArrayList<>();
		modules.add(new DatarouterCoreGuiceModule());
		modules.add(new DatarouterNotificationGuiceModule());
		modules.add(new ExampleGuiceModule());
		modules.add(new ExampleWebGuiceModule());
		return modules;
	}

}
