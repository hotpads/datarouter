/**
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
package io.datarouter.util.net;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlTool{

	public static URL create(String url){
		try{
			return new URL(url);
		}catch(MalformedURLException e){
			throw new RuntimeException("malformed url: " + url, e);
		}
	}

	public static String encode(String url){
		try{
			return URLEncoder.encode(url, StandardCharsets.UTF_8.name());
		}catch(UnsupportedEncodingException exception){
			throw new RuntimeException(exception);
		}
	}

	public static String decode(String encodedUrl){
		try{
			return URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.name());
		}catch(UnsupportedEncodingException exception){
			throw new RuntimeException(exception);
		}
	}

}
