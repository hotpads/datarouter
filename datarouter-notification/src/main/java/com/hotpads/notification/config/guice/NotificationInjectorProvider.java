package com.hotpads.notification.config.guice;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Module;
import com.google.inject.Stage;
import com.hotpads.datarouter.config.DatarouterCoreGuiceModule;
import com.hotpads.datarouter.config.InjectorProvider;

public class NotificationInjectorProvider extends InjectorProvider{

	public NotificationInjectorProvider(Stage stage){
		super(stage, getModules());
	}

	private static List<Module> getModules(){
		List<Module> modules = new ArrayList<>();
		modules.add(new DatarouterCoreGuiceModule());
		modules.add(new NotificationGuiceModule());
		modules.add(new NotificationWebGuiceModule());
		return modules;
	}

}
