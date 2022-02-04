/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.aws.s3;

import java.time.Duration;

import io.datarouter.enums.DatarouterEnumTool;
import io.datarouter.enums.StringEnum;

public class S3Headers{

	public static final String KEY_ACL = "x-amz-acl";
	public static final String KEY_CONTENT_TYPE = "Content-Type";
	public static final String KEY_EXPIRES = "Expires";
	public static final String KEY_CACHE_CONTROL = "Cache-Control";

	public static final String ACL_PUBLIC_READ = "public-read";
	public static final String ACL_PRIVATE = ""; //private is the default acl for s3

	public static final String CACHE_CONTROL_NO_CACHE = "no-cache";
	public static final String CACHE_CONTROL_MINUTES_20 = makeCacheControlString(Duration.ofMinutes(20));
	public static final String CACHE_CONTROL_WEEKS_1 = makeCacheControlString(Duration.ofDays(7));
	public static final String CACHE_CONTROL_MONTHS_1 = makeCacheControlString(Duration.ofDays(30));
	public static final String CACHE_CONTROL_YEARS_10 = makeCacheControlString(Duration.ofDays(365 * 10));

	private static String makeCacheControlString(Duration duration){
		long seconds = duration.toSeconds();
		return "max-age=" + seconds + ", public";
	}

	public static final String getExtensionForContentType(String contentType){
		ContentType type = ContentType.fromMimeType(contentType);
		if(type == null){
			return null;
		}
		return type.getExtension();
	}

	public interface S3ContentType{

		String getMimeType();

	}

	public static enum ContentType implements StringEnum<ContentType>, S3ContentType{
		PNG("image/png","png"),
		JPEG("image/jpeg","jpg"),
		SWF("application/x-shockwave-flash","swf"),
		TEXT_PLAIN("text/plain","txt"),
		TEXT_CSV("text/csv","csv"),
		TEXT_TSV("text/tsv","tsv"),
		TEXT_XML("text/xml","xml"),
		TEXT_SHELLSCRIPT("text/x-shellscript", "sh"),
		APPLICATION_JSON("application/json","json"),
		APPLICATION_XML("application/xml",null), //s3's default
		TEXT_HTML("text/html","html"),
		MULTIPART_MIXED("multipart/mixed",null),
		GZIP("application/x-gzip","gz"),
		ZIP("application/zip","zip"),
		BINARY("binary/octet-stream", null),
		APPLICATION_OCTET_STREAM("application/octet-stream", null),
		PDF("application/pdf", "pdf"),
		MP3("audio/mpeg", "mp3")
		;

		private final String mimeType;
		private final String extension;

		ContentType(String mimeType, String extension){
			this.mimeType = mimeType;
			this.extension = extension;
		}

		@Override
		public String getMimeType(){
			return mimeType;
		}

		public String getExtension(){
			return extension;
		}

		public static ContentType fromExtension(String ext){
			ext = ext.toLowerCase();
			for(ContentType type : values()){
				if(ext.equals(type.getExtension())){
					return type;
				}
			}
			return APPLICATION_XML;
		}

		@Override
		public String getPersistentString(){
			return mimeType;
		}

		@Override
		public ContentType fromPersistentString(String mimeType){
			return fromMimeType(mimeType);
		}

		public static ContentType fromMimeType(String mimeType){
			return DatarouterEnumTool.getEnumFromString(values(), mimeType, null);
		}

	}
}
