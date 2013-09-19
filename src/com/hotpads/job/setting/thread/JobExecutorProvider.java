package com.hotpads.job.setting.thread;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Provider;

import com.google.inject.BindingAnnotation;



/*********************** Guice providers *****************************************/

public class JobExecutorProvider implements Provider<ScheduledExecutorService>{
	
	@BindingAnnotation 
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD }) 
	@Retention(RetentionPolicy.RUNTIME)
	public @interface JobExecutor {}
	
    @Override
    public ScheduledExecutorService get() {
    	return JobExecutors.jobExecutor;
    }
}