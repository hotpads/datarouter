package com.hotpads.handler;

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

import com.hotpads.datarouter.util.ApplicationPaths;

@Singleton
public class LoadedLibraries{

	private static final String HOTPADS_MARKER = "-0.0.1-SNAPSHOT.jar";

	private Map<String,GitProperties> hotpads;
	private List<String> nonHotpads;

	@Inject
	public LoadedLibraries(ApplicationPaths applicationPaths){
		hotpads = new HashMap<>();
		nonHotpads = new ArrayList<>();
		String var = applicationPaths.getRootPath()+"/WEB-INF/lib";

		for(File file : new File(var).listFiles()){
			if(file.getName().endsWith(HOTPADS_MARKER)){
				try (ZipFile zf = new ZipFile(file)){
					ZipEntry entry = zf.getEntry("git.properties");
					GitProperties gitProperties = new GitProperties(zf.getInputStream(entry));
					hotpads.put(file.getName().replace(HOTPADS_MARKER, ""), gitProperties);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}else if(file.getName().endsWith(".jar")){
				nonHotpads.add(file.getName());
			}
		}
	}

	public Map<String, GitProperties> getHotpadsLibraries(){
		return hotpads;
	}

	public List<String> getNonHotpadsLibraries(){
		return nonHotpads;
	}

}
