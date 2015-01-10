package com.hotpads.datarouter.util;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.ServletContext;

public class GuiceApplicationRootPath implements ApplicationRootPath{

	private String path;
	
	@Inject
	public GuiceApplicationRootPath(@Nullable ServletContext servletContext){
		this.path = WebappTool.getApplicationRootPath(servletContext);// null SC ok
		System.out.println(getClass() + " ApplicationRootPath: " + path);
	}
	
	@Override
	public String getPath(){
		return path;
	}


	/******************* provider ********************/
	
//	public static class ApplicationRootPathProvider implements Provider<ApplicationRootPath>{
//		private static final Logger logger = LoggerFactory.getLogger(ApplicationRootPathProvider.class);
//
////		@BindingAnnotation 
////		@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD}) 
////		@Retention(RetentionPolicy.RUNTIME) 
////		public @interface ApplicationRootPath{}
//
//		private String path;
//
//		@Inject
//		public ApplicationRootPathProvider(@Nullable ServletContext servletContext){
//			this.path = WebappTool.getApplicationRootPath(servletContext);// null SC ok
//			System.out.println(getClass() + " ApplicationRootPath: " + path);
//		}
//		
////		@Inject
////		public ApplicationRootPathProvider(@Nullable ServletContext servletContext){
////			this.path = WebappTool.getApplicationRootPath(servletContext);// null SC ok
////			System.out.println(getClass() + " ApplicationRootPath: " + path);
////		}
//
//		// @Inject
//		// public ApplicationRootPathProvider(){
//		// this.path = WebappTool.getApplicationRootPath(null);//null SC ok
//		// logger.warn("ApplicationRootPath: "+path);
//		// System.out.println("syso provider ApplicationRootPath: "+path);
//		// }
//
//		@Override
//		public ApplicationRootPath get(){
//			return new ApplicationRootPath(path);
//		}
//	}
}