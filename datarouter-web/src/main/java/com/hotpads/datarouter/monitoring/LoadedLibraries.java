package com.hotpads.datarouter.monitoring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.app.ApplicationPaths;

@Singleton
public class LoadedLibraries{

	private Map<String,GitProperties> detailedLibraries;
	private List<String> otherLibraries;

	@Inject
	public LoadedLibraries(ApplicationPaths applicationPaths){
		detailedLibraries = new HashMap<>();
		otherLibraries = new ArrayList<>();
		String var = applicationPaths.getRootPath() + "/WEB-INF/lib";

		for(File file : new File(var).listFiles()){
			try (ZipFile zf = new ZipFile(file)){
				ZipEntry entry = zf.getEntry("git.properties");
				if(entry != null){
					GitProperties gitProperties = new GitProperties(zf.getInputStream(entry));
					detailedLibraries.put(file.getName(), gitProperties);
				}else{
					otherLibraries.add(file.getName());
				}
			}catch(IOException ex){
				throw new RuntimeException(ex);
			}
		}
	}

	public Map<String,GitProperties> getDetailedLibraries(){
		return detailedLibraries;
	}

	public List<String> getOtherLibraries(){
		return otherLibraries;
	}

}
