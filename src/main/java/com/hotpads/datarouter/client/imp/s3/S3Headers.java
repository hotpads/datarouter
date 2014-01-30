package com.hotpads.datarouter.client.imp.s3;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public class S3Headers {

	public static final String KEY_ACL = "x-amz-acl";
	public static final String KEY_CONTENT_TYPE = "Content-Type";
	public static final String KEY_EXPIRES = "Expires";
	public static final String KEY_CACHE_CONTROL = "Cache-Control";
	
	
	public static final String ACL_PUBLIC_READ = "public-read";
	public static final String ACL_PRIVATE = ""; //private is the default acl for s3
	
	
	/*
	 *   http://www.utoronto.ca/webdocs/HTMLdocs/Book/Book-3ed/appb/mimetype.html
	 */
//	public static final String CONTENT_TYPE_PNG = "image/png";
//	public static final String CONTENT_TYPE_JPEG = "image/jpeg";
//	public static final String CONTENT_TYPE_SWF = "application/x-shockwave-flash";
//	public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
//	public static final String CONTENT_TYPE_TEXT_XML = "text/xml";
//	public static final String CONTENT_TYPE_TEXT_HTML ="text/html";
//	public static final String CONTENT_TYPE_MULTIPART_MIXED ="multipart/mixed";
//	public static final String CONTENT_TYPE_GZIP = "application/x-gzip";
//	
//	public static final String EXTENSION_TEXT_HTML = "html";
//	public static final String EXTENSION_PNG = "png";
//	public static final String EXTENSION_JPEG = "jpg";
//	public static final String EXTENSION_SWF = "swf";
//	public static final String EXTENSION_TEXT_PLAIN = "txt";
//	public static final String EXTENSION_TEXT_XML = "xml";
	
	public static final String 
		CACHE_CONTROL_NO_CACHE = "no-cache",
		CACHE_CONTROL_MINUTES_20 = makeCacheControlString(20*60),
		CACHE_CONTROL_WEEKS_1 = makeCacheControlString(7*24*60*60),
		CACHE_CONTROL_MONTHS_1 = makeCacheControlString(30*24*60*60),
		CACHE_CONTROL_YEARS_10 = makeCacheControlString(10*365*24*60*60);
	
	private static String makeCacheControlString(int seconds){
		return "max-age="+seconds+", public";
	}
	public static final String getExtensionForContentType(String contentType){
		ContentType type = ContentType.fromMimeType(contentType);
		if(type==null) return null;
		return type.getExtension();
	}
	
	public static enum ContentType implements StringEnum<ContentType>{
		PNG("image/png","png"),
		JPEG("image/jpeg","jpg"),
		SWF("application/x-shockwave-flash","swf"),
		TEXT_PLAIN("text/plain","txt"),
		TEXT_CSV("text/csv","csv"),
		TEXT_XML("text/xml","xml"),
		APPLICATION_XML("application/xml",null), //s3's default
		TEXT_HTML("text/html","html"),
		MULTIPART_MIXED("multipart/mixed",null),
		GZIP("application/x-gzip","gz"),
		;
		
		private String mimeType;
		private String extension;
		
		private ContentType(String mimeType, String extension){
			this.mimeType = mimeType;
			this.extension = extension;
		}
		
		public String getMimeType(){
			return mimeType;
		}
		public String getExtension(){
			return extension;
		}
		
		public static ContentType fromExtension(String ext){
			ext = ext.toLowerCase();
			for(ContentType type : values()){
				if(ext.equals(type.getExtension())) return type;
			}
			return APPLICATION_XML;
		}

		@Override
		public String getPersistentString() {
			return mimeType;
		}

		@Override
		public ContentType fromPersistentString(String mimeType) {
			return fromMimeType(mimeType);
		}
		public static ContentType fromMimeType(String mimeType){
			return DataRouterEnumTool.getEnumFromString(
					values(), mimeType, null);
		}
	}
}
