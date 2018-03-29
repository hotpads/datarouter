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
package io.datarouter.web.monitoring;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.web.app.ApplicationPaths;

@Singleton
public class LoadedLibraries{

	public final Map<String,GitProperties> gitDetailedLibraries;
	public final Map<String,BuildProperties> buildDetailedLibraries;
	public final Collection<String> otherLibraries;

	@Inject
	public LoadedLibraries(ApplicationPaths applicationPaths){
		gitDetailedLibraries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		buildDetailedLibraries = new HashMap<>();
		otherLibraries = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		String var = applicationPaths.getRootPath() + "/WEB-INF/lib";

		for(File file : new File(var).listFiles()){
			try(ZipFile zf = new ZipFile(file)){
				ZipEntry entry = zf.getEntry("git.properties");
				if(entry != null){
					GitProperties gitProperties = new GitProperties(zf.getInputStream(entry));
					gitDetailedLibraries.put(file.getName(), gitProperties);
				}else{
					otherLibraries.add(file.getName());
				}
				entry = zf.getEntry(BuildProperties.FILE_NAME);
				if(entry != null){
					BuildProperties gitProperties = new BuildProperties(zf.getInputStream(entry));
					buildDetailedLibraries.put(file.getName(), gitProperties);
				}
			}catch(IOException ex){
				throw new RuntimeException(ex);
			}
		}
	}

}
