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
package io.datarouter.web.monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.jar.Manifest;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.net.UrlTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.app.ApplicationPaths;

@Singleton
public class ManifestDetails{
	private static final Logger logger = LoggerFactory.getLogger(ManifestDetails.class);

	public static final String FILE_NAME = "META-INF/MANIFEST.MF";

	private static final List<String> BUILD_KEYWORDS = List.of("Build-Jdk-Spec", "Build-Jdk", "Built-JDK",
			"Created-By");

	private Pair<String,String> buildPair;
	private String manifestString;

	@Inject
	public ManifestDetails(ApplicationPaths applicationPaths){
		URL url = UrlTool.create("file:" + applicationPaths.getRootPath() + "/" + FILE_NAME);
		logger.warn("loading manifest info from {}", url);
		try(InputStream resourceAsStream = url.openStream()){
			load(resourceAsStream);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public ManifestDetails(InputStream inputStream){
		load(inputStream);
	}

	public void load(InputStream inputStream){
		Manifest manifest;
		try{
			manifest = new Manifest(inputStream);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		for(String keyword : BUILD_KEYWORDS){
			String value = manifest.getMainAttributes().getValue(keyword);
			if(value != null){
				this.buildPair = new Pair<>(keyword, value);
				break;
			}
		}
		byte[] byteArray;
		try(var baos = new ByteArrayOutputStream()){
			manifest.write(baos);
			byteArray = baos.toByteArray();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		this.manifestString = new String(byteArray);
	}

	public Pair<String,String> getBuildPair(){
		return buildPair;
	}

	public String getManifestString(){
		return manifestString;
	}

}
