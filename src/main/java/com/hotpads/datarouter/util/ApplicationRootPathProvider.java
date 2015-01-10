package com.hotpads.datarouter.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.BindingAnnotation;

public class ApplicationRootPathProvider implements Provider<String>{
	private static final Logger logger = LoggerFactory.getLogger(ApplicationRootPathProvider.class);
	

	@BindingAnnotation 
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD }) 
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ApplicationRootPath{}
	
	
	private String path;
	
	@Inject
	public ApplicationRootPathProvider(@Nullable ServletContext servletContext){
		this.path = WebappTool.getApplicationRootPath(servletContext);//null SC ok
		System.out.println(getClass() + " ApplicationRootPath: "+path);
	}
//	@Inject
//	public ApplicationRootPathProvider(){
//		this.path = WebappTool.getApplicationRootPath(null);//null SC ok
//		logger.warn("ApplicationRootPath: "+path);
//		System.out.println("syso provider ApplicationRootPath: "+path);
//	}
	
	@Override
	public String get(){
		return path;
	}
}
