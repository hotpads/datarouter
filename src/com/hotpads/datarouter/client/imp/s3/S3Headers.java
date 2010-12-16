package com.hotpads.datarouter.client.imp.s3;

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
	public static final String CONTENT_TYPE_PNG = "image/png";
	public static final String CONTENT_TYPE_JPEG = "image/jpeg";
	public static final String CONTENT_TYPE_SWF = "application/x-shockwave-flash";
	public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
	public static final String CONTENT_TYPE_TEXT_XML = "text/xml";
	public static final String CONTENT_TYPE_TEXT_HTML ="text/html";
	public static final String CONTENT_TYPE_MULTIPART_MIXED ="multipart/mixed";
	public static final String CONTENT_TYPE_GZIP = "application/x-gzip";
	
	public static final String EXTENSION_TEXT_HTML = "html";
	public static final String EXTENSION_PNG = "png";
	public static final String EXTENSION_JPEG = "jpg";
	public static final String EXTENSION_SWF = "swf";
	public static final String EXTENSION_TEXT_PLAIN = "txt";
	public static final String EXTENSION_TEXT_XML = "xml";
	
	public static final String CACHE_CONTROL_NO_CACHE = "no-cache";
	public static final String CACHE_CONTROL_MINUTES_20 = "max-age=" + Integer.valueOf(20*60).toString() + ", public";
	public static final String CACHE_CONTROL_WEEKS_1 = "max-age=" + Integer.valueOf(7*24*60*60).toString() + ", public";
	public static final String CACHE_CONTROL_MONTHS_1 = "max-age=" + Integer.valueOf(30*24*60*60).toString() + ", public";
	public static final String CACHE_CONTROL_YEARS_10 = "max-age=" + Integer.valueOf(10*365*24*60*60).toString() + ", public";
	
	public static final String getExtensionForContentType(String contentType){
		if(CONTENT_TYPE_JPEG.equals(contentType)){ return EXTENSION_JPEG; }
		else if(CONTENT_TYPE_SWF.equals(contentType)){ return EXTENSION_SWF; }
		else if(CONTENT_TYPE_TEXT_PLAIN.equals(contentType)){ return EXTENSION_TEXT_PLAIN; }
		else if(CONTENT_TYPE_TEXT_XML.equals(contentType)){ return EXTENSION_TEXT_XML; }
		else if(CONTENT_TYPE_PNG.equals(contentType)) { return EXTENSION_PNG; }
		else if (CONTENT_TYPE_TEXT_HTML.equals(contentType)) { return EXTENSION_TEXT_HTML; }
		else{ return null; }
	}
}
