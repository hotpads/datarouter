package com.hotpads.logging;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.WebAppName;
import com.hotpads.setting.DatarouterSettings;

@Singleton
public class LoggingConfigUpdaterJob implements Runnable{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private HotPadsLoggingConfigDao hotPadsLoggingConfigDao;
	@Inject
	private HotPadsLog4j2Configurator hotPadsLog4j2Configurator;
	@Inject
	private DatarouterSettings datarouterSettings;
	@Inject
	private WebAppName webAppName;
	
	private String previousSignature;

	public void setInitialSignature(String initialSignature){
		this.previousSignature = initialSignature;
	}

	public void setPreviousSignature(String previousSignature){
		this.previousSignature = previousSignature;
	}

	@Override
	public void run(){
		System.out.println();
		System.out.println("WebApp : " + webAppName);
//		Properties properties = System.getProperties();
//		String classPath = properties.getProperty("java.class.path");
//		System.out.println(classPath);
//		System.out.println(properties.entrySet());
		ClassLoader classLoader = LoggingConfigUpdaterJob.class.getClassLoader();
//		System.out.println("class");
		processClassLoader(classLoader);
		processClassLoader(classLoader.getParent());
		processClassLoader(classLoader.getParent().getParent());
		processClassLoader(classLoader.getParent().getParent().getParent());
//		processClassLoader(classLoader.getParent().getParent().getParent().getParent());
//		ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
//		System.out.println("system");
//		System.out.println(systemClassLoader);
//		System.out.println(systemClassLoader.getParent());
//		System.out.println(systemClassLoader.getParent().getParent());
//		try{
//			System.out.println(new GitProperties().getBranch());
//		}catch (IOException e){
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		if(!datarouterSettings.getLoggingConfigUpdaterEnabled().getValue()){
			return;
		}
		LoggingConfig config = hotPadsLoggingConfigDao.loadConfig();
		logger.debug("Logging config updater is running on " + webAppName);
		logger.debug("Logging config signature = " + config.getSignature());
		if(!config.getSignature().equals(previousSignature)){
			logger.info("Logging config apllied on " + webAppName);
			hotPadsLog4j2Configurator.applyConfig(config);
			previousSignature = config.getSignature();
		}
	}

	private void processClassLoader(ClassLoader classLoader){
		System.out.println("processing " + classLoader  + " : ");
		Enumeration<URL> rs = null;
		try{
			rs = classLoader.getResources("");
		}catch (IOException e){
			throw new RuntimeException(e);
		}
		while (rs.hasMoreElements()){
			URL url = rs.nextElement();
			System.out.println(url);
			Collection<String> resources = getResources(new File(url.getPath()), Pattern.compile(".*"));
			for (String ressource : resources){
				System.out.println(ressource);
			}
			System.out.println();
		}
		System.out.println();
	}

	private static Collection<String> getResources(File file, Pattern pattern){
		List<String> retval = new ArrayList<String>();
		if (file.isDirectory()){
			retval.addAll(getResourcesFromDirectory(file, pattern));
		}else{
			System.out.println("jar");
			retval.addAll(getResourcesFromJarFile(file, pattern));
		}
		return retval;
	}

	private static Collection<String> getResourcesFromJarFile(File file, Pattern pattern){
		List<String> retval = new ArrayList<String>();
		ZipFile zf;
		try{
			zf = new ZipFile(file);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		Enumeration<? extends ZipEntry> e = zf.entries();
		while (e.hasMoreElements()){
			ZipEntry ze = e.nextElement();
			System.out.println(ze);
			System.out.println(ze.getName());
			String fileName = ze.getName();
			boolean accept = pattern.matcher(fileName).matches();
			if (accept){
				retval.add(fileName);
			}
		}
		try{
			zf.close();
		}catch(IOException e1){
			throw new RuntimeException(e1);
		}
		return retval;
	}

	private static Collection<String> getResourcesFromDirectory(File directory, Pattern pattern){
		List<String> retval = new ArrayList<String>();
		File[] fileList = directory.listFiles();
		for (File file : fileList){
			if (file.isDirectory()){
				retval.addAll(getResourcesFromDirectory(file, pattern));
			}else{
				try{
					String fileName = file.getCanonicalPath();
					boolean accept = pattern.matcher(fileName).matches();
					if (accept){
						retval.add(fileName);
					}
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}
		}
		return retval;
	}

}
