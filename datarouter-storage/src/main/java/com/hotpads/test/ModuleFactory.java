package com.hotpads.test;

import java.util.ArrayList;
import java.util.List;

import org.testng.IModuleFactory;
import org.testng.ITestContext;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class ModuleFactory implements IModuleFactory{

	private Iterable<? extends Module> modules;

	public ModuleFactory(Iterable<? extends Module> modules){
		this.modules = modules;
	}

	@Override
	public Module createModule(ITestContext context, Class<?> testClass){

		Module globalModule = new AbstractModule(){

			@Override
			protected void configure(){
				for(Module module : modules){
					install(module);
				}
			}

		};
		Iterable<? extends Module> overriders = getOverriders();
		return Modules.override(globalModule).with(overriders);
	}

	protected List<Module> getOverriders(){
		return new ArrayList<>();
	}

}
